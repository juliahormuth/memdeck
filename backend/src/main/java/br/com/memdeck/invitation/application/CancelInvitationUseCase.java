package br.com.memdeck.invitation.application;

import java.util.UUID;

public interface CancelInvitationUseCase {

	void execute(UUID deckId, UUID invitationId, UUID requesterId);
}
