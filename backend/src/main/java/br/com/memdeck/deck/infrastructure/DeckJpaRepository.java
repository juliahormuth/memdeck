package br.com.memdeck.deck.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface DeckJpaRepository extends JpaRepository<DeckJpaModel, UUID> {

	List<DeckJpaModel> findByOwnerIdOrderByCreatedAtDesc(UUID ownerId);
}
