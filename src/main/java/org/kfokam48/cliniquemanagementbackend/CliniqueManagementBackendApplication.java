package org.kfokam48.cliniquemanagementbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class CliniqueManagementBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CliniqueManagementBackendApplication.class, args);
    }

}
