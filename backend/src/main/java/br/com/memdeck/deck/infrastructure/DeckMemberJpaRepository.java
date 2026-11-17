package br.com.memdeck.deck.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface DeckMemberJpaRepository extends JpaRepository<DeckMemberJpaModel, UUID> {

	Optional<DeckMemberJpaModel> findByDeckIdAndUserId(UUID deckId, UUID userId);

	List<DeckMemberJpaModel> findByUserId(UUID userId);
}
