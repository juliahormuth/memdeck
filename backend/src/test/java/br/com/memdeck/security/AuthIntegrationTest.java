package br.com.memdeck.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import br.com.memdeck.TestcontainersConfiguration;
import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AuthIntegrationTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JdbcTemplate jdbc;

	private Cookie csrfCookie() throws Exception {
		Cookie cookie = mockMvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized())
				.andReturn().getResponse().getCookie("XSRF-TOKEN");
		assertThat(cookie).as("the API must hand out a CSRF cookie even on a 401").isNotNull();
		return cookie;
	}

	private String json(String email, String password) {
		return "{\"name\":\"Auth User\",\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password);
	}

	private MockHttpServletResponse postJson(String path, String body, Cookie csrf, Cookie... extra) throws Exception {
		var request = post(path).contentType(MediaType.APPLICATION_JSON).content(body)
				.cookie(csrf).header("X-XSRF-TOKEN", csrf.getValue());
		for (Cookie cookie : extra) {
			request.cookie(cookie);
		}
		return mockMvc.perform(request).andReturn().getResponse();
	}

	@Test
	void rejectsUnauthenticatedAccessToProtectedRoutes() throws Exception {
		mockMvc.perform(get("/api/decks/" + UUID.randomUUID())).andExpect(status().isUnauthorized());
		mockMvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
	}

	@Test
	void rejectsPostsWithoutACsrfToken() throws Exception {
		mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
				.content(json("nocsrf@example.com", "correct-horse-battery"))).andExpect(status().isForbidden());
	}

	@Test
	void fullSessionLifecycle() throws Exception {
		Cookie csrf = csrfCookie();
		String email = "lifecycle@example.com";

		assertThat(postJson("/api/users", json(email, "correct-horse-battery"), csrf).getStatus()).isEqualTo(201);

		MockHttpServletResponse wrong = postJson("/api/auth/login", json(email, "wrong-password-123"), csrf);
		assertThat(wrong.getStatus()).isEqualTo(401);
		assertThat(wrong.getCookie("SESSION")).isNull();

		MockHttpServletResponse login = postJson("/api/auth/login", json(email, "correct-horse-battery"), csrf);
		assertThat(login.getStatus()).isEqualTo(200);
		Cookie session = login.getCookie("SESSION");
		assertThat(session).isNotNull();
		assertThat(session.isHttpOnly()).isTrue();
		assertThat(login.getHeader("Set-Cookie")).containsIgnoringCase("SameSite=Lax").containsIgnoringCase("; Secure");
		assertThat(jdbc.queryForObject("select count(*) from spring_session", Integer.class)).isPositive();

		mockMvc.perform(get("/api/auth/me").cookie(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value(email));
		mockMvc.perform(get("/api/decks/" + UUID.randomUUID()).cookie(session))
				.andExpect(status().isNotFound());

		MockHttpServletResponse logout = postJson("/api/auth/logout", "", csrf, session);
		assertThat(logout.getStatus()).isEqualTo(204);

		mockMvc.perform(get("/api/auth/me").cookie(session)).andExpect(status().isUnauthorized());
	}

	@Test
	void unknownEmailAndWrongPasswordLookTheSame() throws Exception {
		Cookie csrf = csrfCookie();
		postJson("/api/users", json("same@example.com", "correct-horse-battery"), csrf);

		MockHttpServletResponse unknown = postJson("/api/auth/login", json("ghost@example.com", "correct-horse-battery"), csrf);
		MockHttpServletResponse wrong = postJson("/api/auth/login", json("same@example.com", "wrong-password-123"), csrf);

		assertThat(unknown.getStatus()).isEqualTo(401);
		assertThat(wrong.getStatus()).isEqualTo(401);
		assertThat(unknown.getContentAsString()).isEqualTo(wrong.getContentAsString());
	}

	@Test
	void blocksLoginAfterRepeatedFailuresWithoutAffectingOtherAccounts() throws Exception {
		Cookie csrf = csrfCookie();
		postJson("/api/users", json("throttled@example.com", "correct-horse-battery"), csrf);
		postJson("/api/users", json("bystander@example.com", "correct-horse-battery"), csrf);

		for (int i = 0; i < 5; i++) {
			assertThat(postJson("/api/auth/login", json("throttled@example.com", "wrong-password-123"), csrf).getStatus())
					.isEqualTo(401);
		}

		MockHttpServletResponse blocked = postJson("/api/auth/login", json("throttled@example.com", "correct-horse-battery"), csrf);
		assertThat(blocked.getStatus()).isEqualTo(429);
		assertThat(Long.parseLong(blocked.getHeader("Retry-After"))).isPositive();
		assertThat(blocked.getCookie("SESSION")).isNull();

		assertThat(postJson("/api/auth/login", json("bystander@example.com", "correct-horse-battery"), csrf).getStatus())
				.isEqualTo(200);
	}
}
