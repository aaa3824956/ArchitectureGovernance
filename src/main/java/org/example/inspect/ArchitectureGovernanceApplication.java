package org.example.inspect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ArchitectureGovernanceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArchitectureGovernanceApplication.class, args);
    }

}
