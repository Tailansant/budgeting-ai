package dio.budgeting.application;

import dio.budgeting.application.output.SpendingSummaryOutput.CategoryTotal;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetSpendingSummaryUseCaseTest {

    @Mock
    TransactionRepository repository;

    @InjectMocks
    GetSpendingSummaryUseCase useCase;

    @Test
    void should_sumAllTransactionsAndGroupByCategory_when_noCategoryIsGiven() {
        when(repository.findAll()).thenReturn(List.of(
                new Transaction("Mercado", 8000, Category.GROCERIES),
                new Transaction("Padaria", 2000, Category.GROCERIES),
                new Transaction("Remédio", 5000, Category.PHARMA)));

        var summary = useCase.execute(null);

        assertThat(summary.totalSpent()).isEqualTo(150.0);
        assertThat(summary.transactionCount()).isEqualTo(3);
        assertThat(summary.byCategory())
                .extracting(CategoryTotal::category, CategoryTotal::total, CategoryTotal::transactionCount)
                .containsExactly(
                        tuple("GROCERIES", 100.0, 2),
                        tuple("PHARMA", 50.0, 1));
    }

    @Test
    void should_filterByCategory_when_categoryIsGiven() {
        when(repository.findAllByCategory(Category.PHARMA)).thenReturn(List.of(
                new Transaction("Remédio", 5000, Category.PHARMA)));

        var summary = useCase.execute(Category.PHARMA);

        assertThat(summary.totalSpent()).isEqualTo(50.0);
        assertThat(summary.byCategory()).hasSize(1);
        verify(repository, never()).findAll();
    }

    @Test
    void should_returnZeros_when_thereAreNoTransactions() {
        when(repository.findAll()).thenReturn(List.of());

        var summary = useCase.execute(null);

        assertThat(summary.totalSpent()).isZero();
        assertThat(summary.transactionCount()).isZero();
        assertThat(summary.byCategory()).isEmpty();
    }
}
