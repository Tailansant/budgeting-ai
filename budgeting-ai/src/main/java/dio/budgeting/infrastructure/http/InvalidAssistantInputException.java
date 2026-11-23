package dio.budgeting.infrastructure.http;

/** Entrada inutilizável para o assistente: áudio vazio, áudio ininteligível ou mensagem em branco. */
public class InvalidAssistantInputException extends RuntimeException {
    public InvalidAssistantInputException(String message) {
        super(message);
    }
}
