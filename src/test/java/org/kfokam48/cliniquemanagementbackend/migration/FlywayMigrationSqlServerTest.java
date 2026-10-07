package org.kfokam48.cliniquemanagementbackend.migration;

import org.junit.jupiter.api.Test;
import org.kfokam48.cliniquemanagementbackend.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * I9 : les migrations Flyway db/migration/sqlserver créent un schéma qu'Hibernate accepte en
 * ddl-auto=validate (tables, colonnes et types conformes aux entités).
 *
 * Exécuté sur H2 en mode MSSQLServer. Dans la copie de test, deux éléments propres à SQL Server
 * sont traduits pour H2 : le type DATETIMEOFFSET (-> TIMESTAMP WITH TIME ZONE) et
 * EXEC sp_rename (-> ALTER TABLE ... ALTER COLUMN ... RENAME TO).
 */
@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:flywaysqlserver;MODE=MSSQLServer;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        // Indépendant du profil actif (la CI utilise "prod", qui impose le dialecte PostgreSQL)
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=validate"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FlywayMigrationSqlServerTest {

    @DynamicPropertySource
    static void migrationsAdapteesPourH2(DynamicPropertyRegistry registry) {
        registry.add("spring.flyway.locations", () -> MigrationsPourH2.copier("sqlserver",
                (fichier, sql) -> sql
                        .replace("DATETIMEOFFSET(6)", "TIMESTAMP(6) WITH TIME ZONE")
                        .replaceAll("EXEC sp_rename '(\\w+)\\.(\\w+)', '(\\w+)', 'COLUMN';",
                                "ALTER TABLE $1 ALTER COLUMN $2 RENAME TO $3;")));
    }

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Test
    void schemaCreatedByFlywayMatchesTheEntities() {
        // Le contexte ne démarre que si la validation Hibernate du schéma a réussi
        assertThat(utilisateurRepository.count()).isZero();
    }
}
