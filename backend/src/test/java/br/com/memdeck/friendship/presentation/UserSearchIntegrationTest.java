package br.com.memdeck.friendship.presentation;

import static br.com.memdeck.AuthTestSupport.csrfCookie;
import static br.com.memdeck.AuthTestSupport.registerAndLogin;
import static br.com.memdeck.AuthTestSupport.uniqueEmail;
import static br.com.memdeck.AuthTestSupport.usernameOf;
import static br.com.memdeck.AuthTestSupport.withCsrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import br.com.memdeck.AuthTestSupport.Session;
import br.com.memdeck.TestcontainersConfiguration;
import br.com.memdeck.friendship.application.SearchThrottle;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class UserSearchIntegrationTest {

	@Autowired
	MockMvc mockMvc;

	@Test
	void findsAPersonByUsernameWithoutExposingTheEmail() throws Exception {
		Session me = registerAndLogin(mockMvc, uniqueEmail());
		String otherEmail = uniqueEmail();
		registerAndLogin(mockMvc, otherEmail);

		mockMvc.perform(get("/api/users").param("username", usernameOf(otherEmail)).cookie(me.session()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].username").value(usernameOf(otherEmail)))
				.andExpect(jsonPath("$[0].relation").value("NONE"))
				.andExpect(jsonPath("$[0].email").doesNotExist());
		mockMvc.perform(get("/api/users").param("username", "nobody_here").cookie(me.session()))
				.andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void stopsAfterTooManySearches() throws Exception {
		Session me = registerAndLogin(mockMvc, uniqueEmail());
		for (int i = 0; i < SearchThrottle.MAX_SEARCHES; i++) {
			mockMvc.perform(get("/api/users").param("username", "someone").cookie(me.session()))
					.andExpect(status().isOk());
		}

		mockMvc.perform(get("/api/users").param("username", "someone").cookie(me.session()))
				.andExpect(status().isTooManyRequests()).andExpect(header().exists("Retry-After"));
	}

	@Test
	void requiresALogin() throws Exception {
		mockMvc.perform(get("/api/users").param("username", "someone")).andExpect(status().isUnauthorized());
	}

	@Test
	void refusesATakenUsernameAtRegistration() throws Exception {
		String email = uniqueEmail();
		registerAndLogin(mockMvc, email);
		String body = "{\"name\":\"Other\",\"username\":\"%s\",\"email\":\"%s\",\"password\":\"correct-horse-battery\"}"
				.formatted("@" + usernameOf(email).toUpperCase(), uniqueEmail());

		mockMvc.perform(withCsrf(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body),
				csrfCookie(mockMvc)))
				.andExpect(status().isConflict()).andExpect(jsonPath("$.reason").value("USERNAME_TAKEN"));
	}
}
