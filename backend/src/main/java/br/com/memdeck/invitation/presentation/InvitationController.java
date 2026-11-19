package br.com.memdeck.invitation.presentation;

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

import br.com.memdeck.invitation.application.AnswerInvitationUseCase;
import br.com.memdeck.invitation.application.CancelInvitationUseCase;
import br.com.memdeck.invitation.application.InviteToDeckUseCase;
import br.com.memdeck.invitation.application.ListDeckInvitationsUseCase;
import br.com.memdeck.invitation.application.ListMyInvitationsUseCase;
import br.com.memdeck.security.AuthenticatedUser;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class InvitationController {

	private final InviteToDeckUseCase inviteToDeck;
	private final ListDeckInvitationsUseCase listDeckInvitations;
	private final CancelInvitationUseCase cancelInvitation;
	private final ListMyInvitationsUseCase listMyInvitations;
	private final AnswerInvitationUseCase answerInvitation;

	public InvitationController(InviteToDeckUseCase inviteToDeck, ListDeckInvitationsUseCase listDeckInvitations,
			CancelInvitationUseCase cancelInvitation, ListMyInvitationsUseCase listMyInvitations,
			AnswerInvitationUseCase answerInvitation) {
		this.inviteToDeck = inviteToDeck;
		this.listDeckInvitations = listDeckInvitations;
		this.cancelInvitation = cancelInvitation;
		this.listMyInvitations = listMyInvitations;
		this.answerInvitation = answerInvitation;
	}

	@PostMapping("/decks/{deckId}/invitations")
	@ResponseStatus(HttpStatus.ACCEPTED)
	public void invite(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID deckId,
			@Valid @RequestBody InviteRequest request) {
		inviteToDeck.execute(request.toCommand(user.id(), deckId));
	}

	@GetMapping("/decks/{deckId}/invitations")
	public List<DeckInvitationResponse> listForDeck(@AuthenticationPrincipal AuthenticatedUser user,
			@PathVariable UUID deckId) {
		return listDeckInvitations.execute(deckId, user.id()).stream().map(DeckInvitationResponse::from).toList();
	}

	@DeleteMapping("/decks/{deckId}/invitations/{invitationId}")
	public ResponseEntity<Void> cancel(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID deckId,
			@PathVariable UUID invitationId) {
		cancelInvitation.execute(deckId, invitationId, user.id());
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/invitations")
	public List<MyInvitationResponse> listMine(@AuthenticationPrincipal AuthenticatedUser user) {
		return listMyInvitations.execute(user.id()).stream().map(MyInvitationResponse::from).toList();
	}

	@PostMapping("/invitations/{invitationId}/accept")
	public AcceptedInvitationResponse accept(@AuthenticationPrincipal AuthenticatedUser user,
			@PathVariable UUID invitationId) {
		return AcceptedInvitationResponse.from(answerInvitation.accept(invitationId, user.id()));
	}

	@PostMapping("/invitations/{invitationId}/decline")
	public ResponseEntity<Void> decline(@AuthenticationPrincipal AuthenticatedUser user,
			@PathVariable UUID invitationId) {
		answerInvitation.decline(invitationId, user.id());
		return ResponseEntity.noContent().build();
	}
}
