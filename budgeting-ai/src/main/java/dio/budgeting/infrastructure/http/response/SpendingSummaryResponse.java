package dio.budgeting.infrastructure.http.response;

import dio.budgeting.application.output.SpendingSummaryOutput;

import java.util.List;

public record SpendingSummaryResponse(double totalSpent, int transactionCount, List<CategoryTotalResponse> byCategory) {

    public record CategoryTotalResponse(String category, double total, int transactionCount) {
    }

    public static SpendingSummaryResponse from(SpendingSummaryOutput output) {
        return new SpendingSummaryResponse(
                output.totalSpent(),
                output.transactionCount(),
                output.byCategory().stream()
                        .map(c -> new CategoryTotalResponse(c.category(), c.total(), c.transactionCount()))
                        .toList());
    }
}
