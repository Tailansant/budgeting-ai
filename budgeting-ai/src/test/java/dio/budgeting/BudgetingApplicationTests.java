package dio.budgeting;

import dio.budgeting.infrastructure.ai.BudgetAssistant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica que todo o contexto Spring sobe (incluindo as novas ferramentas de IA),
 * sem precisar de MySQL nem de chave real da OpenAI: nenhuma chamada externa é feita.
 */
@SpringBootTest(properties = {
        "spring.ai.openai.api-key=test-key",
        "spring.datasource.url=jdbc:h2:mem:budgeting-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
class BudgetingApplicationTests {

    @Autowired
    BudgetAssistant budgetAssistant;

    @Test
    void contextLoads() {
        assertThat(budgetAssistant).isNotNull();
    }
}
