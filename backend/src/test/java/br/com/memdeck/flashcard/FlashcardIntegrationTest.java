package br.com.memdeck.flashcard;

import static br.com.memdeck.AuthTestSupport.csrfCookie;
import static br.com.memdeck.AuthTestSupport.registerAndLogin;
import static br.com.memdeck.AuthTestSupport.uniqueEmail;
import static br.com.memdeck.AuthTestSupport.withCsrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import org.springframework.test.web.servlet.MockMvc;

import br.com.memdeck.AuthTestSupport.Session;
import br.com.memdeck.TestcontainersConfiguration;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class FlashcardIntegrationTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JdbcTemplate jdbc;

	private String createDeck(Session session, String visibility) throws Exception {
		String location = mockMvc.perform(withCsrf(post("/api/decks").cookie(session.session())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Deck\",\"visibility\":\"%s\"}".formatted(visibility)), session.csrf()))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getHeader("Location");
		return location.substring(location.lastIndexOf('/') + 1);
	}

	private org.springframework.test.web.servlet.ResultActions addFlashcard(Session session, String deckId,
			String body) throws Exception {
		return mockMvc.perform(withCsrf(post("/api/decks/" + deckId + "/flashcards").cookie(session.session())
				.contentType(MediaType.APPLICATION_JSON).content(body), session.csrf()));
	}

	private int countFor(String deckId) {
		return jdbc.queryForObject("SELECT count(*) FROM flashcards WHERE deck_id = ?::uuid", Integer.class, deckId);
	}

	@Test
	void theOwnerSavesAFlashcardInTheDatabase() throws Exception {
		Session owner = registerAndLogin(mockMvc, uniqueEmail());
		String deckId = createDeck(owner, "PRIVATE");

		addFlashcard(owner, deckId, "{\"front\":\"  Femur \",\"back\":\"Thigh bone\"}")
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.deckId").value(deckId))
				.andExpect(jsonPath("$.front").value("Femur"));

		org.assertj.core.api.Assertions.assertThat(countFor(deckId)).isEqualTo(1);
	}

	@Test
	void acceptsTheBackUpToTenThousandCharacters() throws Exception {
		Session owner = registerAndLogin(mockMvc, uniqueEmail());
		String deckId = createDeck(owner, "PRIVATE");

		addFlashcard(owner, deckId, "{\"front\":\"x\",\"back\":\"%s\"}".formatted("a".repeat(10000)))
				.andExpect(status().isCreated());
	}

	@Test
	void aStrangerGets404ForAPrivateDeckAnd403ForAPublicOneAndNothingIsSaved() throws Exception {
		Session owner = registerAndLogin(mockMvc, uniqueEmail());
		Session stranger = registerAndLogin(mockMvc, uniqueEmail());
		String privateDeck = createDeck(owner, "PRIVATE");
		String publicDeck = createDeck(owner, "PUBLIC");
		String body = "{\"front\":\"x\",\"back\":\"y\"}";

		addFlashcard(stranger, privateDeck, body).andExpect(status().isNotFound());
		addFlashcard(stranger, publicDeck, body).andExpect(status().isForbidden());

		org.assertj.core.api.Assertions.assertThat(countFor(privateDeck) + countFor(publicDeck)).isZero();
	}

	@Test
	void returns404ForAnUnknownDeck() throws Exception {
		Session owner = registerAndLogin(mockMvc, uniqueEmail());

		addFlashcard(owner, UUID.randomUUID().toString(), "{\"front\":\"x\",\"back\":\"y\"}")
				.andExpect(status().isNotFound());
	}

	@Test
	void listsTheFlashcardsNewestFirstForTheOwnerAndForAnyoneOnAPublicDeckOnly() throws Exception {
		Session owner = registerAndLogin(mockMvc, uniqueEmail());
		Session stranger = registerAndLogin(mockMvc, uniqueEmail());
		String privateDeck = createDeck(owner, "PRIVATE");
		String publicDeck = createDeck(owner, "PUBLIC");
		addFlashcard(owner, privateDeck, "{\"front\":\"First\",\"back\":\"1\"}").andExpect(status().isCreated());
		Thread.sleep(20);
		addFlashcard(owner, privateDeck, "{\"front\":\"Second\",\"back\":\"2\"}").andExpect(status().isCreated());
		addFlashcard(owner, publicDeck, "{\"front\":\"Open\",\"back\":\"3\"}").andExpect(status().isCreated());

		mockMvc.perform(get("/api/decks/" + privateDeck + "/flashcards").cookie(owner.session()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].front").value("Second"))
				.andExpect(jsonPath("$[1].front").value("First"));
		mockMvc.perform(get("/api/decks/" + privateDeck + "/flashcards").cookie(stranger.session()))
				.andExpect(status().isNotFound());
		mockMvc.perform(get("/api/decks/" + publicDeck + "/flashcards").cookie(stranger.session()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].front").value("Open"));
	}

	@Test
	void listingRequiresALoggedUser() throws Exception {
		mockMvc.perform(get("/api/decks/" + UUID.randomUUID() + "/flashcards")).andExpect(status().isUnauthorized());
	}

	@Test
	void requiresALoggedUser() throws Exception {
		mockMvc.perform(withCsrf(post("/api/decks/" + UUID.randomUUID() + "/flashcards")
				.contentType(MediaType.APPLICATION_JSON).content("{\"front\":\"x\",\"back\":\"y\"}"),
				csrfCookie(mockMvc)))
				.andExpect(status().isUnauthorized());
	}

	private String firstFlashcardId(Session session, String deckId) throws Exception {
		String json = mockMvc.perform(get("/api/decks/" + deckId + "/flashcards").cookie(session.session()))
				.andReturn().getResponse().getContentAsString();
		return com.jayway.jsonpath.JsonPath.read(json, "$[0].id");
	}

	@Test
	void theOwnerEditsAFlashcardAndOthersCannot() throws Exception {
		Session owner = registerAndLogin(mockMvc, uniqueEmail());
		Session stranger = registerAndLogin(mockMvc, uniqueEmail());
		String publicDeck = createDeck(owner, "PUBLIC");
		addFlashcard(owner, publicDeck, "{\"front\":\"Old\",\"back\":\"old\"}").andExpect(status().isCreated());
		String id = firstFlashcardId(owner, publicDeck);
		String url = "/api/decks/" + publicDeck + "/flashcards/" + id;
		String body = "{\"front\":\" New \",\"back\":\"new\"}";

		mockMvc.perform(withCsrf(put(url).cookie(stranger.session()).contentType(MediaType.APPLICATION_JSON).content(body),
				stranger.csrf())).andExpect(status().isForbidden());
		mockMvc.perform(withCsrf(put(url).cookie(owner.session()).contentType(MediaType.APPLICATION_JSON).content(body),
				owner.csrf()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id))
				.andExpect(jsonPath("$.front").value("New"));
		mockMvc.perform(get("/api/decks/" + publicDeck + "/flashcards").cookie(stranger.session()))
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].back").value("new"));
	}

	@Test
	void theOwnerDeletesAFlashcardOnlyThroughItsOwnDeck() throws Exception {
		Session owner = registerAndLogin(mockMvc, uniqueEmail());
		String deck = createDeck(owner, "PRIVATE");
		String otherDeck = createDeck(owner, "PRIVATE");
		addFlashcard(owner, deck, "{\"front\":\"x\",\"back\":\"y\"}").andExpect(status().isCreated());
		String id = firstFlashcardId(owner, deck);

		mockMvc.perform(withCsrf(delete("/api/decks/" + otherDeck + "/flashcards/" + id).cookie(owner.session()), owner.csrf()))
				.andExpect(status().isNotFound());
		org.assertj.core.api.Assertions.assertThat(countFor(deck)).isEqualTo(1);
		mockMvc.perform(withCsrf(delete("/api/decks/" + deck + "/flashcards/" + id).cookie(owner.session()), owner.csrf()))
				.andExpect(status().isNoContent());
		org.assertj.core.api.Assertions.assertThat(countFor(deck)).isZero();
	}
}
