package com.example.saas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SaasApplication {
    /** Point d'entrée ; toutes les couches sont découvertes sous com.example.saas. */
    public static void main(String[] args) {
        SpringApplication.run(SaasApplication.class, args);
    }
}
