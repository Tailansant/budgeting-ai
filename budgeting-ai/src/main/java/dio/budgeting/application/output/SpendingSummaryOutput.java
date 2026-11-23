package dio.budgeting.application.output;

import java.util.List;

public record SpendingSummaryOutput(double totalSpent, int transactionCount, List<CategoryTotal> byCategory) {

    public record CategoryTotal(String category, double total, int transactionCount) {
    }
}
