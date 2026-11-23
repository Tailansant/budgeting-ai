package dio.budgeting.application.output;

import java.math.BigDecimal;

/** Conversão de centavos (como o domínio armazena) para reais (como a pessoa fala). */
public final class Money {
    private Money() {
    }

    public static double fromCents(long cents) {
        return BigDecimal.valueOf(cents, 2).doubleValue();
    }
}
