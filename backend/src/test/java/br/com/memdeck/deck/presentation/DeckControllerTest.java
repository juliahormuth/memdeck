package br.com.memdeck.deck.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import br.com.memdeck.deck.application.GetDeckUseCase;
import br.com.memdeck.deck.application.ListMyDecksUseCase;
import br.com.memdeck.deck.domain.Deck;
import br.com.memdeck.deck.domain.DeckNotFoundException;
import br.com.memdeck.deck.domain.InvalidDeckException;
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
		when(listMyDecks.execute(USER_ID)).thenReturn(List.of(second, first));

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
		when(getDeck.execute(deck.getId(), USER_ID)).thenReturn(deck);

		mockMvc.perform(get("/api/decks/" + deck.getId()).with(authentication(USER)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Anatomy"));
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
}
