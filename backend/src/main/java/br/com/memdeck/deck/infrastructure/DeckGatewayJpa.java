package br.com.memdeck.deck.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import br.com.memdeck.deck.application.DeckGateway;
import br.com.memdeck.deck.domain.Deck;

@Component
class DeckGatewayJpa implements DeckGateway {

	private final DeckJpaRepository repository;

	DeckGatewayJpa(DeckJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public Deck save(Deck deck) {
		return DeckMapper.toDomain(repository.save(DeckMapper.toModel(deck)));
	}

	@Override
	public Optional<Deck> findById(UUID id) {
		return repository.findById(id).map(DeckMapper::toDomain);
	}

	@Override
	public List<Deck> findByOwnerNewestFirst(UUID ownerId) {
		return repository.findByOwnerIdOrderByCreatedAtDesc(ownerId).stream().map(DeckMapper::toDomain).toList();
	}
}
