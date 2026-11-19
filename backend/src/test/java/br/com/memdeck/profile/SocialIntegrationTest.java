package br.com.memdeck.profile;

import static br.com.memdeck.AuthTestSupport.csrfCookie;
import static br.com.memdeck.AuthTestSupport.registerAndLogin;
import static br.com.memdeck.AuthTestSupport.uniqueEmail;
import static br.com.memdeck.AuthTestSupport.withCsrf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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

import com.jayway.jsonpath.JsonPath;

import br.com.memdeck.AuthTestSupport.Session;
import br.com.memdeck.TestcontainersConfiguration;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class SocialIntegrationTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JdbcTemplate jdbc;

	private ResultActions profile(Session session) throws Exception {
		return mockMvc.perform(get("/api/users/me").cookie(session.session()));
	}

	private ResultActions updateProfile(Session session, String body) throws Exception {
		return mockMvc.perform(withCsrf(put("/api/users/me").cookie(session.session())
				.contentType(MediaType.APPLICATION_JSON).content(body), session.csrf()));
	}

	private String userId(Session session) throws Exception {
		return JsonPath.read(profile(session).andReturn().getResponse().getContentAsString(), "$.id");
	}

	private String createDeck(Session session, String visibility) throws Exception {
		String location = mockMvc.perform(withCsrf(post("/api/decks").cookie(session.session())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Deck\",\"visibility\":\"%s\"}".formatted(visibility)), session.csrf()))
				.andExpect(status().isCreated()).andReturn().getResponse().getHeader("Location");
		return location.substring(location.lastIndexOf('/') + 1);
	}

	private String addFlashcard(Session session, String deckId, String front) throws Exception {
		String json = mockMvc.perform(withCsrf(post("/api/decks/" + deckId + "/flashcards").cookie(session.session())
				.contentType(MediaType.APPLICATION_JSON).content("{\"front\":\"%s\",\"back\":\"A\"}".formatted(front)),
				session.csrf())).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
		return JsonPath.read(json, "$.id");
	}

	private void study(Session session, String deckId, String flashcardId) throws Exception {
		mockMvc.perform(withCsrf(post("/api/decks/" + deckId + "/flashcards/" + flashcardId + "/reviews")
				.cookie(session.session()).contentType(MediaType.APPLICATION_JSON).content("{\"grade\":\"GOOD\"}"),
				session.csrf())).andExpect(status().isOk());
	}

	private ResultActions sendRequest(Session session, String email) throws Exception {
		return mockMvc.perform(withCsrf(post("/api/users/me/friend-requests").cookie(session.session())
				.contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"%s\"}".formatted(email)), session.csrf()));
	}

	private String firstRequestId(Session session) throws Exception {
		return JsonPath.read(mockMvc.perform(get("/api/users/me/friend-requests").cookie(session.session()))
				.andReturn().getResponse().getContentAsString(), "$[0].id");
	}

	private ResultActions answerRequest(Session session, String id, String action) throws Exception {
		return mockMvc.perform(withCsrf(post("/api/users/me/friend-requests/" + id + "/" + action)
				.cookie(session.session()), session.csrf()));
	}

	@Test
	void aNewProfileHasTheDefaultAvatarAndNoActivityAndNeverExposesThePassword() throws Exception {
		String email = uniqueEmail();
		Session session = registerAndLogin(mockMvc, email);

		profile(session).andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value(email))
				.andExpect(jsonPath("$.avatar").value("BOOK"))
				.andExpect(jsonPath("$.reviewedDays").value(0))
				.andExpect(jsonPath("$.streakDays").value(0))
				.andExpect(jsonPath("$.friends").value(0))
				.andExpect(jsonPath("$.passwordHash").doesNotExist());
	}

	@Test
	void changesTheNameAndTheAvatarAndKeepsThemAcrossRequests() throws Exception {
		Session session = registerAndLogin(mockMvc, uniqueEmail());

		updateProfile(session, "{\"name\":\"  Julia Hormuth \",\"avatar\":\"STAR\"}").andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Julia Hormuth")).andExpect(jsonPath("$.avatar").value("STAR"));

		profile(session).andExpect(jsonPath("$.name").value("Julia Hormuth")).andExpect(jsonPath("$.avatar").value("STAR"));
		assertThat(jdbc.queryForObject("SELECT avatar FROM users WHERE id = ?::uuid", String.class, userId(session)))
				.isEqualTo("STAR");
	}

	@Test
	void rejectsAnInvalidNameOrAvatarAndChangesNothing() throws Exception {
		Session session = registerAndLogin(mockMvc, uniqueEmail());

		updateProfile(session, "{\"name\":\" \",\"avatar\":\"STAR\"}").andExpect(status().isBadRequest());
		updateProfile(session, "{\"name\":\"Julia\",\"avatar\":\"ROCKET\"}").andExpect(status().isBadRequest());
		updateProfile(session, "{\"name\":\"Julia\"}").andExpect(status().isBadRequest());

		profile(session).andExpect(jsonPath("$.avatar").value("BOOK"));
	}

	@Test
	void studyingCountsADayAStreakAndEachCardOncePerDay() throws Exception {
		Session session = registerAndLogin(mockMvc, uniqueEmail());
		String deckId = createDeck(session, "PRIVATE");
		String first = addFlashcard(session, deckId, "One");
		String second = addFlashcard(session, deckId, "Two");

		study(session, deckId, first);
		study(session, deckId, first);
		study(session, deckId, second);

		profile(session).andExpect(jsonPath("$.reviewedDays").value(1)).andExpect(jsonPath("$.streakDays").value(1));
		assertThat(jdbc.queryForObject("SELECT count(*) FROM review_logs WHERE deck_id = ?::uuid", Integer.class, deckId))
				.isEqualTo(2);
	}

	@Test
	void theFriendRequestFlowFromSendingToAcceptingAndRemoving() throws Exception {
		Session ana = registerAndLogin(mockMvc, uniqueEmail());
		String brunoEmail = uniqueEmail();
		Session bruno = registerAndLogin(mockMvc, brunoEmail);

		sendRequest(ana, brunoEmail.toUpperCase()).andExpect(status().isAccepted()).andExpect(content().string(""));
		mockMvc.perform(get("/api/users/me/friend-requests").cookie(bruno.session()))
				.andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].direction").value("INCOMING"));
		mockMvc.perform(get("/api/users/me/friend-requests").cookie(ana.session()))
				.andExpect(jsonPath("$[0].direction").value("OUTGOING"));
		profile(bruno).andExpect(jsonPath("$.friends").value(0));

		answerRequest(bruno, firstRequestId(bruno), "accept").andExpect(status().isNoContent());

		mockMvc.perform(get("/api/users/me/friends").cookie(ana.session()))
				.andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].userId").value(userId(bruno)));
		mockMvc.perform(get("/api/users/me/friends").cookie(bruno.session()))
				.andExpect(jsonPath("$[0].userId").value(userId(ana)));
		profile(ana).andExpect(jsonPath("$.friends").value(1));
		mockMvc.perform(get("/api/users/me/friend-requests").cookie(ana.session())).andExpect(jsonPath("$.length()").value(0));

		mockMvc.perform(withCsrf(delete("/api/users/me/friends/" + userId(ana)).cookie(bruno.session()), bruno.csrf()))
				.andExpect(status().isNoContent());
		profile(ana).andExpect(jsonPath("$.friends").value(0));
	}

	@Test
	void decliningAndCancellingDeleteTheRequestSoItCanBeSentAgain() throws Exception {
		Session ana = registerAndLogin(mockMvc, uniqueEmail());
		String brunoEmail = uniqueEmail();
		Session bruno = registerAndLogin(mockMvc, brunoEmail);
		sendRequest(ana, brunoEmail).andExpect(status().isAccepted());

		answerRequest(bruno, firstRequestId(bruno), "decline").andExpect(status().isNoContent());
		sendRequest(ana, brunoEmail).andExpect(status().isAccepted());
		String id = firstRequestId(ana);
		mockMvc.perform(withCsrf(delete("/api/users/me/friend-requests/" + id).cookie(ana.session()), ana.csrf()))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/users/me/friend-requests").cookie(bruno.session())).andExpect(jsonPath("$.length()").value(0));
		answerRequest(bruno, id, "accept").andExpect(status().isNotFound());
	}

	@Test
	void anUnknownEmailGetsTheSameAnswerAndTheOwnEmailAndABadEmailAreRejected() throws Exception {
		String email = uniqueEmail();
		Session ana = registerAndLogin(mockMvc, email);

		sendRequest(ana, "nobody-" + UUID.randomUUID() + "@example.com").andExpect(status().isAccepted()).andExpect(content().string(""));
		sendRequest(ana, email).andExpect(status().isBadRequest());
		sendRequest(ana, "not-an-email").andExpect(status().isBadRequest());

		mockMvc.perform(get("/api/users/me/friend-requests").cookie(ana.session())).andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void nobodyElseCanAnswerOrCancelSomeoneElsesRequest() throws Exception {
		Session ana = registerAndLogin(mockMvc, uniqueEmail());
		String brunoEmail = uniqueEmail();
		Session bruno = registerAndLogin(mockMvc, brunoEmail);
		Session carla = registerAndLogin(mockMvc, uniqueEmail());
		sendRequest(ana, brunoEmail).andExpect(status().isAccepted());
		String id = firstRequestId(ana);

		answerRequest(carla, id, "accept").andExpect(status().isNotFound());
		answerRequest(ana, id, "accept").andExpect(status().isNotFound());
		mockMvc.perform(withCsrf(delete("/api/users/me/friend-requests/" + id).cookie(bruno.session()), bruno.csrf()))
				.andExpect(status().isNotFound());
	}

	@Test
	void theRankingOrdersParticipantsByReviewedFlashcardsAndMarksMe() throws Exception {
		Session owner = registerAndLogin(mockMvc, uniqueEmail());
		Session reader = registerAndLogin(mockMvc, uniqueEmail());
		Session stranger = registerAndLogin(mockMvc, uniqueEmail());
		String deckId = createDeck(owner, "PUBLIC");
		String one = addFlashcard(owner, deckId, "One");
		String two = addFlashcard(owner, deckId, "Two");
		mockMvc.perform(withCsrf(post("/api/decks/" + deckId + "/members").cookie(reader.session()), reader.csrf()))
				.andExpect(status().isCreated());
		study(owner, deckId, one);
		study(reader, deckId, one);
		study(reader, deckId, two);

		mockMvc.perform(get("/api/decks/" + deckId + "/ranking").cookie(owner.session()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].userId").value(userId(reader)))
				.andExpect(jsonPath("$[0].reviewedFlashcards").value(2))
				.andExpect(jsonPath("$[0].position").value(1))
				.andExpect(jsonPath("$[0].me").value(false))
				.andExpect(jsonPath("$[1].userId").value(userId(owner)))
				.andExpect(jsonPath("$[1].reviewedFlashcards").value(1))
				.andExpect(jsonPath("$[1].me").value(true))
				.andExpect(jsonPath("$[1].email").doesNotExist());
		mockMvc.perform(get("/api/decks/" + deckId + "/ranking").cookie(stranger.session())).andExpect(status().isForbidden());
	}

	@Test
	void theRankingOfAPrivateDeckIsHiddenFromStrangers() throws Exception {
		Session owner = registerAndLogin(mockMvc, uniqueEmail());
		Session stranger = registerAndLogin(mockMvc, uniqueEmail());
		String deckId = createDeck(owner, "PRIVATE");

		mockMvc.perform(get("/api/decks/" + deckId + "/ranking").cookie(stranger.session())).andExpect(status().isNotFound());
		mockMvc.perform(get("/api/decks/" + UUID.randomUUID() + "/ranking").cookie(owner.session())).andExpect(status().isNotFound());
	}

	@Test
	void everyRouteRequiresALoggedUser() throws Exception {
		mockMvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());
		mockMvc.perform(get("/api/users/me/friends")).andExpect(status().isUnauthorized());
		mockMvc.perform(get("/api/decks/" + UUID.randomUUID() + "/ranking")).andExpect(status().isUnauthorized());
		mockMvc.perform(withCsrf(post("/api/users/me/friend-requests").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"a@b.co\"}"), csrfCookie(mockMvc))).andExpect(status().isUnauthorized());
	}
}
