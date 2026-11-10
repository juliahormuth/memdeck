package br.com.memdeck.deck;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import br.com.memdeck.TestcontainersConfiguration;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class DeckIntegrationTest {

	@Autowired
	MockMvc mockMvc;

	@Test
	void createsAndRetrievesDeckFromDatabase() throws Exception {
		String location = mockMvc.perform(post("/api/decks").with(user("tester")).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name":"  Basic English  ","description":"Vocabulary","visibility":"PUBLIC"}
						"""))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andReturn().getResponse().getHeader("Location");

		mockMvc.perform(get(location).with(user("tester")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Basic English"))
				.andExpect(jsonPath("$.visibility").value("PUBLIC"));
	}

	@Test
	void returns404ForUnknownDeck() throws Exception {
		mockMvc.perform(get("/api/decks/" + UUID.randomUUID()).with(user("tester")))
				.andExpect(status().isNotFound());
	}
}
