package de.axelspringer.publishing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories("de.axelspringer.publishing.persistence")
@ComponentScan("de.axelspringer.publishing")
@EntityScan("de.axelspringer.publishing.model")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
