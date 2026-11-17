package br.com.memdeck.flashcard.presentation;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.memdeck.flashcard.application.CreateFlashcardUseCase;
import br.com.memdeck.flashcard.application.DeleteFlashcardUseCase;
import br.com.memdeck.flashcard.application.ListDeckFlashcardsUseCase;
import br.com.memdeck.flashcard.application.UpdateFlashcardUseCase;
import br.com.memdeck.security.AuthenticatedUser;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/decks/{deckId}/flashcards")
public class FlashcardController {

	private final CreateFlashcardUseCase createFlashcard;

	private final ListDeckFlashcardsUseCase listDeckFlashcards;

	private final UpdateFlashcardUseCase updateFlashcard;
	private final DeleteFlashcardUseCase deleteFlashcard;

	public FlashcardController(CreateFlashcardUseCase createFlashcard, ListDeckFlashcardsUseCase listDeckFlashcards,
			UpdateFlashcardUseCase updateFlashcard, DeleteFlashcardUseCase deleteFlashcard) {
		this.createFlashcard = createFlashcard;
		this.listDeckFlashcards = listDeckFlashcards;
		this.updateFlashcard = updateFlashcard;
		this.deleteFlashcard = deleteFlashcard;
	}

	@GetMapping
	public List<FlashcardResponse> list(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID deckId) {
		return listDeckFlashcards.execute(deckId, user.id()).stream().map(FlashcardResponse::from).toList();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public FlashcardResponse create(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID deckId,
			@Valid @RequestBody CreateFlashcardRequest request) {
		return FlashcardResponse.from(createFlashcard.execute(request.toCommand(user.id(), deckId)));
	}

	@PutMapping("/{flashcardId}")
	public FlashcardResponse update(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID deckId,
			@PathVariable UUID flashcardId, @Valid @RequestBody UpdateFlashcardRequest request) {
		return FlashcardResponse
				.from(updateFlashcard.execute(request.toCommand(user.id(), deckId, flashcardId)));
	}

	@DeleteMapping("/{flashcardId}")
	public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID deckId,
			@PathVariable UUID flashcardId) {
		deleteFlashcard.execute(deckId, flashcardId, user.id());
		return ResponseEntity.noContent().build();
	}
}
