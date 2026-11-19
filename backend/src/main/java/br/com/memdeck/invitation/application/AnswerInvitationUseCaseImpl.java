package br.com.memdeck.invitation.application;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.memdeck.deck.application.DeckGateway;
import br.com.memdeck.deck.application.DeckMemberGateway;
import br.com.memdeck.deck.application.DeckView;
import br.com.memdeck.deck.domain.Deck;
import br.com.memdeck.deck.domain.DeckMember;
import br.com.memdeck.deck.domain.DeckNotFoundException;
import br.com.memdeck.invitation.domain.Invitation;
import br.com.memdeck.invitation.domain.InvitationNotFoundException;

@Service
@Transactional
public class AnswerInvitationUseCaseImpl implements AnswerInvitationUseCase {

	private final InvitationGateway invitations;
	private final DeckGateway decks;
	private final DeckMemberGateway members;

	public AnswerInvitationUseCaseImpl(InvitationGateway invitations, DeckGateway decks, DeckMemberGateway members) {
		this.invitations = invitations;
		this.decks = decks;
		this.members = members;
	}

	@Override
	public DeckView accept(UUID invitationId, UUID requesterId) {
		Invitation invitation = findMine(invitationId, requesterId);
		Deck deck = decks.findById(invitation.getDeckId())
				.orElseThrow(() -> new DeckNotFoundException(invitation.getDeckId()));
		DeckMember member = members.findByDeckAndUser(deck.getId(), requesterId)
				.map(existing -> existing.withRole(invitation.getRole()))
				.orElseGet(() -> DeckMember.join(deck.getId(), requesterId, invitation.getRole()));
		members.save(member);
		invitations.save(invitation.accept());
		return new DeckView(deck, member.getRole());
	}

	@Override
	public void decline(UUID invitationId, UUID requesterId) {
		invitations.save(findMine(invitationId, requesterId).decline());
	}

	private Invitation findMine(UUID invitationId, UUID requesterId) {
		return invitations.findById(invitationId)
				.filter(invitation -> invitation.getInviteeId().equals(requesterId))
				.orElseThrow(() -> new InvitationNotFoundException(invitationId));
	}
}
