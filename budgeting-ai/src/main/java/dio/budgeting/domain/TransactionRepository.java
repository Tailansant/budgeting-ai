package dio.budgeting.domain;

import java.util.List;

public interface TransactionRepository {
    Transaction save(Transaction transaction);

    List<Transaction> findAllByCategory(Category category);

    List<Transaction> findAll();

    /** Transações mais recentes primeiro, limitadas a {@code limit} itens. */
    List<Transaction> findLatest(int limit);
}
