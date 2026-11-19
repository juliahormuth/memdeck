package br.com.memdeck.invitation.application;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.memdeck.deck.application.DeckAccessFinder;
import br.com.memdeck.invitation.domain.Invitation;
import br.com.memdeck.user.application.UserGateway;
import br.com.memdeck.user.domain.User;

@Service
@Transactional(readOnly = true)
public class ListDeckInvitationsUseCaseImpl implements ListDeckInvitationsUseCase {

	private final DeckAccessFinder access;
	private final UserGateway users;
	private final InvitationGateway invitations;

	public ListDeckInvitationsUseCaseImpl(DeckAccessFinder access, UserGateway users, InvitationGateway invitations) {
		this.access = access;
		this.users = users;
		this.invitations = invitations;
	}

	@Override
	public List<DeckInvitationView> execute(UUID deckId, UUID requesterId) {
		access.owned(deckId, requesterId);
		List<Invitation> pending = invitations.findPendingByDeck(deckId);
		Map<UUID, User> usersById = users.findAllById(pending.stream().map(Invitation::getInviteeId).toList())
				.stream().collect(Collectors.toMap(User::getId, Function.identity()));
		return pending.stream()
				.sorted(Comparator.comparing(Invitation::getCreatedAt).reversed())
				.map(invitation -> new DeckInvitationView(invitation.getId(),
						usersById.get(invitation.getInviteeId()).getName(), invitation.getRole(),
						invitation.getCreatedAt()))
				.toList();
	}
}
