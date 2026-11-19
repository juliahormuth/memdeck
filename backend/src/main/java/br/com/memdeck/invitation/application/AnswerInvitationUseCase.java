package br.com.memdeck.invitation.application;

import java.util.UUID;

import br.com.memdeck.deck.application.DeckView;

public interface AnswerInvitationUseCase {

	DeckView accept(UUID invitationId, UUID requesterId);

	void decline(UUID invitationId, UUID requesterId);
}
