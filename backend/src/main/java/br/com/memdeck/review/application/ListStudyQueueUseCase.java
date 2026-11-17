package br.com.memdeck.review.application;

import java.util.List;
import java.util.UUID;

public interface ListStudyQueueUseCase {

	List<StudyCard> execute(UUID deckId, UUID requesterId);
}
