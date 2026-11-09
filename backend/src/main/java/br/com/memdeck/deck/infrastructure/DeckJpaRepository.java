package br.com.memdeck.deck.infrastructure;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface DeckJpaRepository extends JpaRepository<DeckJpaModel, UUID> {
}
