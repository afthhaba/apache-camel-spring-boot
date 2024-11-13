package fr.eido.soa.adresses;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = "fr.eido.soa.adresses.model")
public class AdresseManagementStartApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdresseManagementStartApplication.class, args);
    }
}
