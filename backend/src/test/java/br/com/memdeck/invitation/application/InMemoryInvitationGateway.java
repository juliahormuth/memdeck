package br.com.memdeck.invitation.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.memdeck.invitation.domain.Invitation;

class InMemoryInvitationGateway implements InvitationGateway {

	private final List<Invitation> store = new ArrayList<>();

	@Override
	public Invitation save(Invitation invitation) {
		store.removeIf(existing -> existing.getId().equals(invitation.getId()));
		store.add(invitation);
		return invitation;
	}

	@Override
	public Optional<Invitation> findById(UUID id) {
		return store.stream().filter(invitation -> invitation.getId().equals(id)).findFirst();
	}

	@Override
	public void deleteById(UUID id) {
		store.removeIf(invitation -> invitation.getId().equals(id));
	}

	@Override
	public List<Invitation> findPendingByDeck(UUID deckId) {
		return store.stream().filter(i -> i.getDeckId().equals(deckId) && i.isPending()).toList();
	}

	@Override
	public List<Invitation> findPendingByInvitee(UUID inviteeId) {
		return store.stream().filter(i -> i.getInviteeId().equals(inviteeId) && i.isPending()).toList();
	}

	@Override
	public Optional<Invitation> findPendingByDeckAndInvitee(UUID deckId, UUID inviteeId) {
		return store.stream()
				.filter(i -> i.getDeckId().equals(deckId) && i.getInviteeId().equals(inviteeId) && i.isPending())
				.findFirst();
	}

	int size() {
		return store.size();
	}
}
