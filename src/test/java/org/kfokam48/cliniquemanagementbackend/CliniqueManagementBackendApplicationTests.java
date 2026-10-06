package org.kfokam48.cliniquemanagementbackend;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Démarre l'application complète : nécessite une vraie base de données.
 * Exécuté en CI (PostgreSQL de GitHub Actions, voir .github/workflows) ou en local
 * lorsque SPRING_DATASOURCE_URL est défini ; ignoré sinon.
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "SPRING_DATASOURCE_URL", matches = ".+")
class CliniqueManagementBackendApplicationTests {

    @Test
    void contextLoads() {
    }

}
