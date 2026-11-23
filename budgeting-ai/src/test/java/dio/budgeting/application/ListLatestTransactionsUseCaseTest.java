package dio.budgeting.application;

import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListLatestTransactionsUseCaseTest {

    @Mock
    TransactionRepository repository;

    @InjectMocks
    ListLatestTransactionsUseCase useCase;

    @ParameterizedTest
    @CsvSource(nullValues = "null", value = {"null,5", "0,5", "-2,5", "3,3", "20,20", "100,20"})
    void should_applyDefaultAndMaximumLimit(Integer requested, int expected) {
        when(repository.findLatest(anyInt())).thenReturn(List.of());

        useCase.execute(requested);

        verify(repository).findLatest(expected);
    }

    @Test
    void should_mapTransactionsToOutputInReais() {
        when(repository.findLatest(anyInt())).thenReturn(List.of(new Transaction("Uber", 2590, Category.TRANSPORT)));

        var result = useCase.execute(1);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).value()).isEqualTo(25.90);
        assertThat(result.get(0).category()).isEqualTo("TRANSPORT");
    }
}
