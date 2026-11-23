package dio.budgeting.application;

import dio.budgeting.application.output.TransactionOutput;
import dio.budgeting.domain.TransactionRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;

/** Novo caso de uso: "quais foram meus últimos gastos?". */
@Service
public class ListLatestTransactionsUseCase {
    public static final int DEFAULT_LIMIT = 5;
    public static final int MAX_LIMIT = 20;

    private final TransactionRepository transactionRepository;

    public ListLatestTransactionsUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Tool(name = "list-latest-transactions",
            description = "Lista as transações financeiras mais recentes, da mais nova para a mais antiga")
    public List<TransactionOutput> execute(
            @ToolParam(description = "Quantidade de transações (padrão 5, máximo 20)",
                    required = false) Integer limit) {

        int effectiveLimit = (limit == null || limit < 1) ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);
        return transactionRepository.findLatest(effectiveLimit).stream().map(TransactionOutput::from).toList();
    }
}
