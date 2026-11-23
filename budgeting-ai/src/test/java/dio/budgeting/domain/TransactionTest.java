package dio.budgeting.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionTest {

    @Test
    void should_createTransaction_when_dataIsValid() {
        var transaction = new Transaction("  Mercado  ", 8000, Category.GROCERIES);

        assertThat(transaction.getId()).isNotNull();
        assertThat(transaction.getDescription()).isEqualTo("Mercado");
        assertThat(transaction.getAmount()).isEqualTo(8000);
        assertThat(transaction.getCategory()).isEqualTo(Category.GROCERIES);
        assertThat(transaction.getCreatedAt()).isNotNull();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void should_rejectTransaction_when_descriptionIsBlank(String description) {
        assertThatThrownBy(() -> new Transaction(description, 1000, Category.AUTO))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("descrição");
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1, -8000})
    void should_rejectTransaction_when_amountIsNotPositive(long amount) {
        assertThatThrownBy(() -> new Transaction("Gasolina", amount, Category.AUTO))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("maior que zero");
    }

    @Test
    void should_rejectTransaction_when_amountExceedsLimit() {
        assertThatThrownBy(() -> new Transaction("Carro", Transaction.MAX_AMOUNT_IN_CENTS + 1, Category.AUTO))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("100.000,00");
    }

    @Test
    void should_acceptTransaction_when_amountEqualsLimit() {
        var transaction = new Transaction("Carro", Transaction.MAX_AMOUNT_IN_CENTS, Category.AUTO);

        assertThat(transaction.getAmount()).isEqualTo(Transaction.MAX_AMOUNT_IN_CENTS);
    }

    @Test
    void should_rejectTransaction_when_categoryIsNull() {
        assertThatThrownBy(() -> new Transaction("Mercado", 1000, null))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("categoria");
    }

    @Test
    void should_rejectTransaction_when_descriptionIsTooLong() {
        var longDescription = "a".repeat(Transaction.MAX_DESCRIPTION_LENGTH + 1);

        assertThatThrownBy(() -> new Transaction(longDescription, 1000, Category.OTHER))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("no máximo");
    }
}
