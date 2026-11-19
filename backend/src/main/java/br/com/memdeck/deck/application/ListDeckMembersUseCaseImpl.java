package br.com.memdeck.deck.application;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.memdeck.deck.domain.DeckMember;
import br.com.memdeck.user.application.UserGateway;
import br.com.memdeck.user.domain.User;

@Service
@Transactional(readOnly = true)
public class ListDeckMembersUseCaseImpl implements ListDeckMembersUseCase {

	private final DeckAccessFinder access;
	private final DeckMemberGateway members;
	private final UserGateway users;

	public ListDeckMembersUseCaseImpl(DeckAccessFinder access, DeckMemberGateway members, UserGateway users) {
		this.access = access;
		this.members = members;
		this.users = users;
	}

	@Override
	public List<DeckMemberView> execute(UUID deckId, UUID requesterId) {
		access.owned(deckId, requesterId);
		List<DeckMember> found = members.findByDeck(deckId);
		Map<UUID, User> usersById = users.findAllById(found.stream().map(DeckMember::getUserId).toList()).stream()
				.collect(Collectors.toMap(User::getId, Function.identity()));
		return found.stream()
				.map(member -> new DeckMemberView(member.getUserId(), usersById.get(member.getUserId()).getName(),
						member.getRole(), member.getJoinedAt()))
				.toList();
	}
}
