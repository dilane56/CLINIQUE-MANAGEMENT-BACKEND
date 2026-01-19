package org.kfokam48.cliniquemanagementbackend.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Profile("render")
public class RenderDatabaseUrlConverter {

    @EventListener(ApplicationReadyEvent.class)
    public void convertDatabaseUrl() {
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl != null && !databaseUrl.startsWith("jdbc:")) {
            String jdbcUrl = "jdbc:" + databaseUrl;
            System.setProperty("spring.datasource.url", jdbcUrl);
            System.out.println("Converted DATABASE_URL to JDBC format: " + jdbcUrl);
        }
    }
}