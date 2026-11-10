package br.com.memdeck.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import br.com.memdeck.TestcontainersConfiguration;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class UserIntegrationTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JdbcTemplate jdbc;

	@Test
	void registersUserStoringAnArgon2HashAndRejectsDuplicateEmail() throws Exception {
		String body = """
				{"name":"Integration User","email":"Integration@Example.com","password":"correct-horse-battery"}
				""";

		mockMvc.perform(post("/api/users").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isCreated());

		String stored = jdbc.queryForObject("select password_hash from users where email = ?", String.class,
				"integration@example.com");
		assertThat(stored).startsWith("$argon2id$").doesNotContain("correct-horse-battery");

		mockMvc.perform(post("/api/users").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body.replace("Integration@", "INTEGRATION@")))
				.andExpect(status().isConflict());
	}
}
