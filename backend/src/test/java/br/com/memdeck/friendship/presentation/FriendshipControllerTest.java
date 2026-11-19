package br.com.memdeck.friendship.presentation;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.memdeck.friendship.application.AnswerFriendRequestUseCase;
import br.com.memdeck.friendship.application.FriendRequestView;
import br.com.memdeck.friendship.application.FriendView;
import br.com.memdeck.friendship.application.ListFriendRequestsUseCase;
import br.com.memdeck.friendship.application.ListFriendsUseCase;
import br.com.memdeck.friendship.application.RemoveFriendUseCase;
import br.com.memdeck.friendship.application.SendFriendRequestUseCase;
import br.com.memdeck.friendship.domain.FriendshipNotFoundException;
import br.com.memdeck.friendship.domain.InvalidFriendRequestException;
import br.com.memdeck.security.AuthenticatedUser;
import br.com.memdeck.security.SecurityConfig;
import br.com.memdeck.user.domain.Avatar;

@WebMvcTest(FriendshipController.class)
@Import(SecurityConfig.class)
class FriendshipControllerTest {

	private static final UUID USER_ID = UUID.randomUUID();
	private static final UUID OTHER_ID = UUID.randomUUID();
	private static final UUID REQUEST_ID = UUID.randomUUID();
	private static final Authentication USER = UsernamePasswordAuthenticationToken.authenticated(
			new AuthenticatedUser(USER_ID, "julia@example.com"), null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

	@Autowired
	MockMvc mockMvc;

	@MockitoBean
	SendFriendRequestUseCase sendFriendRequest;

	@MockitoBean
	ListFriendRequestsUseCase listFriendRequests;

	@MockitoBean
	AnswerFriendRequestUseCase answerFriendRequest;

	@MockitoBean
	ListFriendsUseCase listFriends;

	@MockitoBean
	RemoveFriendUseCase removeFriend;

	@Test
	void listsMyFriendsWithoutEmails() throws Exception {
		when(listFriends.execute(USER_ID)).thenReturn(
				List.of(new FriendView(OTHER_ID, "Bruno", Avatar.STAR, Instant.parse("2026-01-01T10:00:00Z"))));

		mockMvc.perform(get("/api/users/me/friends").with(authentication(USER)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].userId").value(OTHER_ID.toString()))
				.andExpect(jsonPath("$[0].name").value("Bruno"))
				.andExpect(jsonPath("$[0].avatar").value("STAR"))
				.andExpect(jsonPath("$[0].email").doesNotExist());
	}

	@Test
	void listsTheFriendRequestsWithTheirDirection() throws Exception {
		when(listFriendRequests.execute(USER_ID)).thenReturn(List.of(new FriendRequestView(REQUEST_ID,
				FriendRequestView.Direction.INCOMING, OTHER_ID, "Bruno", Avatar.BOOK, Instant.parse("2026-01-01T10:00:00Z"))));

		mockMvc.perform(get("/api/users/me/friend-requests").with(authentication(USER)))
				.andExpect(jsonPath("$[0].id").value(REQUEST_ID.toString()))
				.andExpect(jsonPath("$[0].direction").value("INCOMING"))
				.andExpect(jsonPath("$[0].name").value("Bruno"));
	}

	@Test
	void sendsARequestWithAnEmpty202() throws Exception {
		mockMvc.perform(post("/api/users/me/friend-requests").with(authentication(USER)).with(csrf())
				.contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"bruno@example.com\"}"))
				.andExpect(status().isAccepted()).andExpect(content().string(""));

		verify(sendFriendRequest).execute(USER_ID, "bruno@example.com");
	}

	@Test
	void rejectsABlankEmailAndTranslatesAnInvalidRequestTo400() throws Exception {
		mockMvc.perform(post("/api/users/me/friend-requests").with(authentication(USER)).with(csrf())
				.contentType(MediaType.APPLICATION_JSON).content("{\"email\":\" \"}")).andExpect(status().isBadRequest());
		verifyNoInteractions(sendFriendRequest);

		doThrow(new InvalidFriendRequestException("You cannot send a friend request to yourself"))
				.when(sendFriendRequest).execute(any(), any());
		mockMvc.perform(post("/api/users/me/friend-requests").with(authentication(USER)).with(csrf())
				.contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"julia@example.com\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void acceptsDeclinesAndCancelsOnBehalfOfTheLoggedUser() throws Exception {
		String base = "/api/users/me/friend-requests/" + REQUEST_ID;

		mockMvc.perform(post(base + "/accept").with(authentication(USER)).with(csrf())).andExpect(status().isNoContent());
		mockMvc.perform(post(base + "/decline").with(authentication(USER)).with(csrf())).andExpect(status().isNoContent());
		mockMvc.perform(delete(base).with(authentication(USER)).with(csrf())).andExpect(status().isNoContent());

		verify(answerFriendRequest).accept(REQUEST_ID, USER_ID);
		verify(answerFriendRequest).decline(REQUEST_ID, USER_ID);
		verify(answerFriendRequest).cancel(REQUEST_ID, USER_ID);
	}

	@Test
	void translatesAMissingRequestOrFriendTo404() throws Exception {
		doThrow(new FriendshipNotFoundException(REQUEST_ID)).when(answerFriendRequest).accept(any(), any());
		doThrow(new FriendshipNotFoundException(OTHER_ID)).when(removeFriend).execute(any(), any());

		mockMvc.perform(post("/api/users/me/friend-requests/" + REQUEST_ID + "/accept").with(authentication(USER)).with(csrf()))
				.andExpect(status().isNotFound());
		mockMvc.perform(delete("/api/users/me/friends/" + OTHER_ID).with(authentication(USER)).with(csrf()))
				.andExpect(status().isNotFound());
	}

	@Test
	void removesAFriend() throws Exception {
		mockMvc.perform(delete("/api/users/me/friends/" + OTHER_ID).with(authentication(USER)).with(csrf()))
				.andExpect(status().isNoContent());

		verify(removeFriend).execute(USER_ID, OTHER_ID);
	}

	@Test
	void everyRouteRequiresALoggedUser() throws Exception {
		mockMvc.perform(get("/api/users/me/friends")).andExpect(status().isUnauthorized());
		mockMvc.perform(get("/api/users/me/friend-requests")).andExpect(status().isUnauthorized());
		mockMvc.perform(post("/api/users/me/friend-requests/" + REQUEST_ID + "/accept").with(csrf()))
				.andExpect(status().isUnauthorized());

		verifyNoInteractions(sendFriendRequest, listFriendRequests, answerFriendRequest, listFriends, removeFriend);
	}
}
