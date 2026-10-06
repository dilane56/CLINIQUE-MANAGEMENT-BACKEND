package org.kfokam48.cliniquemanagementbackend.migration;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BinaryOperator;

/**
 * Copie les migrations Flyway d'une base dans target/ en adaptant les rares instructions que H2
 * ne connaît pas. Les fichiers de src/main/resources ne sont jamais modifiés : ce qui est adapté
 * ici est vérifié sur la vraie base par PostgreSqlIntegrationTest (CI) ou au démarrage en dev.
 */
final class MigrationsPourH2 {

    private MigrationsPourH2() {
    }

    /**
     * @param vendor     dossier db/migration/{vendor}
     * @param adaptation (nom du fichier, contenu) -> contenu adapté pour H2
     * @return l'emplacement à donner à spring.flyway.locations
     */
    static String copier(String vendor, BinaryOperator<String> adaptation) {
        try {
            Path cible = Files.createDirectories(Path.of("target", "flyway-" + vendor + "-h2"));
            for (Resource migration : new PathMatchingResourcePatternResolver()
                    .getResources("classpath:db/migration/" + vendor + "/*.sql")) {
                String sql = migration.getContentAsString(StandardCharsets.UTF_8);
                Files.writeString(cible.resolve(migration.getFilename()),
                        adaptation.apply(migration.getFilename(), sql), StandardCharsets.UTF_8);
            }
            return "filesystem:" + cible.toAbsolutePath();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
