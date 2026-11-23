package dio.budgeting.application;

import dio.budgeting.application.output.Money;
import dio.budgeting.application.output.SpendingSummaryOutput;
import dio.budgeting.application.output.SpendingSummaryOutput.CategoryTotal;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Novo caso de uso: "quanto eu gastei?".
 * Usado pelo endpoint REST GET /transactions/summary e pela IA (Tool Calling).
 */
@Service
public class GetSpendingSummaryUseCase {
    private final TransactionRepository transactionRepository;

    public GetSpendingSummaryUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Tool(name = "get-spending-summary",
            description = "Informa o total gasto e a quantidade de transações, no geral ou em uma categoria, "
                    + "com o detalhamento por categoria. Use para perguntas como 'quanto gastei?'")
    public SpendingSummaryOutput execute(
            @ToolParam(description = "Categoria a consultar. Omita para somar todas as categorias",
                    required = false) Category category) {

        List<Transaction> transactions = category == null
                ? transactionRepository.findAll()
                : transactionRepository.findAllByCategory(category);

        Map<Category, List<Transaction>> grouped = new EnumMap<>(Category.class);
        for (Transaction transaction : transactions) {
            grouped.computeIfAbsent(transaction.getCategory(), key -> new ArrayList<>()).add(transaction);
        }

        List<CategoryTotal> byCategory = new ArrayList<>();
        for (Map.Entry<Category, List<Transaction>> entry : grouped.entrySet()) {
            long subtotal = entry.getValue().stream().mapToLong(Transaction::getAmount).sum();
            byCategory.add(new CategoryTotal(entry.getKey().name(), Money.fromCents(subtotal), entry.getValue().size()));
        }

        long total = transactions.stream().mapToLong(Transaction::getAmount).sum();
        return new SpendingSummaryOutput(Money.fromCents(total), transactions.size(), byCategory);
    }
}
