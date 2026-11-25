package com.mezen.users.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService implements EmailSender {

	private final JavaMailSender mailSender;

	@Value("${spring.mail.username:}")
	private String username;

	@Value("${spring.mail.password:}")
	private String password;

	/**
	 * Les fichiers .properties ne retirent pas les guillemets : une valeur
	 * écrite 'user@gmail.com' est utilisée avec les apostrophes, ce qui casse
	 * l'adresse "From" et l'authentification SMTP. On normalise donc les
	 * identifiants en mémoire pour rester tolérant aux deux écritures.
	 */
	@PostConstruct
	void normalizeMailCredentials() {
		boolean quoted = hasSurroundingQuotes(username) || hasSurroundingQuotes(password);
		if (quoted) {
			log.warn("spring.mail.username/password contiennent des guillemets : "
					+ "ils sont retirés automatiquement (les fichiers .properties ne le font pas).");
		}

		if (mailSender instanceof JavaMailSenderImpl impl) {
			impl.setUsername(unquote(username));
			impl.setPassword(unquote(password));
		}
	}

	@Override
	public void sendEmail(String toEmail, String body) {
		String from = unquote(username);
		String secret = unquote(password);

		// Sans identifiants SMTP, l'envoi ne peut pas aboutir. On le signale
		// clairement au lieu d'échouer silencieusement (l'inscription, elle,
		// reste valide : le code est déjà enregistré en base).
		if (isBlank(from) || isBlank(secret)) {
			log.error("Email non envoyé à {} : SMTP non configuré. "
					+ "Renseignez spring.mail.username et spring.mail.password "
					+ "(mot de passe d'application Gmail) puis redémarrez users-microservice.",
					toEmail);
			// Mode développement : on affiche le contenu (dont le code) dans la
			// console pour pouvoir tester la validation sans serveur SMTP.
			log.warn("Contenu de l'email (mode développement) pour {} : {}", toEmail, body);
			return;
		}

		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
			helper.setTo(toEmail);
			helper.setSubject("Confirmation de votre compte");
			helper.setFrom(from);
			helper.setText(body, true);
			mailSender.send(message);
			log.info("Email de confirmation envoyé à {}", toEmail);
		} catch (MessagingException | MailException e) {
			// L'inscription ne doit pas échouer si le serveur SMTP est indisponible.
			// Le code reste enregistré en base pour permettre la validation.
			log.error("Échec de l'envoi du mail à {} : {}", toEmail, e.getMessage(), e);
		}
	}

	private boolean hasSurroundingQuotes(String value) {
		if (value == null)
			return false;
		String v = value.trim();
		if (v.length() < 2)
			return false;
		char first = v.charAt(0);
		char last = v.charAt(v.length() - 1);
		return (first == '\'' && last == '\'') || (first == '"' && last == '"');
	}

	private String unquote(String value) {
		if (value == null)
			return "";
		String v = value.trim();
		if (hasSurroundingQuotes(v))
			v = v.substring(1, v.length() - 1).trim();
		return v;
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}
}
