package dio.budgeting.application.output;

import dio.budgeting.domain.Transaction;

public record TransactionOutput(String id, String description, String category, double value, String createdAt) {
    public static TransactionOutput from(Transaction transaction) {
        return new TransactionOutput(
                transaction.getId().uuid().toString(),
                transaction.getDescription(),
                transaction.getCategory().name(),
                Money.fromCents(transaction.getAmount()),
                transaction.getCreatedAt() == null ? null : transaction.getCreatedAt().toString());
    }
}
