package com.mzn.Stadium.security;

/**
 * Paramètres partagés avec le microservice users.
 *
 * Le JWT est signé par users-microservice (port 8081) avec SECRET et vérifié
 * ici par le service Stadium (port 8080). Les deux valeurs DOIVENT rester
 * identiques, sinon tous les tokens seront rejetés.
 */
public interface SecParams {
	long EXP_TIME = 10L * 24 * 60 * 60 * 1000; // 10 jours (informatif)
	String SECRET = "Mazen@123";
	String PREFIX = "Bearer ";
}
