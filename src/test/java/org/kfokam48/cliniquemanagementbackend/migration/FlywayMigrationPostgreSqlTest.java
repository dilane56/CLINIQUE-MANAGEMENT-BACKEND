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
 * I9 : les migrations Flyway db/migration/postgresql créent un schéma qu'Hibernate accepte en
 * ddl-auto=validate (tables, colonnes et types conformes aux entités).
 *
 * Exécuté sur H2 en mode PostgreSQL. H2 ne supporte pas la contrainte d'exclusion de V2
 * (EXCLUDE USING gist) : dans la copie de test, V2 est remplacée par une instruction neutre.
 * V2 est vérifiée sur un vrai PostgreSQL par PostgreSqlIntegrationTest (CI).
 */
@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:flywaypostgresql;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        // Indépendant du profil actif (la CI utilise "prod", qui impose le dialecte PostgreSQL)
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=validate"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FlywayMigrationPostgreSqlTest {

    @DynamicPropertySource
    static void migrationsAdapteesPourH2(DynamicPropertyRegistry registry) {
        registry.add("spring.flyway.locations", () -> MigrationsPourH2.copier("postgresql",
                (fichier, sql) -> fichier.startsWith("V2__")
                        ? "-- Contrainte d'exclusion PostgreSQL non supportée par H2 (vérifiée en CI)\nSELECT 1;"
                        : sql));
    }

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Test
    void schemaCreatedByFlywayMatchesTheEntities() {
        // Le contexte ne démarre que si la validation Hibernate du schéma a réussi
        assertThat(utilisateurRepository.count()).isZero();
    }
}
