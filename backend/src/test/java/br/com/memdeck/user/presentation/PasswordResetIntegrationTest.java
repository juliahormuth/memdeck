package br.com.memdeck.user.presentation;

import static br.com.memdeck.AuthTestSupport.PASSWORD;
import static br.com.memdeck.AuthTestSupport.csrfCookie;
import static br.com.memdeck.AuthTestSupport.registerAndLogin;
import static br.com.memdeck.AuthTestSupport.uniqueEmail;
import static br.com.memdeck.AuthTestSupport.withCsrf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import br.com.memdeck.AuthTestSupport.Session;
import br.com.memdeck.TestcontainersConfiguration;
import br.com.memdeck.user.application.PasswordResetMailer;
import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class PasswordResetIntegrationTest {

	private static final String NEW_PASSWORD = "brand-new-password";

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JdbcTemplate jdbc;

	@MockitoBean
	PasswordResetMailer mailer;

	private ResultActions postJson(String path, String body, Cookie csrf) throws Exception {
		return mockMvc.perform(withCsrf(post(path).contentType(MediaType.APPLICATION_JSON).content(body), csrf));
	}

	private ResultActions requestCode(String email, Cookie csrf) throws Exception {
		return postJson("/api/auth/password-reset", "{\"email\":\"%s\"}".formatted(email), csrf);
	}

	private ResultActions confirm(String email, String code, Cookie csrf) throws Exception {
		return postJson("/api/auth/password-reset/confirm",
				"{\"email\":\"%s\",\"code\":\"%s\",\"newPassword\":\"%s\"}".formatted(email, code, NEW_PASSWORD), csrf);
	}

	private ResultActions login(String email, String password, Cookie csrf) throws Exception {
		return postJson("/api/auth/login", "{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password), csrf);
	}

	private String sentCode(String email) {
		ArgumentCaptor<String> code = ArgumentCaptor.forClass(String.class);
		verify(mailer).sendCode(eq(email), code.capture());
		return code.getValue();
	}

	private int failedAttempts(String email) {
		return jdbc.queryForObject("select failed_attempts from password_resets r join users u on u.id = r.user_id "
				+ "where u.email = ?", Integer.class, email);
	}

	@Test
	void recoversTheAccountAndEndsTheOpenSessions() throws Exception {
		String email = uniqueEmail();
		Session open = registerAndLogin(mockMvc, email);
		Cookie csrf = csrfCookie(mockMvc);

		requestCode(email, csrf).andExpect(status().isAccepted()).andExpect(content().string(""));
		String code = sentCode(email);
		String wrongCode = code.equals("000000") ? "000001" : "000000";

		confirm(email, wrongCode, csrf).andExpect(status().isBadRequest());
		assertThat(failedAttempts(email)).as("a wrong guess must be committed").isOne();

		confirm(email, code, csrf).andExpect(status().isNoContent());

		mockMvc.perform(get("/api/auth/me").cookie(open.session())).andExpect(status().isUnauthorized());
		login(email, PASSWORD, csrf).andExpect(status().isUnauthorized());
		login(email, NEW_PASSWORD, csrf).andExpect(status().isOk());
		confirm(email, code, csrf).andExpect(status().isBadRequest());
	}

	@Test
	void answersAnUnknownEmailWithTheSameEmptyResponse() throws Exception {
		requestCode(uniqueEmail(), csrfCookie(mockMvc))
				.andExpect(status().isAccepted()).andExpect(content().string(""));

		verifyNoInteractions(mailer);
	}

	@Test
	void validatesTheRequestBodies() throws Exception {
		Cookie csrf = csrfCookie(mockMvc);

		requestCode("not-an-email", csrf).andExpect(status().isBadRequest());
		confirm(uniqueEmail(), "12ab", csrf).andExpect(status().isBadRequest());
		verifyNoInteractions(mailer);
	}

	@Test
	void requiresACsrfToken() throws Exception {
		mockMvc.perform(post("/api/auth/password-reset").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"%s\"}".formatted(uniqueEmail()))).andExpect(status().isForbidden());
	}
}
