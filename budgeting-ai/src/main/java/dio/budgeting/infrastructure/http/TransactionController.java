package dio.budgeting.infrastructure.http;

import dio.budgeting.application.GetSpendingSummaryUseCase;
import dio.budgeting.application.ListLatestTransactionsUseCase;
import dio.budgeting.application.ListTransactionsByCategoryUseCase;
import dio.budgeting.application.PersistTransactionUseCase;
import dio.budgeting.domain.Category;
import dio.budgeting.infrastructure.ai.BudgetAssistant;
import dio.budgeting.infrastructure.http.request.AssistantRequest;
import dio.budgeting.infrastructure.http.request.TransactionRequest;
import dio.budgeting.infrastructure.http.response.AssistantResponse;
import dio.budgeting.infrastructure.http.response.SpendingSummaryResponse;
import dio.budgeting.infrastructure.http.response.TransactionResponse;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {
    private final PersistTransactionUseCase persistTransactionUseCase;
    private final ListTransactionsByCategoryUseCase listTransactionsByCategoryUseCase;
    private final GetSpendingSummaryUseCase getSpendingSummaryUseCase;
    private final ListLatestTransactionsUseCase listLatestTransactionsUseCase;

    private final BudgetAssistant budgetAssistant;
    private final TranscriptionModel transcriptionModel;
    private final TextToSpeechModel textToSpeechModel;

    public TransactionController(PersistTransactionUseCase persistTransactionUseCase,
                                 ListTransactionsByCategoryUseCase listTransactionsByCategoryUseCase,
                                 GetSpendingSummaryUseCase getSpendingSummaryUseCase,
                                 ListLatestTransactionsUseCase listLatestTransactionsUseCase,
                                 BudgetAssistant budgetAssistant,
                                 TranscriptionModel transcriptionModel,
                                 TextToSpeechModel textToSpeechModel) {
        this.persistTransactionUseCase = persistTransactionUseCase;
        this.listTransactionsByCategoryUseCase = listTransactionsByCategoryUseCase;
        this.getSpendingSummaryUseCase = getSpendingSummaryUseCase;
        this.listLatestTransactionsUseCase = listLatestTransactionsUseCase;
        this.budgetAssistant = budgetAssistant;
        this.transcriptionModel = transcriptionModel;
        this.textToSpeechModel = textToSpeechModel;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse createTransaction(@RequestBody TransactionRequest request) {
        var transaction = persistTransactionUseCase.execute(request.toInput());
        return TransactionResponse.from(transaction);
    }

    @GetMapping("/{category}")
    public List<TransactionResponse> readTransactions(@PathVariable Category category) {
        return listTransactionsByCategoryUseCase.execute(category).stream().map(TransactionResponse::from).toList();
    }

    @GetMapping("/summary")
    public SpendingSummaryResponse summary(@RequestParam(required = false) Category category) {
        return SpendingSummaryResponse.from(getSpendingSummaryUseCase.execute(category));
    }

    @GetMapping("/latest")
    public List<TransactionResponse> latest(@RequestParam(required = false) Integer limit) {
        return listLatestTransactionsUseCase.execute(limit).stream().map(TransactionResponse::from).toList();
    }

    /** Fluxo completo: áudio -> texto -> IA + ferramentas -> texto -> áudio. */
    @PostMapping(value = "/ai", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = "audio/mp3")
    public ResponseEntity<Resource> askByVoice(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidAssistantInputException("O arquivo de áudio está vazio.");
        }

        var userMessage = transcriptionModel.transcribe(file.getResource());
        if (userMessage == null || userMessage.isBlank()) {
            throw new InvalidAssistantInputException("Não consegui entender o áudio. Tente gravar novamente.");
        }

        var answer = budgetAssistant.ask(userMessage);

        byte[] audio = textToSpeechModel.call(answer);
        var resource = new ByteArrayResource(audio);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename("audio.mp3")
                                .build()
                                .toString())
                .body(resource);
    }

    /** Mesmo assistente, mas com texto no lugar de áudio: ótimo para testar sem gravar nada. */
    @PostMapping(value = "/ai/text", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public AssistantResponse askByText(@RequestBody AssistantRequest request) {
        if (request == null || request.message() == null || request.message().isBlank()) {
            throw new InvalidAssistantInputException("A mensagem não pode estar em branco.");
        }
        return new AssistantResponse(budgetAssistant.ask(request.message()));
    }
}
