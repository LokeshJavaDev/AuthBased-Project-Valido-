package io.service.valido.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"io.service.valido"})
public class ValidoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ValidoApplication.class, args);
    }
}