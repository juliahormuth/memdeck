package br.com.memdeck.deck.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.UUID;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.memdeck.deck.application.CreateDeckUseCase;
import br.com.memdeck.deck.application.GetDeckUseCase;
import br.com.memdeck.deck.domain.Deck;
import br.com.memdeck.deck.domain.DeckNotFoundException;
import br.com.memdeck.deck.domain.InvalidDeckException;
import br.com.memdeck.deck.domain.Visibility;
import br.com.memdeck.security.SecurityConfig;

@WebMvcTest(DeckController.class)
@Import(SecurityConfig.class)
@WithMockUser
class DeckControllerTest {

	@Autowired
	MockMvc mockMvc;

	@MockitoBean
	CreateDeckUseCase createDeck;

	@MockitoBean
	GetDeckUseCase getDeck;

	@Test
	void createsValidDeckAndReturns201WithLocation() throws Exception {
		Deck saved = Deck.create("Anatomy", "Bones of the body", Visibility.PRIVATE);
		when(createDeck.execute(any())).thenReturn(saved);

		mockMvc.perform(post("/api/decks").with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name":"Anatomy","description":"Bones of the body","visibility":"PRIVATE"}
						"""))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", Matchers.endsWith("/api/decks/" + saved.getId())))
				.andExpect(jsonPath("$.id").value(saved.getId().toString()))
				.andExpect(jsonPath("$.name").value("Anatomy"))
				.andExpect(jsonPath("$.visibility").value("PRIVATE"));
	}

	@Test
	void rejectsBlankNameWithoutCallingTheUseCase() throws Exception {
		mockMvc.perform(post("/api/decks").with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name":"   ","visibility":"PUBLIC"}
						"""))
				.andExpect(status().isBadRequest());

		verifyNoInteractions(createDeck);
	}

	@Test
	void rejectsMissingVisibility() throws Exception {
		mockMvc.perform(post("/api/decks").with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name":"English"}
						"""))
				.andExpect(status().isBadRequest());
	}

	@Test
	void rejectsInvalidVisibility() throws Exception {
		mockMvc.perform(post("/api/decks").with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name":"English","visibility":"SECRET"}
						"""))
				.andExpect(status().isBadRequest());
	}

	@Test
	void translatesDomainValidationErrorTo400() throws Exception {
		when(createDeck.execute(any())).thenThrow(new InvalidDeckException("Deck name must not be blank"));

		mockMvc.perform(post("/api/decks").with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name":"English","visibility":"PUBLIC"}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("Deck name must not be blank"));
	}

	@Test
	void translatesMissingDeckTo404() throws Exception {
		UUID id = UUID.randomUUID();
		when(getDeck.execute(id)).thenThrow(new DeckNotFoundException(id));

		mockMvc.perform(get("/api/decks/" + id))
				.andExpect(status().isNotFound());
	}
}
