package br.com.memdeck.deck.presentation;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.memdeck.deck.application.LeaveDeckUseCase;
import br.com.memdeck.deck.application.ListDeckMembersUseCase;
import br.com.memdeck.deck.application.RemoveDeckMemberUseCase;
import br.com.memdeck.security.AuthenticatedUser;

@RestController
@RequestMapping("/api/decks/{deckId}/members")
public class DeckMemberController {

	private final ListDeckMembersUseCase listMembers;
	private final RemoveDeckMemberUseCase removeMember;
	private final LeaveDeckUseCase leaveDeck;

	public DeckMemberController(ListDeckMembersUseCase listMembers, RemoveDeckMemberUseCase removeMember,
			LeaveDeckUseCase leaveDeck) {
		this.listMembers = listMembers;
		this.removeMember = removeMember;
		this.leaveDeck = leaveDeck;
	}

	@GetMapping
	public List<DeckMemberResponse> list(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID deckId) {
		return listMembers.execute(deckId, user.id()).stream().map(DeckMemberResponse::from).toList();
	}

	@DeleteMapping("/me")
	public ResponseEntity<Void> leave(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID deckId) {
		leaveDeck.execute(deckId, user.id());
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{userId}")
	public ResponseEntity<Void> remove(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID deckId,
			@PathVariable UUID userId) {
		removeMember.execute(deckId, userId, user.id());
		return ResponseEntity.noContent().build();
	}
}
