package de.witchcafe.app;

import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.spring.annotation.EnableVaadin;

@SpringBootApplication
@StyleSheet(Lumo.STYLESHEET) // Use Aura.STYLESHEET to use Aura instead
@StyleSheet(Lumo.UTILITY_STYLESHEET)
@StyleSheet("styles.css") // Your custom styles
// "de.witchcafe" deckt die eigene App (de.witchcafe.app) und witch-auth (de.witchcafe.auth) ab
@ComponentScan(basePackages = "de.witchcafe")
@EnableVaadin("de.witchcafe")
@EnableJpaRepositories("de.witchcafe") // Findet die Repository-Interfaces
@EntityScan("de.witchcafe") // Findet die @Entity Klassen
public class Application implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
