package br.com.memdeck.deck.presentation;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.memdeck.deck.application.CreateDeckUseCase;
import br.com.memdeck.deck.application.GetDeckUseCase;
import br.com.memdeck.deck.domain.Deck;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/decks")
@Tag(name = "Decks", description = "Create and retrieve flashcard decks")
public class DeckController {

	private final CreateDeckUseCase createDeck;
	private final GetDeckUseCase getDeck;

	public DeckController(CreateDeckUseCase createDeck, GetDeckUseCase getDeck) {
		this.createDeck = createDeck;
		this.getDeck = getDeck;
	}

	@PostMapping
	@Operation(summary = "Create a deck")
	@ApiResponse(responseCode = "201", description = "Deck created; the Location header points to it")
	@ApiResponse(responseCode = "400", description = "Invalid request (problem details)")
	public ResponseEntity<DeckResponse> create(@Valid @RequestBody CreateDeckRequest request) {
		Deck deck = createDeck.execute(request.toCommand());
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(deck.getId())
				.toUri();
		return ResponseEntity.created(location).body(DeckResponse.from(deck));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get a deck by id")
	@ApiResponse(responseCode = "200", description = "Deck found")
	@ApiResponse(responseCode = "404", description = "Deck not found (problem details)")
	public DeckResponse findById(@PathVariable UUID id) {
		return DeckResponse.from(getDeck.execute(id));
	}
}
