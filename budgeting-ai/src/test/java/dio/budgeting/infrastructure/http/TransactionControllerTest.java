package dio.budgeting.infrastructure.http;

import dio.budgeting.application.GetSpendingSummaryUseCase;
import dio.budgeting.application.ListLatestTransactionsUseCase;
import dio.budgeting.application.ListTransactionsByCategoryUseCase;
import dio.budgeting.application.PersistTransactionUseCase;
import dio.budgeting.application.input.PersistTransactionInput;
import dio.budgeting.application.output.SpendingSummaryOutput;
import dio.budgeting.application.output.SpendingSummaryOutput.CategoryTotal;
import dio.budgeting.application.output.TransactionOutput;
import dio.budgeting.domain.InvalidTransactionException;
import dio.budgeting.infrastructure.ai.BudgetAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testa o contrato HTTP e o fluxo de IA sem chamar a OpenAI:
 * os modelos de IA e os casos de uso são substituídos por mocks (MockMvc em modo standalone).
 */
@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    PersistTransactionUseCase persistTransactionUseCase;
    @Mock
    ListTransactionsByCategoryUseCase listTransactionsByCategoryUseCase;
    @Mock
    GetSpendingSummaryUseCase getSpendingSummaryUseCase;
    @Mock
    ListLatestTransactionsUseCase listLatestTransactionsUseCase;
    @Mock
    BudgetAssistant budgetAssistant;
    @Mock
    TranscriptionModel transcriptionModel;
    @Mock
    TextToSpeechModel textToSpeechModel;

    MockMvc mvc;

    @BeforeEach
    void setUp() {
        var controller = new TransactionController(
                persistTransactionUseCase,
                listTransactionsByCategoryUseCase,
                getSpendingSummaryUseCase,
                listLatestTransactionsUseCase,
                budgetAssistant,
                transcriptionModel,
                textToSpeechModel);

        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    // ---------- REST ----------

    @Test
    void should_return201_when_transactionIsCreated() throws Exception {
        when(persistTransactionUseCase.execute(any(PersistTransactionInput.class)))
                .thenReturn(new TransactionOutput("id-1", "Mercado", "GROCERIES", 80.0, null));

        mvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Mercado\",\"category\":\"GROCERIES\",\"amount\":8000}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.category").value("GROCERIES"))
                .andExpect(jsonPath("$.amount").value(80.0));
    }

    @Test
    void should_return422WithReason_when_transactionBreaksBusinessRules() throws Exception {
        when(persistTransactionUseCase.execute(any(PersistTransactionInput.class)))
                .thenThrow(new InvalidTransactionException("O valor da transação deve ser maior que zero."));

        mvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Mercado\",\"category\":\"GROCERIES\",\"amount\":0}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("O valor da transação deve ser maior que zero."));
    }

    @Test
    void should_return400_when_categoryInBodyDoesNotExist() throws Exception {
        mvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Mercado\",\"category\":\"INEXISTENTE\",\"amount\":8000}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_return400_when_categoryInPathDoesNotExist() throws Exception {
        mvc.perform(get("/transactions/INEXISTENTE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_returnSummary_when_summaryEndpointIsCalled() throws Exception {
        when(getSpendingSummaryUseCase.execute(null)).thenReturn(
                new SpendingSummaryOutput(150.0, 3, List.of(new CategoryTotal("GROCERIES", 100.0, 2))));

        mvc.perform(get("/transactions/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSpent").value(150.0))
                .andExpect(jsonPath("$.transactionCount").value(3))
                .andExpect(jsonPath("$.byCategory[0].category").value("GROCERIES"));
    }

    // ---------- IA (texto) ----------

    @Test
    void should_returnAssistantAnswer_when_textMessageIsSent() throws Exception {
        when(budgetAssistant.ask("quanto gastei?")).thenReturn("Você gastou cento e cinquenta reais.");

        mvc.perform(post("/transactions/ai/text")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"quanto gastei?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("Você gastou cento e cinquenta reais."));
    }

    @Test
    void should_return422_when_textMessageIsBlank() throws Exception {
        mvc.perform(post("/transactions/ai/text")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"   \"}"))
                .andExpect(status().isUnprocessableEntity());

        verifyNoInteractions(budgetAssistant);
    }

    // ---------- IA (voz): áudio -> texto -> assistente -> áudio ----------

    @Test
    void should_returnSpeech_when_audioIsSent() throws Exception {
        byte[] speech = "mp3-bytes".getBytes(StandardCharsets.UTF_8);
        when(transcriptionModel.transcribe(any(Resource.class))).thenReturn("gastei oitenta reais no mercado");
        when(budgetAssistant.ask("gastei oitenta reais no mercado")).thenReturn("Gasto registrado.");
        when(textToSpeechModel.call("Gasto registrado.")).thenReturn(speech);

        var audio = new MockMultipartFile("file", "gasto.m4a", "audio/mp4", new byte[]{1, 2, 3});

        mvc.perform(multipart("/transactions/ai").file(audio))
                .andExpect(status().isOk())
                .andExpect(content().bytes(speech));
    }

    @Test
    void should_return422_when_audioCannotBeTranscribed() throws Exception {
        when(transcriptionModel.transcribe(any(Resource.class))).thenReturn("   ");

        var audio = new MockMultipartFile("file", "silencio.m4a", "audio/mp4", new byte[]{1, 2, 3});

        mvc.perform(multipart("/transactions/ai").file(audio))
                .andExpect(status().isUnprocessableEntity());

        verifyNoInteractions(budgetAssistant, textToSpeechModel);
    }

    @Test
    void should_return422_when_audioFileIsEmpty() throws Exception {
        var audio = new MockMultipartFile("file", "vazio.m4a", "audio/mp4", new byte[0]);

        mvc.perform(multipart("/transactions/ai").file(audio))
                .andExpect(status().isUnprocessableEntity());

        verifyNoInteractions(transcriptionModel, budgetAssistant, textToSpeechModel);
    }
}
