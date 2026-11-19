package br.com.memdeck.invitation.presentation;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
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

import br.com.memdeck.deck.application.DeckView;
import br.com.memdeck.deck.domain.Deck;
import br.com.memdeck.deck.domain.DeckNotFoundException;
import br.com.memdeck.deck.domain.DeckRole;
import br.com.memdeck.deck.domain.NotDeckOwnerException;
import br.com.memdeck.deck.domain.Visibility;
import br.com.memdeck.invitation.application.AnswerInvitationUseCase;
import br.com.memdeck.invitation.application.CancelInvitationUseCase;
import br.com.memdeck.invitation.application.DeckInvitationView;
import br.com.memdeck.invitation.application.InviteToDeckCommand;
import br.com.memdeck.invitation.application.InviteToDeckUseCase;
import br.com.memdeck.invitation.application.ListDeckInvitationsUseCase;
import br.com.memdeck.invitation.application.ListMyInvitationsUseCase;
import br.com.memdeck.invitation.application.MyInvitationView;
import br.com.memdeck.invitation.domain.InvalidInvitationException;
import br.com.memdeck.invitation.domain.InvitationNotFoundException;
import br.com.memdeck.invitation.domain.InvitationNotPendingException;
import br.com.memdeck.security.AuthenticatedUser;
import br.com.memdeck.security.SecurityConfig;

@WebMvcTest(InvitationController.class)
@Import(SecurityConfig.class)
class InvitationControllerTest {

	private static final UUID USER_ID = UUID.randomUUID();
	private static final UUID DECK_ID = UUID.randomUUID();
	private static final UUID INVITATION_ID = UUID.randomUUID();
	private static final String DECK_URL = "/api/decks/" + DECK_ID + "/invitations";
	private static final Authentication USER = UsernamePasswordAuthenticationToken.authenticated(
			new AuthenticatedUser(USER_ID, "julia@example.com"), null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

	@Autowired
	MockMvc mockMvc;

	@MockitoBean
	InviteToDeckUseCase inviteToDeck;

	@MockitoBean
	ListDeckInvitationsUseCase listDeckInvitations;

	@MockitoBean
	CancelInvitationUseCase cancelInvitation;

	@MockitoBean
	ListMyInvitationsUseCase listMyInvitations;

	@MockitoBean
	AnswerInvitationUseCase answerInvitation;

	@Test
	void invitesAndAnswersWithAnEmpty202() throws Exception {
		mockMvc.perform(post(DECK_URL).with(authentication(USER)).with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"rita@example.com\",\"role\":\"EDITOR\"}"))
				.andExpect(status().isAccepted())
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(""));

		ArgumentCaptor<InviteToDeckCommand> command = ArgumentCaptor.forClass(InviteToDeckCommand.class);
		verify(inviteToDeck).execute(command.capture());
		assertThat(command.getValue().requesterId()).isEqualTo(USER_ID);
		assertThat(command.getValue().deckId()).isEqualTo(DECK_ID);
		assertThat(command.getValue().email()).isEqualTo("rita@example.com");
		assertThat(command.getValue().role()).isEqualTo(DeckRole.EDITOR);
	}

	@Test
	void rejectsABlankEmailOrAMissingOrUnknownRoleWithoutCallingTheUseCase() throws Exception {
		for (String body : List.of("{\"email\":\" \",\"role\":\"READER\"}", "{\"email\":\"a@b.co\"}",
				"{\"email\":\"a@b.co\",\"role\":\"BOSS\"}")) {
			mockMvc.perform(post(DECK_URL).with(authentication(USER)).with(csrf())
					.contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isBadRequest());
		}

		verifyNoInteractions(inviteToDeck);
	}

	@Test
	void translatesDomainErrorsOfInviting() throws Exception {
		String body = "{\"email\":\"rita@example.com\",\"role\":\"READER\"}";
		doThrow(new InvalidInvitationException("The e-mail is not valid")).when(inviteToDeck).execute(any());
		mockMvc.perform(post(DECK_URL).with(authentication(USER)).with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content(body)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.detail").value("The e-mail is not valid"));

		doThrow(new NotDeckOwnerException(DECK_ID)).when(inviteToDeck).execute(any());
		mockMvc.perform(post(DECK_URL).with(authentication(USER)).with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content(body)).andExpect(status().isForbidden());

		doThrow(new DeckNotFoundException(DECK_ID)).when(inviteToDeck).execute(any());
		mockMvc.perform(post(DECK_URL).with(authentication(USER)).with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content(body)).andExpect(status().isNotFound());
	}

	@Test
	void listsAndCancelsThePendingInvitationsOfADeck() throws Exception {
		when(listDeckInvitations.execute(DECK_ID, USER_ID)).thenReturn(
				List.of(new DeckInvitationView(INVITATION_ID, "Rita", DeckRole.READER, Instant.parse("2026-01-01T10:00:00Z"))));

		mockMvc.perform(get(DECK_URL).with(authentication(USER)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(INVITATION_ID.toString()))
				.andExpect(jsonPath("$[0].inviteeName").value("Rita"))
				.andExpect(jsonPath("$[0].role").value("READER"));
		mockMvc.perform(delete(DECK_URL + "/" + INVITATION_ID).with(authentication(USER)).with(csrf()))
				.andExpect(status().isNoContent());

		verify(cancelInvitation).execute(DECK_ID, INVITATION_ID, USER_ID);
	}

	@Test
	void listsMyPendingInvitations() throws Exception {
		when(listMyInvitations.execute(USER_ID)).thenReturn(List.of(new MyInvitationView(INVITATION_ID, DECK_ID,
				"Anatomia", "Olivia", DeckRole.EDITOR, Instant.parse("2026-01-01T10:00:00Z"))));

		mockMvc.perform(get("/api/invitations").with(authentication(USER)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].deckId").value(DECK_ID.toString()))
				.andExpect(jsonPath("$[0].deckName").value("Anatomia"))
				.andExpect(jsonPath("$[0].inviterName").value("Olivia"))
				.andExpect(jsonPath("$[0].role").value("EDITOR"));
	}

	@Test
	void acceptsAnInvitationAndReturnsTheDeckAndTheRole() throws Exception {
		Deck deck = Deck.create(UUID.randomUUID(), "Anatomia", null, Visibility.PRIVATE);
		when(answerInvitation.accept(INVITATION_ID, USER_ID)).thenReturn(new DeckView(deck, DeckRole.EDITOR));

		mockMvc.perform(post("/api/invitations/" + INVITATION_ID + "/accept").with(authentication(USER)).with(csrf()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.deckId").value(deck.getId().toString()))
				.andExpect(jsonPath("$.role").value("EDITOR"));
	}

	@Test
	void declinesAnInvitation() throws Exception {
		mockMvc.perform(post("/api/invitations/" + INVITATION_ID + "/decline").with(authentication(USER)).with(csrf()))
				.andExpect(status().isNoContent());

		verify(answerInvitation).decline(INVITATION_ID, USER_ID);
	}

	@Test
	void translatesUnknownAndAlreadyAnsweredInvitations() throws Exception {
		doThrow(new InvitationNotFoundException(INVITATION_ID)).when(answerInvitation).decline(any(), any());
		mockMvc.perform(post("/api/invitations/" + INVITATION_ID + "/decline").with(authentication(USER)).with(csrf()))
				.andExpect(status().isNotFound());

		when(answerInvitation.accept(any(), any())).thenThrow(new InvitationNotPendingException(INVITATION_ID));
		mockMvc.perform(post("/api/invitations/" + INVITATION_ID + "/accept").with(authentication(USER)).with(csrf()))
				.andExpect(status().isConflict());
	}

	@Test
	void everyRouteRequiresALoggedUser() throws Exception {
		mockMvc.perform(get("/api/invitations")).andExpect(status().isUnauthorized());
		mockMvc.perform(get(DECK_URL)).andExpect(status().isUnauthorized());
		mockMvc.perform(post("/api/invitations/" + INVITATION_ID + "/accept").with(csrf())).andExpect(status().isUnauthorized());

		verifyNoInteractions(inviteToDeck, listDeckInvitations, cancelInvitation, listMyInvitations, answerInvitation);
	}
}
