package br.com.memdeck.review.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.memdeck.deck.domain.DeckNotFoundException;
import br.com.memdeck.flashcard.domain.Flashcard;
import br.com.memdeck.flashcard.domain.FlashcardNotFoundException;
import br.com.memdeck.deck.domain.NotDeckParticipantException;
import br.com.memdeck.review.application.GetDeckRankingUseCase;
import br.com.memdeck.review.application.RankingEntry;
import br.com.memdeck.review.application.ListStudyQueueUseCase;
import br.com.memdeck.user.domain.Avatar;
import br.com.memdeck.review.application.RecordReviewCommand;
import br.com.memdeck.review.application.RecordReviewUseCase;
import br.com.memdeck.review.application.StudyCard;
import br.com.memdeck.review.domain.ReviewGrade;
import br.com.memdeck.review.domain.ReviewSchedule;
import br.com.memdeck.security.AuthenticatedUser;
import br.com.memdeck.security.SecurityConfig;

@WebMvcTest(ReviewController.class)
@Import(SecurityConfig.class)
class ReviewControllerTest {

	private static final UUID USER_ID = UUID.randomUUID();
	private static final UUID DECK_ID = UUID.randomUUID();
	private static final UUID CARD_ID = UUID.randomUUID();
	private static final String STUDY_URL = "/api/decks/" + DECK_ID + "/study";
	private static final String REVIEW_URL = "/api/decks/" + DECK_ID + "/flashcards/" + CARD_ID + "/reviews";
	private static final Authentication USER = UsernamePasswordAuthenticationToken.authenticated(
			new AuthenticatedUser(USER_ID, "julia@example.com"), null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

	@Autowired
	MockMvc mockMvc;

	@MockitoBean
	ListStudyQueueUseCase listStudyQueue;

	@MockitoBean
	RecordReviewUseCase recordReview;

	@MockitoBean
	GetDeckRankingUseCase getDeckRanking;

	@Test
	void returnsTheStudyQueueForTheLoggedUser() throws Exception {
		Flashcard card = Flashcard.create(DECK_ID, "Femur", "Thigh bone");
		when(listStudyQueue.execute(DECK_ID, USER_ID)).thenReturn(List.of(new StudyCard(card, true)));

		mockMvc.perform(get(STUDY_URL).with(authentication(USER)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].id").value(card.getId().toString()))
				.andExpect(jsonPath("$[0].front").value("Femur"))
				.andExpect(jsonPath("$[0].back").value("Thigh bone"))
				.andExpect(jsonPath("$[0].newCard").value(true));
	}

	@Test
	void translatesAMissingOrPrivateDeckTo404() throws Exception {
		when(listStudyQueue.execute(DECK_ID, USER_ID)).thenThrow(new DeckNotFoundException(DECK_ID));

		mockMvc.perform(get(STUDY_URL).with(authentication(USER))).andExpect(status().isNotFound());
	}

	@Test
	void theStudyQueueRequiresALoggedUser() throws Exception {
		mockMvc.perform(get(STUDY_URL)).andExpect(status().isUnauthorized());
	}

	@Test
	void recordsAReviewWithTheGradeAndReturnsTheNextDueDate() throws Exception {
		ReviewSchedule schedule = ReviewSchedule.firstReview(USER_ID, CARD_ID, ReviewGrade.GOOD,
				LocalDate.of(2026, 11, 17));
		when(recordReview.execute(any())).thenReturn(schedule);

		mockMvc.perform(post(REVIEW_URL).with(authentication(USER)).with(csrf())
				.contentType(MediaType.APPLICATION_JSON).content("{\"grade\":\"GOOD\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.dueDate").value("2026-11-18"))
				.andExpect(jsonPath("$.intervalDays").value(1));

		ArgumentCaptor<RecordReviewCommand> command = ArgumentCaptor.forClass(RecordReviewCommand.class);
		verify(recordReview).execute(command.capture());
		assertThat(command.getValue().requesterId()).isEqualTo(USER_ID);
		assertThat(command.getValue().deckId()).isEqualTo(DECK_ID);
		assertThat(command.getValue().flashcardId()).isEqualTo(CARD_ID);
		assertThat(command.getValue().grade()).isEqualTo(ReviewGrade.GOOD);
	}

	@Test
	void rejectsAMissingOrUnknownGradeWithoutCallingTheUseCase() throws Exception {
		mockMvc.perform(post(REVIEW_URL).with(authentication(USER)).with(csrf())
				.contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isBadRequest());
		mockMvc.perform(post(REVIEW_URL).with(authentication(USER)).with(csrf())
				.contentType(MediaType.APPLICATION_JSON).content("{\"grade\":\"PERFECT\"}"))
				.andExpect(status().isBadRequest());

		verifyNoInteractions(recordReview);
	}

	@Test
	void translatesAMissingFlashcardTo404() throws Exception {
		when(recordReview.execute(any())).thenThrow(new FlashcardNotFoundException(CARD_ID));

		mockMvc.perform(post(REVIEW_URL).with(authentication(USER)).with(csrf())
				.contentType(MediaType.APPLICATION_JSON).content("{\"grade\":\"EASY\"}"))
				.andExpect(status().isNotFound());
	}

	@Test
	void recordingAReviewRequiresACsrfToken() throws Exception {
		mockMvc.perform(post(REVIEW_URL).with(authentication(USER))
				.contentType(MediaType.APPLICATION_JSON).content("{\"grade\":\"GOOD\"}"))
				.andExpect(status().isForbidden());

		verifyNoInteractions(recordReview);
	}

	@Test
	void showsTheRankingMarkingTheLoggedUser() throws Exception {
		UUID other = UUID.randomUUID();
		when(getDeckRanking.execute(DECK_ID, USER_ID)).thenReturn(List.of(
				new RankingEntry(1, other, "Rita", Avatar.STAR, 9), new RankingEntry(2, USER_ID, "Julia", Avatar.BOOK, 4)));

		mockMvc.perform(get("/api/decks/" + DECK_ID + "/ranking").with(authentication(USER)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].position").value(1))
				.andExpect(jsonPath("$[0].name").value("Rita"))
				.andExpect(jsonPath("$[0].reviewedFlashcards").value(9))
				.andExpect(jsonPath("$[0].me").value(false))
				.andExpect(jsonPath("$[1].me").value(true));
	}

	@Test
	void theRankingIsForbiddenToNonParticipantsAndHiddenForPrivateDecks() throws Exception {
		when(getDeckRanking.execute(DECK_ID, USER_ID)).thenThrow(new NotDeckParticipantException(DECK_ID));

		mockMvc.perform(get("/api/decks/" + DECK_ID + "/ranking").with(authentication(USER))).andExpect(status().isForbidden());
		mockMvc.perform(get("/api/decks/" + DECK_ID + "/ranking")).andExpect(status().isUnauthorized());
	}
}
