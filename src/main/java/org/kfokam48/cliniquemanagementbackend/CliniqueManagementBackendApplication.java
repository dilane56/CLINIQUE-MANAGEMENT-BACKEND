package org.kfokam48.cliniquemanagementbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CliniqueManagementBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CliniqueManagementBackendApplication.class, args);
    }

}
