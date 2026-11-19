package br.com.memdeck.deck;

import static br.com.memdeck.AuthTestSupport.csrfCookie;
import static br.com.memdeck.AuthTestSupport.registerAndLogin;
import static br.com.memdeck.AuthTestSupport.uniqueEmail;
import static br.com.memdeck.AuthTestSupport.withCsrf;
import static org.assertj.core.api.Assertions.assertThat;
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
import org.springframework.test.web.servlet.ResultActions;

import br.com.memdeck.AuthTestSupport.Session;
import br.com.memdeck.TestcontainersConfiguration;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ExploreDecksIntegrationTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JdbcTemplate jdbc;

	private String createDeck(Session session, String name, String description, String visibility) throws Exception {
		String body = description == null
				? "{\"name\":\"%s\",\"visibility\":\"%s\"}".formatted(name, visibility)
				: "{\"name\":\"%s\",\"description\":\"%s\",\"visibility\":\"%s\"}".formatted(name, description, visibility);
		String location = mockMvc.perform(withCsrf(post("/api/decks").cookie(session.session())
				.contentType(MediaType.APPLICATION_JSON).content(body), session.csrf()))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getHeader("Location");
		return location.substring(location.lastIndexOf('/') + 1);
	}

	private ResultActions search(Session session, String term) throws Exception {
		return mockMvc.perform(get("/api/decks/public").param("search", term).cookie(session.session()));
	}

	private ResultActions join(Session session, String deckId) throws Exception {
		return mockMvc.perform(withCsrf(post("/api/decks/" + deckId + "/members").cookie(session.session()),
				session.csrf()));
	}

	private String unique(String prefix) {
		return prefix + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
	}

	@Test
	void findsPublicDecksOfOthersByNameOrDescriptionAndNeverPrivateOnesOrMyOwn() throws Exception {
		Session owner = registerAndLogin(mockMvc, uniqueEmail());
		Session seeker = registerAndLogin(mockMvc, uniqueEmail());
		String term = unique("Zq");
		createDeck(owner, term + " by name", null, "PUBLIC");
		createDeck(owner, "By description", "talks about " + term.toUpperCase(), "PUBLIC");
		createDeck(owner, term + " private", null, "PRIVATE");
		createDeck(seeker, term + " mine", null, "PUBLIC");

		search(seeker, term).andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].name").value("By description"))
				.andExpect(jsonPath("$[1].name").value(term + " by name"))
				.andExpect(jsonPath("$[0].ownedByMe").value(false))
				.andExpect(jsonPath("$[0].role").doesNotExist());
	}

	@Test
	void percentAndUnderscoreAreSearchedLiterally() throws Exception {
		Session owner = registerAndLogin(mockMvc, uniqueEmail());
		Session seeker = registerAndLogin(mockMvc, uniqueEmail());
		String tag = unique("Lit");
		createDeck(owner, tag + " 100%", null, "PUBLIC");
		createDeck(owner, tag + " 1000", null, "PUBLIC");
		createDeck(owner, tag + " snake_case", null, "PUBLIC");
		createDeck(owner, tag + " snakeXcase", null, "PUBLIC");

		search(seeker, tag + " 100%").andExpect(jsonPath("$.length()").value(1));
		search(seeker, "snake_case").andExpect(status().isOk());
		search(seeker, tag + " snake_case").andExpect(jsonPath("$.length()").value(1));
	}

	@Test
	void joiningAPublicDeckAddsItToMyDecksWithTheReaderRoleAndMarksTheSearch() throws Exception {
		Session owner = registerAndLogin(mockMvc, uniqueEmail());
		Session reader = registerAndLogin(mockMvc, uniqueEmail());
		String name = unique("Join");
		String deckId = createDeck(owner, name, null, "PUBLIC");

		join(reader, deckId).andExpect(status().isCreated())
				.andExpect(jsonPath("$.role").value("READER"))
				.andExpect(jsonPath("$.ownedByMe").value(false));

		mockMvc.perform(get("/api/decks").cookie(reader.session()))
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].id").value(deckId))
				.andExpect(jsonPath("$[0].role").value("READER"));
		mockMvc.perform(get("/api/decks/" + deckId).cookie(reader.session()))
				.andExpect(jsonPath("$.role").value("READER"));
		search(reader, name).andExpect(jsonPath("$[0].role").value("READER"));
		mockMvc.perform(get("/api/decks").cookie(owner.session()))
				.andExpect(jsonPath("$[0].role").value("OWNER"));
		assertThat(jdbc.queryForObject("SELECT count(*) FROM deck_members WHERE deck_id = ?::uuid", Integer.class,
				deckId)).isEqualTo(1);
	}

	@Test
	void joiningTwiceOrJoiningMyOwnDeckIs409() throws Exception {
		Session owner = registerAndLogin(mockMvc, uniqueEmail());
		Session reader = registerAndLogin(mockMvc, uniqueEmail());
		String deckId = createDeck(owner, unique("Twice"), null, "PUBLIC");
		join(reader, deckId).andExpect(status().isCreated());

		join(reader, deckId).andExpect(status().isConflict());
		join(owner, deckId).andExpect(status().isConflict());
	}

	@Test
	void joiningAPrivateOrUnknownDeckIs404() throws Exception {
		Session owner = registerAndLogin(mockMvc, uniqueEmail());
		Session stranger = registerAndLogin(mockMvc, uniqueEmail());
		String privateDeck = createDeck(owner, unique("Priv"), null, "PRIVATE");

		join(stranger, privateDeck).andExpect(status().isNotFound());
		join(stranger, UUID.randomUUID().toString()).andExpect(status().isNotFound());
		assertThat(jdbc.queryForObject("SELECT count(*) FROM deck_members WHERE deck_id = ?::uuid", Integer.class,
				privateDeck)).isZero();
	}

	@Test
	void aReaderCanStudyTheDeckButNotChangeIt() throws Exception {
		Session owner = registerAndLogin(mockMvc, uniqueEmail());
		Session reader = registerAndLogin(mockMvc, uniqueEmail());
		String deckId = createDeck(owner, unique("Read"), null, "PUBLIC");
		join(reader, deckId).andExpect(status().isCreated());

		mockMvc.perform(get("/api/decks/" + deckId + "/study").cookie(reader.session())).andExpect(status().isOk());
		mockMvc.perform(withCsrf(put("/api/decks/" + deckId).cookie(reader.session())
				.contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"x\",\"visibility\":\"PUBLIC\"}"),
				reader.csrf())).andExpect(status().isForbidden());
		mockMvc.perform(withCsrf(delete("/api/decks/" + deckId).cookie(reader.session()), reader.csrf()))
				.andExpect(status().isForbidden());
	}

	@Test
	void aJoinedDeckStaysAvailableToItsReaderAfterTheOwnerMakesItPrivate() throws Exception {
		Session owner = registerAndLogin(mockMvc, uniqueEmail());
		Session reader = registerAndLogin(mockMvc, uniqueEmail());
		String deckId = createDeck(owner, unique("Flip"), null, "PUBLIC");
		join(reader, deckId).andExpect(status().isCreated());

		changeVisibility(owner, deckId, "PRIVATE");

		mockMvc.perform(get("/api/decks").cookie(reader.session())).andExpect(jsonPath("$.length()").value(1));
		mockMvc.perform(get("/api/decks/" + deckId).cookie(reader.session()))
				.andExpect(status().isOk()).andExpect(jsonPath("$.role").value("READER"));
		mockMvc.perform(get("/api/decks/" + deckId + "/study").cookie(reader.session())).andExpect(status().isOk());
	}

	@Test
	void deletingTheDeckRemovesItsMembers() throws Exception {
		Session owner = registerAndLogin(mockMvc, uniqueEmail());
		Session reader = registerAndLogin(mockMvc, uniqueEmail());
		String deckId = createDeck(owner, unique("Gone"), null, "PUBLIC");
		join(reader, deckId).andExpect(status().isCreated());

		mockMvc.perform(withCsrf(delete("/api/decks/" + deckId).cookie(owner.session()), owner.csrf()))
				.andExpect(status().isNoContent());

		assertThat(jdbc.queryForObject("SELECT count(*) FROM deck_members WHERE deck_id = ?::uuid", Integer.class,
				deckId)).isZero();
		mockMvc.perform(get("/api/decks").cookie(reader.session())).andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void searchAndJoinRequireALoggedUser() throws Exception {
		mockMvc.perform(get("/api/decks/public")).andExpect(status().isUnauthorized());
		mockMvc.perform(withCsrf(post("/api/decks/" + UUID.randomUUID() + "/members"), csrfCookie(mockMvc)))
				.andExpect(status().isUnauthorized());
	}

	private void changeVisibility(Session owner, String deckId, String visibility) throws Exception {
		mockMvc.perform(withCsrf(put("/api/decks/" + deckId).cookie(owner.session())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Flip\",\"visibility\":\"%s\"}".formatted(visibility)), owner.csrf()))
				.andExpect(status().isOk());
	}
}
