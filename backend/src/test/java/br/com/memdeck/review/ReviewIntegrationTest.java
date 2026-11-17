package br.com.memdeck.review;

import static br.com.memdeck.AuthTestSupport.csrfCookie;
import static br.com.memdeck.AuthTestSupport.registerAndLogin;
import static br.com.memdeck.AuthTestSupport.uniqueEmail;
import static br.com.memdeck.AuthTestSupport.withCsrf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.ZoneOffset;
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
class ReviewIntegrationTest {

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

	private String addFlashcard(Session session, String deckId, String front) throws Exception {
		String json = mockMvc.perform(withCsrf(post("/api/decks/" + deckId + "/flashcards").cookie(session.session())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"front\":\"%s\",\"back\":\"answer\"}".formatted(front)), session.csrf()))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return com.jayway.jsonpath.JsonPath.read(json, "$.id");
	}

	private ResultActions review(Session session, String deckId, String flashcardId, String grade) throws Exception {
		return mockMvc.perform(withCsrf(post("/api/decks/" + deckId + "/flashcards/" + flashcardId + "/reviews")
				.cookie(session.session()).contentType(MediaType.APPLICATION_JSON)
				.content("{\"grade\":\"%s\"}".formatted(grade)), session.csrf()));
	}

	private ResultActions queue(Session session, String deckId) throws Exception {
		return mockMvc.perform(get("/api/decks/" + deckId + "/study").cookie(session.session()));
	}

	private int scheduleCount(String flashcardId) {
		return jdbc.queryForObject("SELECT count(*) FROM review_schedules WHERE flashcard_id = ?::uuid",
				Integer.class, flashcardId);
	}

	@Test
	void studyingAFlashcardSchedulesItAndRemovesItFromTodaysQueue() throws Exception {
		Session owner = registerAndLogin(mockMvc, uniqueEmail());
		String deckId = createDeck(owner, "PRIVATE");
		String cardId = addFlashcard(owner, deckId, "Femur");

		queue(owner, deckId).andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].id").value(cardId))
				.andExpect(jsonPath("$[0].newCard").value(true));

		String tomorrow = LocalDate.now(ZoneOffset.UTC).plusDays(1).toString();
		review(owner, deckId, cardId, "GOOD").andExpect(status().isOk())
				.andExpect(jsonPath("$.dueDate").value(tomorrow))
				.andExpect(jsonPath("$.intervalDays").value(1));

		queue(owner, deckId).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
		assertThat(scheduleCount(cardId)).isEqualTo(1);
	}

	@Test
	void aCardDueTodayComesBackWithTheStoredSchedule() throws Exception {
		Session owner = registerAndLogin(mockMvc, uniqueEmail());
		String deckId = createDeck(owner, "PRIVATE");
		String cardId = addFlashcard(owner, deckId, "Femur");
		review(owner, deckId, cardId, "GOOD").andExpect(status().isOk());

		jdbc.update("UPDATE review_schedules SET due_date = ?, first_reviewed_on = ? WHERE flashcard_id = ?::uuid",
				LocalDate.now(ZoneOffset.UTC), LocalDate.now(ZoneOffset.UTC).minusDays(1), cardId);

		queue(owner, deckId).andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].newCard").value(false));
		review(owner, deckId, cardId, "GOOD").andExpect(jsonPath("$.intervalDays").value(6));
	}

	@Test
	void offersTwentyNewCardsPerDayAndCountsTheOnesAlreadyStudiedToday() throws Exception {
		Session owner = registerAndLogin(mockMvc, uniqueEmail());
		String deckId = createDeck(owner, "PRIVATE");
		String firstId = null;
		for (int i = 1; i <= 22; i++) {
			String id = addFlashcard(owner, deckId, "card " + i);
			if (firstId == null) {
				firstId = id;
			}
		}

		queue(owner, deckId).andExpect(jsonPath("$.length()").value(20))
				.andExpect(jsonPath("$[0].front").value("card 1"));
		review(owner, deckId, firstId, "AGAIN").andExpect(status().isOk());

		queue(owner, deckId).andExpect(jsonPath("$.length()").value(19));
	}

	@Test
	void eachUserHasAnIndependentScheduleOnAPublicDeck() throws Exception {
		Session owner = registerAndLogin(mockMvc, uniqueEmail());
		Session stranger = registerAndLogin(mockMvc, uniqueEmail());
		String deckId = createDeck(owner, "PUBLIC");
		String cardId = addFlashcard(owner, deckId, "Open");
		review(owner, deckId, cardId, "EASY").andExpect(status().isOk());

		queue(owner, deckId).andExpect(jsonPath("$.length()").value(0));
		queue(stranger, deckId).andExpect(jsonPath("$.length()").value(1));
		review(stranger, deckId, cardId, "GOOD").andExpect(status().isOk());
		assertThat(scheduleCount(cardId)).isEqualTo(2);
	}

	@Test
	void aStrangerGets404ForAPrivateDeck() throws Exception {
		Session owner = registerAndLogin(mockMvc, uniqueEmail());
		Session stranger = registerAndLogin(mockMvc, uniqueEmail());
		String deckId = createDeck(owner, "PRIVATE");
		String cardId = addFlashcard(owner, deckId, "Secret");

		queue(stranger, deckId).andExpect(status().isNotFound());
		review(stranger, deckId, cardId, "GOOD").andExpect(status().isNotFound());
		assertThat(scheduleCount(cardId)).isZero();
	}

	@Test
	void aFlashcardOfAnotherDeckIsNotFound() throws Exception {
		Session owner = registerAndLogin(mockMvc, uniqueEmail());
		String deckId = createDeck(owner, "PRIVATE");
		String otherDeckId = createDeck(owner, "PRIVATE");
		String cardId = addFlashcard(owner, otherDeckId, "Elsewhere");

		review(owner, deckId, cardId, "GOOD").andExpect(status().isNotFound());
		review(owner, deckId, UUID.randomUUID().toString(), "GOOD").andExpect(status().isNotFound());
	}

	@Test
	void rejectsAnUnknownGrade() throws Exception {
		Session owner = registerAndLogin(mockMvc, uniqueEmail());
		String deckId = createDeck(owner, "PRIVATE");
		String cardId = addFlashcard(owner, deckId, "Femur");

		review(owner, deckId, cardId, "PERFECT").andExpect(status().isBadRequest());
		assertThat(scheduleCount(cardId)).isZero();
	}

	@Test
	void deletingTheFlashcardOrTheDeckRemovesItsSchedules() throws Exception {
		Session owner = registerAndLogin(mockMvc, uniqueEmail());
		String deckId = createDeck(owner, "PRIVATE");
		String firstId = addFlashcard(owner, deckId, "First");
		String secondId = addFlashcard(owner, deckId, "Second");
		review(owner, deckId, firstId, "GOOD").andExpect(status().isOk());
		review(owner, deckId, secondId, "GOOD").andExpect(status().isOk());

		mockMvc.perform(withCsrf(delete("/api/decks/" + deckId + "/flashcards/" + firstId)
				.cookie(owner.session()), owner.csrf())).andExpect(status().isNoContent());
		assertThat(scheduleCount(firstId)).isZero();
		assertThat(scheduleCount(secondId)).isEqualTo(1);

		mockMvc.perform(withCsrf(delete("/api/decks/" + deckId).cookie(owner.session()), owner.csrf()))
				.andExpect(status().isNoContent());
		assertThat(scheduleCount(secondId)).isZero();
	}

	@Test
	void requireALoggedUser() throws Exception {
		mockMvc.perform(get("/api/decks/" + UUID.randomUUID() + "/study")).andExpect(status().isUnauthorized());
		mockMvc.perform(withCsrf(post("/api/decks/" + UUID.randomUUID() + "/flashcards/" + UUID.randomUUID()
				+ "/reviews").contentType(MediaType.APPLICATION_JSON).content("{\"grade\":\"GOOD\"}"),
				csrfCookie(mockMvc)))
				.andExpect(status().isUnauthorized());
	}
}
