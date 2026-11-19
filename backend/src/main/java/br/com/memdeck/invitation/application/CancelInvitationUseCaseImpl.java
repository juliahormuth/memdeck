package br.com.memdeck.invitation.application;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.memdeck.deck.application.DeckAccessFinder;
import br.com.memdeck.invitation.domain.InvitationNotFoundException;

@Service
@Transactional
public class CancelInvitationUseCaseImpl implements CancelInvitationUseCase {

	private final DeckAccessFinder access;
	private final InvitationGateway invitations;

	public CancelInvitationUseCaseImpl(DeckAccessFinder access, InvitationGateway invitations) {
		this.access = access;
		this.invitations = invitations;
	}

	@Override
	public void execute(UUID deckId, UUID invitationId, UUID requesterId) {
		access.owned(deckId, requesterId);
		invitations.findById(invitationId)
				.filter(invitation -> invitation.getDeckId().equals(deckId) && invitation.isPending())
				.orElseThrow(() -> new InvitationNotFoundException(invitationId));
		invitations.deleteById(invitationId);
	}
}
