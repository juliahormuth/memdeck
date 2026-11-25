package br.com.memdeck.user.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

class SmtpPasswordResetMailerTest {

	private final JavaMailSender sender = mock(JavaMailSender.class);

	private SmtpPasswordResetMailer mailer(String username) {
		return new SmtpPasswordResetMailer(sender, new SyncTaskExecutor(), username, "");
	}

	@BeforeEach
	void createEmptyMessages() {
		when(sender.createMimeMessage()).thenAnswer(invocation -> new MimeMessage((Session) null));
	}

	private static String raw(MimeMessage message) throws Exception {
		message.saveChanges();
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		message.writeTo(bytes);
		return bytes.toString(StandardCharsets.UTF_8);
	}

	@Test
	void sendsTheCodeFromTheConfiguredAccount() throws Exception {
		mailer("memdeck@example.com").sendCode("julia@example.com", "123456");

		ArgumentCaptor<MimeMessage> message = ArgumentCaptor.forClass(MimeMessage.class);
		verify(sender).send(message.capture());
		assertThat(message.getValue().getFrom()[0].toString()).isEqualTo("Memdeck <memdeck@example.com>");
		assertThat(message.getValue().getAllRecipients()[0].toString()).isEqualTo("julia@example.com");
		assertThat(raw(message.getValue()))
				.contains("text/plain", "text/html", "123456", "Content-ID: <memdeck-logo>")
				.doesNotContain("{{");
	}

	@Test
	void sendsNothingWhenEmailIsNotConfigured() {
		mailer("").sendCode("julia@example.com", "123456");

		verify(sender, never()).send(any(MimeMessage.class));
	}

	@Test
	void doesNotFailTheRequestWhenTheServerRefusesTheMessage() {
		doThrow(new MailSendException("refused")).when(sender).send(any(MimeMessage.class));

		assertThatCode(() -> mailer("memdeck@example.com").sendCode("julia@example.com", "123456"))
				.doesNotThrowAnyException();
	}
}
