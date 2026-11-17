package br.com.memdeck.review.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface ReviewScheduleJpaRepository extends JpaRepository<ReviewScheduleJpaModel, UUID> {

	Optional<ReviewScheduleJpaModel> findByUserIdAndFlashcardId(UUID userId, UUID flashcardId);

	// FlashcardJpaModel is referenced by entity name only, so this feature does not import the flashcard infrastructure
	@Query("""
			select s from ReviewScheduleJpaModel s
			where s.userId = :userId
			  and s.flashcardId in (select f.id from FlashcardJpaModel f where f.deckId = :deckId)
			""")
	List<ReviewScheduleJpaModel> findByUserIdAndDeckId(@Param("userId") UUID userId, @Param("deckId") UUID deckId);
}
