package dio.budgeting.application;

import dio.budgeting.application.output.Money;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class MoneyTest {

    @ParameterizedTest
    @CsvSource({"8000,80.0", "1250,12.5", "5,0.05", "0,0.0"})
    void should_convertCentsToReais(long cents, double reais) {
        assertThat(Money.fromCents(cents)).isEqualTo(reais);
    }
}
