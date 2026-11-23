package dio.budgeting.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class Transaction {
    public static final long MAX_AMOUNT_IN_CENTS = 10_000_000L; // R$ 100.000,00
    public static final int MAX_DESCRIPTION_LENGTH = 120;

    private TransactionId id;
    private String description;
    private long amount;
    private Category category;
    private Instant createdAt;

    /** Cria uma NOVA transação, aplicando as regras de negócio. */
    public Transaction(String description, long amount, Category category) {
        validate(description, amount, category);
        this.id = new TransactionId();
        this.description = description.trim();
        this.amount = amount;
        this.category = category;
        this.createdAt = Instant.now();
    }

    private static void validate(String description, long amount, Category category) {
        if (description == null || description.isBlank()) {
            throw new InvalidTransactionException("A descrição da transação é obrigatória.");
        }
        if (description.trim().length() > MAX_DESCRIPTION_LENGTH) {
            throw new InvalidTransactionException(
                    "A descrição deve ter no máximo " + MAX_DESCRIPTION_LENGTH + " caracteres.");
        }
        if (amount <= 0) {
            throw new InvalidTransactionException("O valor da transação deve ser maior que zero.");
        }
        if (amount > MAX_AMOUNT_IN_CENTS) {
            throw new InvalidTransactionException("O valor da transação não pode passar de R$ 100.000,00.");
        }
        if (category == null) {
            throw new InvalidTransactionException("A categoria da transação é obrigatória.");
        }
    }
}
