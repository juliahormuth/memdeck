package br.com.memdeck.friendship.presentation;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.memdeck.friendship.application.AnswerFriendRequestUseCase;
import br.com.memdeck.friendship.application.ListFriendRequestsUseCase;
import br.com.memdeck.friendship.application.ListFriendsUseCase;
import br.com.memdeck.friendship.application.RemoveFriendUseCase;
import br.com.memdeck.friendship.application.SendFriendRequestUseCase;
import br.com.memdeck.security.AuthenticatedUser;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users/me")
public class FriendshipController {

	private final SendFriendRequestUseCase sendFriendRequest;
	private final ListFriendRequestsUseCase listFriendRequests;
	private final AnswerFriendRequestUseCase answerFriendRequest;
	private final ListFriendsUseCase listFriends;
	private final RemoveFriendUseCase removeFriend;

	public FriendshipController(SendFriendRequestUseCase sendFriendRequest,
			ListFriendRequestsUseCase listFriendRequests, AnswerFriendRequestUseCase answerFriendRequest,
			ListFriendsUseCase listFriends, RemoveFriendUseCase removeFriend) {
		this.sendFriendRequest = sendFriendRequest;
		this.listFriendRequests = listFriendRequests;
		this.answerFriendRequest = answerFriendRequest;
		this.listFriends = listFriends;
		this.removeFriend = removeFriend;
	}

	@GetMapping("/friends")
	public List<FriendResponse> friends(@AuthenticationPrincipal AuthenticatedUser user) {
		return listFriends.execute(user.id()).stream().map(FriendResponse::from).toList();
	}

	@DeleteMapping("/friends/{friendId}")
	public ResponseEntity<Void> removeFriend(@AuthenticationPrincipal AuthenticatedUser user,
			@PathVariable UUID friendId) {
		removeFriend.execute(user.id(), friendId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/friend-requests")
	public List<FriendRequestResponse> requests(@AuthenticationPrincipal AuthenticatedUser user) {
		return listFriendRequests.execute(user.id()).stream().map(FriendRequestResponse::from).toList();
	}

	@PostMapping("/friend-requests")
	@ResponseStatus(HttpStatus.ACCEPTED)
	public void send(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody FriendRequestBody body) {
		sendFriendRequest.execute(user.id(), body.email());
	}

	@PostMapping("/friend-requests/{requestId}/accept")
	public ResponseEntity<Void> accept(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID requestId) {
		answerFriendRequest.accept(requestId, user.id());
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/friend-requests/{requestId}/decline")
	public ResponseEntity<Void> decline(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID requestId) {
		answerFriendRequest.decline(requestId, user.id());
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/friend-requests/{requestId}")
	public ResponseEntity<Void> cancel(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID requestId) {
		answerFriendRequest.cancel(requestId, user.id());
		return ResponseEntity.noContent().build();
	}
}
