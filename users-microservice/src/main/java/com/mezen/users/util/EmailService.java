package com.mezen.users.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService implements EmailSender {

	private final JavaMailSender mailSender;

	@Value("${spring.mail.username:no-reply@localhost}")
	private String from;

	@Override
	public void sendEmail(String toEmail, String body) {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
			helper.setTo(toEmail);
			helper.setSubject("Confirmation de votre compte");
			helper.setFrom(from == null || from.isBlank() ? "no-reply@localhost" : from);
			helper.setText(body, true);
			mailSender.send(message);
		} catch (MessagingException | MailException e) {
			// L'inscription ne doit pas échouer si le serveur SMTP est indisponible.
			// Le code reste enregistré en base pour permettre la validation.
			log.warn("Échec de l'envoi du mail à {} : {}", toEmail, e.getMessage());
		}
	}
}
