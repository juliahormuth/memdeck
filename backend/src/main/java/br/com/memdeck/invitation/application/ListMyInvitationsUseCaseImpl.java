package br.com.memdeck.invitation.application;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.memdeck.deck.application.DeckGateway;
import br.com.memdeck.deck.domain.Deck;
import br.com.memdeck.invitation.domain.Invitation;
import br.com.memdeck.user.application.UserGateway;
import br.com.memdeck.user.domain.User;

@Service
@Transactional(readOnly = true)
public class ListMyInvitationsUseCaseImpl implements ListMyInvitationsUseCase {

	private final DeckGateway decks;
	private final UserGateway users;
	private final InvitationGateway invitations;

	public ListMyInvitationsUseCaseImpl(DeckGateway decks, UserGateway users, InvitationGateway invitations) {
		this.decks = decks;
		this.users = users;
		this.invitations = invitations;
	}

	@Override
	public List<MyInvitationView> execute(UUID userId) {
		List<Invitation> pending = invitations.findPendingByInvitee(userId);
		Map<UUID, Deck> decksById = decks.findAllById(pending.stream().map(Invitation::getDeckId).toList()).stream()
				.collect(Collectors.toMap(Deck::getId, Function.identity()));
		Map<UUID, User> invitersById = users.findAllById(pending.stream().map(Invitation::getInviterId).toList())
				.stream().collect(Collectors.toMap(User::getId, Function.identity()));
		return pending.stream()
				.sorted(Comparator.comparing(Invitation::getCreatedAt).reversed())
				.map(invitation -> new MyInvitationView(invitation.getId(), invitation.getDeckId(),
						decksById.get(invitation.getDeckId()).getName(),
						invitersById.get(invitation.getInviterId()).getName(), invitation.getRole(),
						invitation.getCreatedAt()))
				.toList();
	}
}
