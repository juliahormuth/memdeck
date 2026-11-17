package br.com.memdeck.flashcard.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import br.com.memdeck.flashcard.application.FlashcardGateway;
import br.com.memdeck.flashcard.domain.Flashcard;

@Component
class FlashcardGatewayJpa implements FlashcardGateway {

	private final FlashcardJpaRepository repository;

	FlashcardGatewayJpa(FlashcardJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public Flashcard save(Flashcard flashcard) {
		return FlashcardMapper.toDomain(repository.save(FlashcardMapper.toModel(flashcard)));
	}

	@Override
	public Optional<Flashcard> findById(UUID id) {
		return repository.findById(id).map(FlashcardMapper::toDomain);
	}

	@Override
	public void deleteById(UUID id) {
		repository.deleteById(id);
	}

	@Override
	public List<Flashcard> findByDeckNewestFirst(UUID deckId) {
		return repository.findByDeckIdOrderByCreatedAtDesc(deckId).stream().map(FlashcardMapper::toDomain).toList();
	}
}
