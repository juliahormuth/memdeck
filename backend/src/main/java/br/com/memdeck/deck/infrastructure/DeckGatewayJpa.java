package br.com.memdeck.deck.infrastructure;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import br.com.memdeck.deck.application.DeckGateway;
import br.com.memdeck.deck.domain.Deck;
import br.com.memdeck.deck.domain.Visibility;

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
	public void deleteById(UUID id) {
		repository.deleteById(id);
	}

	@Override
	public List<Deck> findByOwnerNewestFirst(UUID ownerId) {
		return repository.findByOwnerIdOrderByCreatedAtDesc(ownerId).stream().map(DeckMapper::toDomain).toList();
	}

	@Override
	public List<Deck> findAllById(Collection<UUID> ids) {
		return repository.findAllById(ids).stream().map(DeckMapper::toDomain).toList();
	}

	@Override
	public List<Deck> searchPublicNewestFirst(UUID excludedOwnerId, String term, int limit) {
		String escaped = term.toLowerCase(Locale.ROOT).replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
		return repository.searchByVisibility(Visibility.PUBLIC, excludedOwnerId, "%" + escaped + "%", PageRequest.of(0, limit))
				.stream().map(DeckMapper::toDomain).toList();
	}
}
