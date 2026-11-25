package br.com.memdeck.user.infrastructure;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import br.com.memdeck.user.application.PasswordResetMailer;
import br.com.memdeck.user.domain.PasswordReset;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Component
class SmtpPasswordResetMailer implements PasswordResetMailer {

	private static final Logger LOG = LoggerFactory.getLogger(SmtpPasswordResetMailer.class);

	private final JavaMailSender sender;
	private final TaskExecutor executor;
	private final String username;
	private final String from;
	private final String htmlTemplate = readHtmlTemplate();
	// attached to the message: mail clients block or strip remote and data: images
	private final Resource logo = new ClassPathResource("mail/logo.png");

	SmtpPasswordResetMailer(JavaMailSender sender, @Qualifier("applicationTaskExecutor") TaskExecutor executor,
			@Value("${spring.mail.username:}") String username, @Value("${memdeck.mail.from:}") String from) {
		this.sender = sender;
		this.executor = executor;
		this.username = username;
		this.from = from.isBlank() ? username : from;
	}

	// sent in the background: waiting for the SMTP server would reveal by timing which emails have an account
	@Override
	public void sendCode(String email, String code) {
		if (username.isBlank()) {
			LOG.warn("E-mail is not configured (MAIL_USERNAME): the password reset code was not sent");
			return;
		}
		MimeMessage message = message(email, code);
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			deliver(message);
			return;
		}
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				deliver(message);
			}
		});
	}

	private MimeMessage message(String email, String code) {
		MimeMessage message = sender.createMimeMessage();
		String minutes = String.valueOf(PasswordReset.CODE_LIFETIME.toMinutes());
		try {
			MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
			helper.setFrom(from, "Memdeck");
			helper.setTo(email);
			helper.setSubject("Seu código do Memdeck");
			helper.setText(plainText(code, minutes),
					htmlTemplate.replace("{{code}}", code).replace("{{minutes}}", minutes));
			helper.addInline("memdeck-logo", logo);
		}
		catch (MessagingException | IOException exception) {
			throw new IllegalStateException("Could not compose the password reset e-mail", exception);
		}
		return message;
	}

	private static String plainText(String code, String minutes) {
		return """
				Olá!

				Seu código para criar uma nova senha no Memdeck é: %s

				Ele vale por %s minutos. Se você não pediu esse código, ignore este e-mail: sua senha continua a mesma.
				""".formatted(code, minutes);
	}

	private static String readHtmlTemplate() {
		try {
			return new ClassPathResource("mail/password-reset.html").getContentAsString(StandardCharsets.UTF_8);
		}
		catch (IOException exception) {
			throw new UncheckedIOException(exception);
		}
	}

	private void deliver(MimeMessage message) {
		executor.execute(() -> {
			try {
				sender.send(message);
			}
			catch (MailException exception) {
				// the message of a mail exception can carry the recipient address
				LOG.error("Could not send the password reset e-mail: {}", exception.getClass().getSimpleName());
			}
		});
	}
}
