package org.kfokam48.cliniquemanagementbackend.migration;

import org.junit.jupiter.api.Test;
import org.kfokam48.cliniquemanagementbackend.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * I9 : la migration Flyway db/migration/sqlserver crée un schéma qu'Hibernate accepte en
 * ddl-auto=validate (tables, colonnes et types conformes aux entités).
 *
 * Exécuté sur H2 en mode MSSQLServer. H2 ne connaît pas le type DATETIMEOFFSET de SQL Server :
 * les migrations sont copiées dans target/ et ce seul type y est remplacé par son équivalent H2
 * (TIMESTAMP WITH TIME ZONE). Les fichiers de src/main/resources ne sont pas modifiés.
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
        registry.add("spring.flyway.locations", () -> "filesystem:" + copierMigrationsPourH2());
    }

    private static Path copierMigrationsPourH2() {
        try {
            Path cible = Files.createDirectories(Path.of("target", "flyway-sqlserver-h2"));
            for (Resource migration : new PathMatchingResourcePatternResolver()
                    .getResources("classpath:db/migration/sqlserver/*.sql")) {
                String sql = migration.getContentAsString(StandardCharsets.UTF_8)
                        .replace("DATETIMEOFFSET(6)", "TIMESTAMP(6) WITH TIME ZONE");
                Files.writeString(cible.resolve(migration.getFilename()), sql, StandardCharsets.UTF_8);
            }
            return cible.toAbsolutePath();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Test
    void schemaCreatedByFlywayMatchesTheEntities() {
        // Le contexte ne démarre que si la validation Hibernate du schéma a réussi
        assertThat(utilisateurRepository.count()).isZero();
    }
}
