package dio.budgeting.application;

import dio.budgeting.application.input.PersistTransactionInput;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.InvalidTransactionException;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersistTransactionUseCaseTest {

    @Mock
    TransactionRepository repository;

    @InjectMocks
    PersistTransactionUseCase useCase;

    @Test
    void should_saveTransactionAndReturnValueInReais_when_inputIsValid() {
        when(repository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var output = useCase.execute(new PersistTransactionInput("Farmácia", 4050, Category.PHARMA));

        assertThat(output.description()).isEqualTo("Farmácia");
        assertThat(output.category()).isEqualTo("PHARMA");
        assertThat(output.value()).isEqualTo(40.50);
        verify(repository).save(any(Transaction.class));
    }

    @Test
    void should_notSaveAnything_when_inputBreaksBusinessRules() {
        var invalid = new PersistTransactionInput("Mercado", -100, Category.GROCERIES);

        assertThatThrownBy(() -> useCase.execute(invalid)).isInstanceOf(InvalidTransactionException.class);

        verifyNoInteractions(repository);
    }
}
