package dio.budgeting.domain;

/**
 * Lançada quando uma transação viola uma regra de negócio.
 * A mensagem é escrita para a pessoa usuária: a IA a repassa na resposta falada
 * e a API a devolve no campo "detail" do erro HTTP.
 */
public class InvalidTransactionException extends RuntimeException {
    public InvalidTransactionException(String message) {
        super(message);
    }
}
