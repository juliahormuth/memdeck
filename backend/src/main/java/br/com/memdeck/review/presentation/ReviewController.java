package br.com.memdeck.review.presentation;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.memdeck.review.application.ListStudyQueueUseCase;
import br.com.memdeck.review.application.RecordReviewUseCase;
import br.com.memdeck.security.AuthenticatedUser;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/decks/{deckId}")
public class ReviewController {

	private final ListStudyQueueUseCase listStudyQueue;
	private final RecordReviewUseCase recordReview;

	public ReviewController(ListStudyQueueUseCase listStudyQueue, RecordReviewUseCase recordReview) {
		this.listStudyQueue = listStudyQueue;
		this.recordReview = recordReview;
	}

	@GetMapping("/study")
	public List<StudyCardResponse> studyQueue(@AuthenticationPrincipal AuthenticatedUser user,
			@PathVariable UUID deckId) {
		return listStudyQueue.execute(deckId, user.id()).stream().map(StudyCardResponse::from).toList();
	}

	@PostMapping("/flashcards/{flashcardId}/reviews")
	public ReviewResponse review(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID deckId,
			@PathVariable UUID flashcardId, @Valid @RequestBody RecordReviewRequest request) {
		return ReviewResponse.from(recordReview.execute(request.toCommand(user.id(), deckId, flashcardId)));
	}
}
