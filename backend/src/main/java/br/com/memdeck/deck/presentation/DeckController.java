package br.com.memdeck.deck.presentation;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.memdeck.deck.application.CreateDeckUseCase;
import br.com.memdeck.deck.application.GetDeckUseCase;
import br.com.memdeck.deck.application.ListMyDecksUseCase;
import br.com.memdeck.deck.domain.Deck;
import br.com.memdeck.security.AuthenticatedUser;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/decks")
public class DeckController {

	private final CreateDeckUseCase createDeck;
	private final GetDeckUseCase getDeck;
	private final ListMyDecksUseCase listMyDecks;

	public DeckController(CreateDeckUseCase createDeck, GetDeckUseCase getDeck, ListMyDecksUseCase listMyDecks) {
		this.createDeck = createDeck;
		this.getDeck = getDeck;
		this.listMyDecks = listMyDecks;
	}

	@PostMapping
	public ResponseEntity<DeckResponse> create(@AuthenticationPrincipal AuthenticatedUser user,
			@Valid @RequestBody CreateDeckRequest request) {
		Deck deck = createDeck.execute(request.toCommand(user.id()));
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(deck.getId())
				.toUri();
		return ResponseEntity.created(location).body(DeckResponse.from(deck));
	}

	@GetMapping
	public List<DeckResponse> listMine(@AuthenticationPrincipal AuthenticatedUser user) {
		return listMyDecks.execute(user.id()).stream().map(DeckResponse::from).toList();
	}

	@GetMapping("/{id}")
	public DeckResponse findById(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID id) {
		return DeckResponse.from(getDeck.execute(id, user.id()));
	}
}
