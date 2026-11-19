package br.com.memdeck.deck.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.memdeck.deck.application.DeckMemberView;
import br.com.memdeck.deck.application.LeaveDeckUseCase;
import br.com.memdeck.deck.application.ListDeckMembersUseCase;
import br.com.memdeck.deck.application.RemoveDeckMemberUseCase;
import br.com.memdeck.deck.domain.DeckMemberNotFoundException;
import br.com.memdeck.deck.domain.DeckNotFoundException;
import br.com.memdeck.deck.domain.DeckRole;
import br.com.memdeck.deck.domain.NotDeckOwnerException;
import br.com.memdeck.deck.domain.OwnerCannotLeaveException;
import br.com.memdeck.security.AuthenticatedUser;
import br.com.memdeck.security.SecurityConfig;

@WebMvcTest(DeckMemberController.class)
@Import(SecurityConfig.class)
class DeckMemberControllerTest {

	private static final UUID USER_ID = UUID.randomUUID();
	private static final UUID DECK_ID = UUID.randomUUID();
	private static final UUID MEMBER_ID = UUID.randomUUID();
	private static final String URL = "/api/decks/" + DECK_ID + "/members";
	private static final Authentication USER = UsernamePasswordAuthenticationToken.authenticated(
			new AuthenticatedUser(USER_ID, "julia@example.com"), null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

	@Autowired
	MockMvc mockMvc;

	@MockitoBean
	ListDeckMembersUseCase listMembers;

	@MockitoBean
	RemoveDeckMemberUseCase removeMember;

	@MockitoBean
	LeaveDeckUseCase leaveDeck;

	@Test
	void listsTheMembers() throws Exception {
		when(listMembers.execute(DECK_ID, USER_ID)).thenReturn(
				List.of(new DeckMemberView(MEMBER_ID, "Rita", DeckRole.READER, Instant.parse("2026-01-01T10:00:00Z"))));

		mockMvc.perform(get(URL).with(authentication(USER)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].userId").value(MEMBER_ID.toString()))
				.andExpect(jsonPath("$[0].name").value("Rita"))
				.andExpect(jsonPath("$[0].role").value("READER"));
	}

	@Test
	void translatesAMissingDeckTo404AndANonOwnerTo403() throws Exception {
		UUID other = UUID.randomUUID();
		when(listMembers.execute(DECK_ID, USER_ID)).thenThrow(new NotDeckOwnerException(DECK_ID));
		when(listMembers.execute(other, USER_ID)).thenThrow(new DeckNotFoundException(other));

		mockMvc.perform(get(URL).with(authentication(USER))).andExpect(status().isForbidden());
		mockMvc.perform(get("/api/decks/" + other + "/members").with(authentication(USER))).andExpect(status().isNotFound());
	}

	@Test
	void theOwnerRemovesAMember() throws Exception {
		mockMvc.perform(delete(URL + "/" + MEMBER_ID).with(authentication(USER)).with(csrf()))
				.andExpect(status().isNoContent());

		verify(removeMember).execute(DECK_ID, MEMBER_ID, USER_ID);
		verifyNoInteractions(leaveDeck);
	}

	@Test
	void removingSomeoneWhoIsNotAMemberIs404() throws Exception {
		doThrow(new DeckMemberNotFoundException(DECK_ID, MEMBER_ID)).when(removeMember).execute(any(), any(), any());

		mockMvc.perform(delete(URL + "/" + MEMBER_ID).with(authentication(USER)).with(csrf()))
				.andExpect(status().isNotFound());
	}

	@Test
	void leavingUsesTheLoggedUserAndDoesNotTouchTheRemoveRoute() throws Exception {
		mockMvc.perform(delete(URL + "/me").with(authentication(USER)).with(csrf())).andExpect(status().isNoContent());

		verify(leaveDeck).execute(DECK_ID, USER_ID);
		verifyNoInteractions(removeMember);
	}

	@Test
	void theOwnerCannotLeaveAndGetsA409() throws Exception {
		doThrow(new OwnerCannotLeaveException(DECK_ID)).when(leaveDeck).execute(any(), any());

		mockMvc.perform(delete(URL + "/me").with(authentication(USER)).with(csrf())).andExpect(status().isConflict());
	}

	@Test
	void everyRouteRequiresALoggedUser() throws Exception {
		mockMvc.perform(get(URL)).andExpect(status().isUnauthorized());
		mockMvc.perform(delete(URL + "/me").with(csrf())).andExpect(status().isUnauthorized());

		verifyNoInteractions(listMembers, removeMember, leaveDeck);
	}
}
