package com.mzn.Stadium;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;

import com.mzn.Stadium.entities.Pays;
import com.mzn.Stadium.entities.Stade;

// Pas d'utilisateur "in-memory" : l'authentification est entièrement gérée
// par le filtre JWT (tokens émis par users-microservice).
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class StadiumApplication implements CommandLineRunner {

    @Autowired
    private RepositoryRestConfiguration repositoryRestConfiguration;

    public static void main(String[] args) {
        SpringApplication.run(StadiumApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        repositoryRestConfiguration.exposeIdsFor(Stade.class);
        repositoryRestConfiguration.exposeIdsFor(Pays.class);
    }
}