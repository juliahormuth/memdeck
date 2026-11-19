package br.com.memdeck.invitation.application;

import java.util.List;
import java.util.UUID;

public interface ListDeckInvitationsUseCase {

	List<DeckInvitationView> execute(UUID deckId, UUID requesterId);
}
