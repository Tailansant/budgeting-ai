package dio.budgeting.infrastructure.ai;

import dio.budgeting.application.GetSpendingSummaryUseCase;
import dio.budgeting.application.ListLatestTransactionsUseCase;
import dio.budgeting.application.ListTransactionsByCategoryUseCase;
import dio.budgeting.application.PersistTransactionUseCase;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Isola a configuração do ChatClient (prompt de sistema + ferramentas) do controller.
 * Todas as ferramentas são os MESMOS casos de uso usados pelos endpoints REST,
 * então a IA nunca passa por cima das regras do domínio.
 */
@Component
public class BudgetAssistant {
    private final ChatClient chatClient;

    public BudgetAssistant(ChatClient.Builder chatClientBuilder,
                           @Value("classpath:prompts/system-message.st") Resource systemPrompt,
                           PersistTransactionUseCase persistTransactionUseCase,
                           ListTransactionsByCategoryUseCase listTransactionsByCategoryUseCase,
                           ListLatestTransactionsUseCase listLatestTransactionsUseCase,
                           GetSpendingSummaryUseCase getSpendingSummaryUseCase) throws IOException {
        this.chatClient = chatClientBuilder
                .defaultSystem(systemPrompt.getContentAsString(StandardCharsets.UTF_8))
                .defaultTools(
                        persistTransactionUseCase,
                        listTransactionsByCategoryUseCase,
                        listLatestTransactionsUseCase,
                        getSpendingSummaryUseCase)
                .build();
    }

    public String ask(String message) {
        return chatClient.prompt().user(message).call().content();
    }
}
