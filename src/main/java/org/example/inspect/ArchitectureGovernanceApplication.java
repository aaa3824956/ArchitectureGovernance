package org.example.inspect;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("org.example.inspect.repository")
public class ArchitectureGovernanceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArchitectureGovernanceApplication.class, args);
    }

}
