package br.com.memdeck.deck.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import br.com.memdeck.deck.application.DeckMemberGateway;
import br.com.memdeck.deck.domain.DeckMember;

@Component
class DeckMemberGatewayJpa implements DeckMemberGateway {

	private final DeckMemberJpaRepository repository;

	DeckMemberGatewayJpa(DeckMemberJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public DeckMember save(DeckMember member) {
		return toDomain(repository.save(new DeckMemberJpaModel(member.getId(), member.getDeckId(),
				member.getUserId(), member.getRole(), member.getJoinedAt())));
	}

	@Override
	public Optional<DeckMember> findByDeckAndUser(UUID deckId, UUID userId) {
		return repository.findByDeckIdAndUserId(deckId, userId).map(DeckMemberGatewayJpa::toDomain);
	}

	@Override
	public List<DeckMember> findByUser(UUID userId) {
		return repository.findByUserId(userId).stream().map(DeckMemberGatewayJpa::toDomain).toList();
	}

	private static DeckMember toDomain(DeckMemberJpaModel model) {
		return DeckMember.restore(model.getId(), model.getDeckId(), model.getUserId(), model.getRole(),
				model.getJoinedAt());
	}
}
