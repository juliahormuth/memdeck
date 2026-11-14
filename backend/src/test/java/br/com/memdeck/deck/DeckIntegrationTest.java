package br.com.memdeck.deck;

import static br.com.memdeck.AuthTestSupport.registerAndLogin;
import static br.com.memdeck.AuthTestSupport.uniqueEmail;
import static br.com.memdeck.AuthTestSupport.withCsrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import br.com.memdeck.AuthTestSupport.Session;
import br.com.memdeck.TestcontainersConfiguration;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class DeckIntegrationTest {

	@Autowired
	MockMvc mockMvc;

	private String createDeck(Session session, String name, String visibility) throws Exception {
		return mockMvc.perform(withCsrf(post("/api/decks").cookie(session.session())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"%s\",\"visibility\":\"%s\"}".formatted(name, visibility)), session.csrf()))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andReturn().getResponse().getHeader("Location");
	}

	@Test
	void createsAndRetrievesDeckFromDatabase() throws Exception {
		Session session = registerAndLogin(mockMvc, uniqueEmail());

		String location = mockMvc.perform(withCsrf(post("/api/decks").cookie(session.session())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name":"  Basic English  ","description":"Vocabulary","visibility":"PUBLIC"}
						"""), session.csrf()))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andReturn().getResponse().getHeader("Location");

		mockMvc.perform(get(location).cookie(session.session()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Basic English"))
				.andExpect(jsonPath("$.visibility").value("PUBLIC"));
	}

	@Test
	void returns404ForUnknownDeck() throws Exception {
		Session session = registerAndLogin(mockMvc, uniqueEmail());

		mockMvc.perform(get("/api/decks/" + UUID.randomUUID()).cookie(session.session()))
				.andExpect(status().isNotFound());
	}

	@Test
	void aPrivateDeckIsHiddenFromOtherUsersButAPublicOneIsNot() throws Exception {
		Session owner = registerAndLogin(mockMvc, uniqueEmail());
		Session stranger = registerAndLogin(mockMvc, uniqueEmail());
		String privateDeck = createDeck(owner, "Secret", "PRIVATE");
		String publicDeck = createDeck(owner, "Open", "PUBLIC");

		mockMvc.perform(get(privateDeck).cookie(owner.session())).andExpect(status().isOk());
		mockMvc.perform(get(privateDeck).cookie(stranger.session())).andExpect(status().isNotFound());
		mockMvc.perform(get(publicDeck).cookie(stranger.session()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Open"));
	}

	@Test
	void listsOnlyMyDecksNewestFirst() throws Exception {
		Session me = registerAndLogin(mockMvc, uniqueEmail());
		Session other = registerAndLogin(mockMvc, uniqueEmail());
		createDeck(me, "First", "PRIVATE");
		Thread.sleep(20);
		createDeck(me, "Second", "PUBLIC");
		createDeck(other, "Not mine", "PUBLIC");

		mockMvc.perform(get("/api/decks").cookie(me.session()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].name").value("Second"))
				.andExpect(jsonPath("$[1].name").value("First"));
		mockMvc.perform(get("/api/decks").cookie(other.session()))
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].name").value("Not mine"));
	}

	@Test
	void listingRequiresALoggedUser() throws Exception {
		mockMvc.perform(get("/api/decks")).andExpect(status().isUnauthorized());
	}
}
