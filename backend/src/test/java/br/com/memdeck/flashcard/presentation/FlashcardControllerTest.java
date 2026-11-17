package br.com.memdeck.flashcard.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import br.com.memdeck.flashcard.application.CreateFlashcardCommand;
import br.com.memdeck.flashcard.application.CreateFlashcardUseCase;
import br.com.memdeck.flashcard.application.DeleteFlashcardUseCase;
import br.com.memdeck.flashcard.application.ListDeckFlashcardsUseCase;
import br.com.memdeck.flashcard.application.UpdateFlashcardCommand;
import br.com.memdeck.flashcard.application.UpdateFlashcardUseCase;
import br.com.memdeck.flashcard.domain.Flashcard;
import br.com.memdeck.flashcard.domain.FlashcardNotFoundException;
import br.com.memdeck.flashcard.domain.InvalidFlashcardException;
import br.com.memdeck.deck.domain.NotDeckOwnerException;
import br.com.memdeck.security.AuthenticatedUser;
import br.com.memdeck.security.SecurityConfig;

@WebMvcTest(FlashcardController.class)
@Import(SecurityConfig.class)
class FlashcardControllerTest {

	private static final UUID USER_ID = UUID.randomUUID();
	private static final UUID DECK_ID = UUID.randomUUID();
	private static final String URL = "/api/decks/" + DECK_ID + "/flashcards";
	private static final String BODY = "{\"front\":\"Femur\",\"back\":\"Thigh bone\"}";
	private static final Authentication USER = UsernamePasswordAuthenticationToken.authenticated(
			new AuthenticatedUser(USER_ID, "julia@example.com"), null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

	@Autowired
	MockMvc mockMvc;

	@MockitoBean
	CreateFlashcardUseCase createFlashcard;

	@MockitoBean
	ListDeckFlashcardsUseCase listDeckFlashcards;

	@MockitoBean
	UpdateFlashcardUseCase updateFlashcard;

	@MockitoBean
	DeleteFlashcardUseCase deleteFlashcard;

	@Test
	void createsTheFlashcardForTheLoggedUserAndReturns201() throws Exception {
		when(createFlashcard.execute(any())).thenReturn(Flashcard.create(DECK_ID, "Femur", "Thigh bone"));

		mockMvc.perform(post(URL).with(authentication(USER)).with(csrf())
				.contentType(MediaType.APPLICATION_JSON).content(BODY))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.deckId").value(DECK_ID.toString()))
				.andExpect(jsonPath("$.front").value("Femur"))
				.andExpect(jsonPath("$.back").value("Thigh bone"));

		ArgumentCaptor<CreateFlashcardCommand> command = ArgumentCaptor.forClass(CreateFlashcardCommand.class);
		verify(createFlashcard).execute(command.capture());
		assertThat(command.getValue().requesterId()).isEqualTo(USER_ID);
		assertThat(command.getValue().deckId()).isEqualTo(DECK_ID);
	}

	@Test
	void rejectsBlankFrontAndBackWithoutCallingTheUseCase() throws Exception {
		mockMvc.perform(post(URL).with(authentication(USER)).with(csrf())
				.contentType(MediaType.APPLICATION_JSON).content("{\"front\":\" \",\"back\":\"x\"}"))
				.andExpect(status().isBadRequest());
		mockMvc.perform(post(URL).with(authentication(USER)).with(csrf())
				.contentType(MediaType.APPLICATION_JSON).content("{\"front\":\"x\"}"))
				.andExpect(status().isBadRequest());

		verifyNoInteractions(createFlashcard);
	}

	@Test
	void rejectsTextOverTheLimitsWithoutCallingTheUseCase() throws Exception {
		mockMvc.perform(post(URL).with(authentication(USER)).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"front\":\"%s\",\"back\":\"x\"}".formatted("a".repeat(501))))
				.andExpect(status().isBadRequest());
		mockMvc.perform(post(URL).with(authentication(USER)).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"front\":\"x\",\"back\":\"%s\"}".formatted("a".repeat(10001))))
				.andExpect(status().isBadRequest());

		verifyNoInteractions(createFlashcard);
	}

	@Test
	void translatesDomainValidationErrorTo400() throws Exception {
		when(createFlashcard.execute(any())).thenThrow(new InvalidFlashcardException("Flashcard front must not be blank"));

		mockMvc.perform(post(URL).with(authentication(USER)).with(csrf())
				.contentType(MediaType.APPLICATION_JSON).content(BODY))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("Flashcard front must not be blank"));
	}

	@Test
	void translatesMissingDeckTo404() throws Exception {
		when(createFlashcard.execute(any())).thenThrow(new DeckNotFoundException(DECK_ID));

		mockMvc.perform(post(URL).with(authentication(USER)).with(csrf())
				.contentType(MediaType.APPLICATION_JSON).content(BODY))
				.andExpect(status().isNotFound());
	}

	@Test
	void translatesANonOwnerTo403() throws Exception {
		when(createFlashcard.execute(any())).thenThrow(new NotDeckOwnerException(DECK_ID));

		mockMvc.perform(post(URL).with(authentication(USER)).with(csrf())
				.contentType(MediaType.APPLICATION_JSON).content(BODY))
				.andExpect(status().isForbidden());
	}

	@Test
	void listsTheFlashcardsOfTheDeckOnBehalfOfTheLoggedUser() throws Exception {
		when(listDeckFlashcards.execute(DECK_ID, USER_ID)).thenReturn(List.of(
				Flashcard.create(DECK_ID, "Tibia", "Shin bone"), Flashcard.create(DECK_ID, "Femur", "Thigh bone")));

		mockMvc.perform(get(URL).with(authentication(USER)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].front").value("Tibia"))
				.andExpect(jsonPath("$[1].back").value("Thigh bone"));
	}

	@Test
	void translatesAnInvisibleDeckTo404WhenListing() throws Exception {
		when(listDeckFlashcards.execute(DECK_ID, USER_ID)).thenThrow(new DeckNotFoundException(DECK_ID));

		mockMvc.perform(get(URL).with(authentication(USER))).andExpect(status().isNotFound());
	}

	@Test
	void requiresALoggedUser() throws Exception {
		mockMvc.perform(post(URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(BODY))
				.andExpect(status().isUnauthorized());
		mockMvc.perform(get(URL)).andExpect(status().isUnauthorized());

		verifyNoInteractions(createFlashcard, listDeckFlashcards);
	}

	@Test
	void updatesTheFlashcardOnBehalfOfTheLoggedUser() throws Exception {
		UUID flashcardId = UUID.randomUUID();
		when(updateFlashcard.execute(any())).thenReturn(Flashcard.create(DECK_ID, "Tibia", "Shin bone"));

		mockMvc.perform(put(URL + "/" + flashcardId).with(authentication(USER)).with(csrf())
				.contentType(MediaType.APPLICATION_JSON).content("{\"front\":\"Tibia\",\"back\":\"Shin bone\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.front").value("Tibia"));

		ArgumentCaptor<UpdateFlashcardCommand> command = ArgumentCaptor.forClass(UpdateFlashcardCommand.class);
		verify(updateFlashcard).execute(command.capture());
		assertThat(command.getValue().requesterId()).isEqualTo(USER_ID);
		assertThat(command.getValue().deckId()).isEqualTo(DECK_ID);
		assertThat(command.getValue().flashcardId()).isEqualTo(flashcardId);
	}

	@Test
	void rejectsAnInvalidUpdateWithoutCallingTheUseCase() throws Exception {
		mockMvc.perform(put(URL + "/" + UUID.randomUUID()).with(authentication(USER)).with(csrf())
				.contentType(MediaType.APPLICATION_JSON).content("{\"front\":\" \",\"back\":\"x\"}"))
				.andExpect(status().isBadRequest());

		verifyNoInteractions(updateFlashcard);
	}

	@Test
	void translatesMissingFlashcardTo404AndANonOwnerTo403() throws Exception {
		UUID flashcardId = UUID.randomUUID();
		when(updateFlashcard.execute(any())).thenThrow(new FlashcardNotFoundException(flashcardId));
		doThrow(new NotDeckOwnerException(DECK_ID)).when(deleteFlashcard).execute(DECK_ID, flashcardId, USER_ID);

		mockMvc.perform(put(URL + "/" + flashcardId).with(authentication(USER)).with(csrf())
				.contentType(MediaType.APPLICATION_JSON).content(BODY))
				.andExpect(status().isNotFound());
		mockMvc.perform(delete(URL + "/" + flashcardId).with(authentication(USER)).with(csrf()))
				.andExpect(status().isForbidden());
	}

	@Test
	void deletesTheFlashcardAndAnswers204() throws Exception {
		UUID flashcardId = UUID.randomUUID();

		mockMvc.perform(delete(URL + "/" + flashcardId).with(authentication(USER)).with(csrf()))
				.andExpect(status().isNoContent());

		verify(deleteFlashcard).execute(DECK_ID, flashcardId, USER_ID);
	}

	@Test
	void changingOrDeletingRequiresALoggedUser() throws Exception {
		mockMvc.perform(put(URL + "/" + UUID.randomUUID()).with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content(BODY)).andExpect(status().isUnauthorized());
		mockMvc.perform(delete(URL + "/" + UUID.randomUUID()).with(csrf())).andExpect(status().isUnauthorized());

		verifyNoInteractions(updateFlashcard, deleteFlashcard);
	}
}
