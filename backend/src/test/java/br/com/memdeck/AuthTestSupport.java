package br.com.memdeck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import jakarta.servlet.http.Cookie;

/**
 * Integration tests share one cached Spring context, so they must not use spring-security-test's csrf() or user():
 * those swap the CSRF token repository and the security context repository of the context for every later test.
 * This support class follows the real cookie flow instead.
 */
public final class AuthTestSupport {

	public static final String PASSWORD = "correct-horse-battery";

	public record Session(Cookie csrf, Cookie session) {
	}

	private AuthTestSupport() {
	}

	public static String uniqueEmail() {
		return "it-" + UUID.randomUUID() + "@example.com";
	}

	public static Cookie csrfCookie(MockMvc mockMvc) throws Exception {
		Cookie cookie = mockMvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized())
				.andReturn().getResponse().getCookie("XSRF-TOKEN");
		assertThat(cookie).as("the API must hand out a CSRF cookie even on a 401").isNotNull();
		return cookie;
	}

	public static MockHttpServletRequestBuilder withCsrf(MockHttpServletRequestBuilder request, Cookie csrf) {
		return request.cookie(csrf).header("X-XSRF-TOKEN", csrf.getValue());
	}

	public static Session registerAndLogin(MockMvc mockMvc, String email) throws Exception {
		Cookie csrf = csrfCookie(mockMvc);
		String body = "{\"name\":\"Test User\",\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, PASSWORD);
		mockMvc.perform(withCsrf(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body), csrf))
				.andExpect(status().isCreated());
		MockHttpServletResponse login = mockMvc
				.perform(withCsrf(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body), csrf))
				.andExpect(status().isOk()).andReturn().getResponse();
		Cookie session = login.getCookie("SESSION");
		assertThat(session).as("login must start a session").isNotNull();
		return new Session(csrf, session);
	}
}
