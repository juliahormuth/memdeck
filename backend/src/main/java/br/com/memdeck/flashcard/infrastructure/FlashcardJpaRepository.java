package br.com.memdeck.flashcard.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface FlashcardJpaRepository extends JpaRepository<FlashcardJpaModel, UUID> {

	List<FlashcardJpaModel> findByDeckIdOrderByCreatedAtDesc(UUID deckId);
}
