package br.com.memdeck.deck.application;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.memdeck.deck.domain.DeckMember;
import br.com.memdeck.deck.domain.DeckRole;

@Service
@Transactional(readOnly = true)
public class ListMyDecksUseCaseImpl implements ListMyDecksUseCase {

	private final DeckGateway gateway;
	private final DeckMemberGateway members;

	public ListMyDecksUseCaseImpl(DeckGateway gateway, DeckMemberGateway members) {
		this.gateway = gateway;
		this.members = members;
	}

	@Override
	public List<DeckView> execute(UUID userId) {
		Map<UUID, DeckRole> joinedRoles = members.findByUser(userId).stream()
				.collect(Collectors.toMap(DeckMember::getDeckId, DeckMember::getRole));
		Stream<DeckView> joined = gateway.findAllById(joinedRoles.keySet()).stream()
				.map(deck -> new DeckView(deck, joinedRoles.get(deck.getId())));
		Stream<DeckView> owned = gateway.findByOwnerNewestFirst(userId).stream().map(DeckView::ownedBy);
		return Stream.concat(owned, joined)
				.sorted(Comparator.comparing((DeckView view) -> view.deck().getCreatedAt()).reversed())
				.toList();
	}
}
