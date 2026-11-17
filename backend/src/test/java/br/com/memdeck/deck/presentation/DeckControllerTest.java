package br.com.memdeck.deck.presentation;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.hamcrest.Matchers;
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

import br.com.memdeck.deck.application.CreateDeckCommand;
import br.com.memdeck.deck.application.CreateDeckUseCase;
import br.com.memdeck.deck.application.DeleteDeckUseCase;
import br.com.memdeck.deck.application.DeckView;
import br.com.memdeck.deck.application.GetDeckUseCase;
import br.com.memdeck.deck.application.JoinDeckUseCase;
import br.com.memdeck.deck.application.SearchPublicDecksUseCase;
import br.com.memdeck.deck.domain.AlreadyDeckMemberException;
import br.com.memdeck.deck.domain.DeckRole;
import br.com.memdeck.deck.application.ListMyDecksUseCase;
import br.com.memdeck.deck.application.UpdateDeckUseCase;
import br.com.memdeck.deck.domain.Deck;
import br.com.memdeck.deck.domain.DeckNotFoundException;
import br.com.memdeck.deck.application.UpdateDeckCommand;
import br.com.memdeck.deck.domain.InvalidDeckException;
import br.com.memdeck.deck.domain.NotDeckOwnerException;
import br.com.memdeck.deck.domain.Visibility;
import br.com.memdeck.security.AuthenticatedUser;
import br.com.memdeck.security.SecurityConfig;

@WebMvcTest(DeckController.class)
@Import(SecurityConfig.class)
class DeckControllerTest {

	private static final UUID USER_ID = UUID.randomUUID();
	private static final Authentication USER = UsernamePasswordAuthenticationToken.authenticated(
			new AuthenticatedUser(USER_ID, "julia@example.com"), null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

	@Autowired
	MockMvc mockMvc;

	@MockitoBean
	CreateDeckUseCase createDeck;

	@MockitoBean
	GetDeckUseCase getDeck;

	@MockitoBean
	ListMyDecksUseCase listMyDecks;

	@MockitoBean
	SearchPublicDecksUseCase searchPublicDecks;

	@MockitoBean
	JoinDeckUseCase joinDeck;

	@MockitoBean
	UpdateDeckUseCase updateDeck;

	@MockitoBean
	DeleteDeckUseCase deleteDeck;

	@Test
	void createsValidDeckForTheLoggedUserAndReturns201WithLocation() throws Exception {
		Deck saved = Deck.create(USER_ID, "Anatomy", "Bones of the body", Visibility.PRIVATE);
		when(createDeck.execute(any())).thenReturn(saved);

		mockMvc.perform(post("/api/decks").with(authentication(USER)).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name":"Anatomy","description":"Bones of the body","visibility":"PRIVATE"}
						"""))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", Matchers.endsWith("/api/decks/" + saved.getId())))
				.andExpect(jsonPath("$.id").value(saved.getId().toString()))
				.andExpect(jsonPath("$.name").value("Anatomy"))
				.andExpect(jsonPath("$.visibility").value("PRIVATE"));

		ArgumentCaptor<CreateDeckCommand> command = ArgumentCaptor.forClass(CreateDeckCommand.class);
		verify(createDeck).execute(command.capture());
		assertThat(command.getValue().ownerId()).isEqualTo(USER_ID);
	}

	@Test
	void doesNotTakeTheOwnerFromTheRequestBody() throws Exception {
		when(createDeck.execute(any())).thenReturn(Deck.create(USER_ID, "Anatomy", null, Visibility.PRIVATE));
		UUID someoneElse = UUID.randomUUID();

		mockMvc.perform(post("/api/decks").with(authentication(USER)).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Anatomy\",\"visibility\":\"PRIVATE\",\"ownerId\":\"" + someoneElse + "\"}"))
				.andExpect(status().isCreated());

		ArgumentCaptor<CreateDeckCommand> command = ArgumentCaptor.forClass(CreateDeckCommand.class);
		verify(createDeck).execute(command.capture());
		assertThat(command.getValue().ownerId()).isEqualTo(USER_ID);
	}

	@Test
	void rejectsBlankNameWithoutCallingTheUseCase() throws Exception {
		mockMvc.perform(post("/api/decks").with(authentication(USER)).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name":"   ","visibility":"PUBLIC"}
						"""))
				.andExpect(status().isBadRequest());

		verifyNoInteractions(createDeck);
	}

	@Test
	void rejectsMissingVisibility() throws Exception {
		mockMvc.perform(post("/api/decks").with(authentication(USER)).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name":"English"}
						"""))
				.andExpect(status().isBadRequest());
	}

	@Test
	void rejectsInvalidVisibility() throws Exception {
		mockMvc.perform(post("/api/decks").with(authentication(USER)).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name":"English","visibility":"SECRET"}
						"""))
				.andExpect(status().isBadRequest());
	}

	@Test
	void translatesDomainValidationErrorTo400() throws Exception {
		when(createDeck.execute(any())).thenThrow(new InvalidDeckException("Deck name must not be blank"));

		mockMvc.perform(post("/api/decks").with(authentication(USER)).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name":"English","visibility":"PUBLIC"}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("Deck name must not be blank"));
	}

	@Test
	void listsTheDecksOfTheLoggedUser() throws Exception {
		Deck first = Deck.create(USER_ID, "Anatomy", null, Visibility.PRIVATE);
		Deck second = Deck.create(USER_ID, "English", "Words", Visibility.PUBLIC);
		when(listMyDecks.execute(USER_ID)).thenReturn(List.of(DeckView.ownedBy(second), DeckView.ownedBy(first)));

		mockMvc.perform(get("/api/decks").with(authentication(USER)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].name").value("English"))
				.andExpect(jsonPath("$[0].visibility").value("PUBLIC"))
				.andExpect(jsonPath("$[1].name").value("Anatomy"));
	}

	@Test
	void asksForTheDeckOnBehalfOfTheLoggedUser() throws Exception {
		Deck deck = Deck.create(USER_ID, "Anatomy", null, Visibility.PRIVATE);
		when(getDeck.execute(deck.getId(), USER_ID)).thenReturn(DeckView.ownedBy(deck));

		mockMvc.perform(get("/api/decks/" + deck.getId()).with(authentication(USER)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Anatomy"));
	}

	@Test
	void tellsWhetherTheLoggedUserOwnsTheDeck() throws Exception {
		Deck mine = Deck.create(USER_ID, "Anatomy", null, Visibility.PUBLIC);
		Deck notMine = Deck.create(UUID.randomUUID(), "English", null, Visibility.PUBLIC);
		when(getDeck.execute(mine.getId(), USER_ID)).thenReturn(DeckView.ownedBy(mine));
		when(getDeck.execute(notMine.getId(), USER_ID)).thenReturn(new DeckView(notMine, null));

		mockMvc.perform(get("/api/decks/" + mine.getId()).with(authentication(USER)))
				.andExpect(jsonPath("$.ownedByMe").value(true));
		mockMvc.perform(get("/api/decks/" + notMine.getId()).with(authentication(USER)))
				.andExpect(jsonPath("$.ownedByMe").value(false));
	}

	@Test
	void showsTheRoleOfTheLoggedUserOnEachDeck() throws Exception {
		Deck mine = Deck.create(USER_ID, "Anatomy", null, Visibility.PRIVATE);
		Deck joined = Deck.create(UUID.randomUUID(), "English", null, Visibility.PUBLIC);
		when(listMyDecks.execute(USER_ID))
				.thenReturn(List.of(DeckView.ownedBy(mine), new DeckView(joined, DeckRole.READER)));

		mockMvc.perform(get("/api/decks").with(authentication(USER)))
				.andExpect(jsonPath("$[0].role").value("OWNER"))
				.andExpect(jsonPath("$[1].role").value("READER"))
				.andExpect(jsonPath("$[1].ownedByMe").value(false));
	}

	@Test
	void searchesThePublicDecksWithTheTermAndReturnsTheRoleOfWhoAlreadyJoined() throws Exception {
		Deck open = Deck.create(UUID.randomUUID(), "English", "Words", Visibility.PUBLIC);
		Deck joined = Deck.create(UUID.randomUUID(), "English verbs", null, Visibility.PUBLIC);
		when(searchPublicDecks.execute(USER_ID, "english"))
				.thenReturn(List.of(new DeckView(open, null), new DeckView(joined, DeckRole.READER)));

		mockMvc.perform(get("/api/decks/public").param("search", "english").with(authentication(USER)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].name").value("English"))
				.andExpect(jsonPath("$[0].role").doesNotExist())
				.andExpect(jsonPath("$[1].role").value("READER"));
	}

	@Test
	void searchesWithAnEmptyTermWhenNoneIsGiven() throws Exception {
		when(searchPublicDecks.execute(USER_ID, "")).thenReturn(List.of());

		mockMvc.perform(get("/api/decks/public").with(authentication(USER)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void joinsAPublicDeckAsAReader() throws Exception {
		Deck deck = Deck.create(UUID.randomUUID(), "English", null, Visibility.PUBLIC);
		when(joinDeck.execute(deck.getId(), USER_ID)).thenReturn(new DeckView(deck, DeckRole.READER));

		mockMvc.perform(post("/api/decks/" + deck.getId() + "/members").with(authentication(USER)).with(csrf()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.role").value("READER"))
				.andExpect(jsonPath("$.ownedByMe").value(false));
	}

	@Test
	void joiningAMissingOrPrivateDeckIs404AndJoiningTwiceIs409() throws Exception {
		UUID missing = UUID.randomUUID();
		UUID joined = UUID.randomUUID();
		when(joinDeck.execute(missing, USER_ID)).thenThrow(new DeckNotFoundException(missing));
		when(joinDeck.execute(joined, USER_ID)).thenThrow(new AlreadyDeckMemberException(joined));

		mockMvc.perform(post("/api/decks/" + missing + "/members").with(authentication(USER)).with(csrf()))
				.andExpect(status().isNotFound());
		mockMvc.perform(post("/api/decks/" + joined + "/members").with(authentication(USER)).with(csrf()))
				.andExpect(status().isConflict());
	}

	@Test
	void searchAndJoinRequireALoggedUser() throws Exception {
		mockMvc.perform(get("/api/decks/public")).andExpect(status().isUnauthorized());
		mockMvc.perform(post("/api/decks/" + UUID.randomUUID() + "/members").with(csrf()))
				.andExpect(status().isUnauthorized());

		verifyNoInteractions(searchPublicDecks, joinDeck);
	}

	@Test
	void translatesMissingDeckTo404() throws Exception {
		UUID id = UUID.randomUUID();
		when(getDeck.execute(id, USER_ID)).thenThrow(new DeckNotFoundException(id));

		mockMvc.perform(get("/api/decks/" + id).with(authentication(USER)))
				.andExpect(status().isNotFound());
	}

	@Test
	void everyRouteRequiresALoggedUser() throws Exception {
		mockMvc.perform(get("/api/decks")).andExpect(status().isUnauthorized());
		mockMvc.perform(get("/api/decks/" + UUID.randomUUID())).andExpect(status().isUnauthorized());
		mockMvc.perform(post("/api/decks").with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"English\",\"visibility\":\"PUBLIC\"}")).andExpect(status().isUnauthorized());

		verifyNoInteractions(createDeck, getDeck, listMyDecks);
	}

	@Test
	void updatesTheDeckOnBehalfOfTheLoggedUser() throws Exception {
		UUID id = UUID.randomUUID();
		when(updateDeck.execute(any())).thenReturn(Deck.create(USER_ID, "Human anatomy", null, Visibility.PUBLIC));

		mockMvc.perform(put("/api/decks/" + id).with(authentication(USER)).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Human anatomy\",\"visibility\":\"PUBLIC\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Human anatomy"))
				.andExpect(jsonPath("$.ownedByMe").value(true));

		ArgumentCaptor<UpdateDeckCommand> command = ArgumentCaptor.forClass(UpdateDeckCommand.class);
		verify(updateDeck).execute(command.capture());
		assertThat(command.getValue().requesterId()).isEqualTo(USER_ID);
		assertThat(command.getValue().deckId()).isEqualTo(id);
	}

	@Test
	void rejectsAnInvalidUpdateWithoutCallingTheUseCase() throws Exception {
		mockMvc.perform(put("/api/decks/" + UUID.randomUUID()).with(authentication(USER)).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\" \",\"visibility\":\"PUBLIC\"}"))
				.andExpect(status().isBadRequest());

		verifyNoInteractions(updateDeck);
	}

	@Test
	void translatesANonOwnerTo403AndAMissingDeckTo404WhenChanging() throws Exception {
		UUID id = UUID.randomUUID();
		when(updateDeck.execute(any())).thenThrow(new NotDeckOwnerException(id));
		doThrow(new DeckNotFoundException(id)).when(deleteDeck).execute(id, USER_ID);

		mockMvc.perform(put("/api/decks/" + id).with(authentication(USER)).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"x\",\"visibility\":\"PUBLIC\"}"))
				.andExpect(status().isForbidden());
		mockMvc.perform(delete("/api/decks/" + id).with(authentication(USER)).with(csrf()))
				.andExpect(status().isNotFound());
	}

	@Test
	void deletesTheDeckAndAnswers204() throws Exception {
		UUID id = UUID.randomUUID();

		mockMvc.perform(delete("/api/decks/" + id).with(authentication(USER)).with(csrf()))
				.andExpect(status().isNoContent());

		verify(deleteDeck).execute(id, USER_ID);
	}

	@Test
	void changingOrDeletingRequiresALoggedUser() throws Exception {
		mockMvc.perform(put("/api/decks/" + UUID.randomUUID()).with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"x\",\"visibility\":\"PUBLIC\"}")).andExpect(status().isUnauthorized());
		mockMvc.perform(delete("/api/decks/" + UUID.randomUUID()).with(csrf())).andExpect(status().isUnauthorized());

		verifyNoInteractions(updateDeck, deleteDeck);
	}
}
