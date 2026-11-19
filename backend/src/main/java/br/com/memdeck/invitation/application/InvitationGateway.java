package br.com.memdeck.invitation.application;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.memdeck.invitation.domain.Invitation;

public interface InvitationGateway {

	Invitation save(Invitation invitation);

	Optional<Invitation> findById(UUID id);

	void deleteById(UUID id);

	List<Invitation> findPendingByDeck(UUID deckId);

	List<Invitation> findPendingByInvitee(UUID inviteeId);

	Optional<Invitation> findPendingByDeckAndInvitee(UUID deckId, UUID inviteeId);
}
