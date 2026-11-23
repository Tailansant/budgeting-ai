package dio.budgeting.infrastructure.http;

import dio.budgeting.domain.Category;
import dio.budgeting.domain.InvalidTransactionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Arrays;

/** Converte exceções em respostas HTTP padronizadas (RFC 7807 / ProblemDetail). */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(InvalidTransactionException.class)
    public ProblemDetail handleInvalidTransaction(InvalidTransactionException ex) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Transação inválida", ex.getMessage());
    }

    @ExceptionHandler(InvalidAssistantInputException.class)
    public ProblemDetail handleInvalidAssistantInput(InvalidAssistantInputException ex) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Entrada inválida para o assistente", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String detail = "Valor inválido para '" + ex.getName() + "'.";
        if (Category.class.equals(ex.getRequiredType())) {
            detail += " Categorias aceitas: " + Arrays.toString(Category.values()) + ".";
        }
        return problem(HttpStatus.BAD_REQUEST, "Parâmetro inválido", detail);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadableBody(HttpMessageNotReadableException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido",
                "O JSON enviado é inválido ou contém valores não reconhecidos. "
                        + "Categorias aceitas: " + Arrays.toString(Category.values()) + ".");
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}
