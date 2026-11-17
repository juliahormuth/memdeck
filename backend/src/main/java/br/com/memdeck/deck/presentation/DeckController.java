package br.com.memdeck.deck.presentation;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.memdeck.deck.application.CreateDeckUseCase;
import br.com.memdeck.deck.application.DeleteDeckUseCase;
import br.com.memdeck.deck.application.GetDeckUseCase;
import br.com.memdeck.deck.application.JoinDeckUseCase;
import br.com.memdeck.deck.application.ListMyDecksUseCase;
import br.com.memdeck.deck.application.SearchPublicDecksUseCase;
import br.com.memdeck.deck.application.UpdateDeckUseCase;
import br.com.memdeck.deck.domain.Deck;
import br.com.memdeck.security.AuthenticatedUser;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/decks")
public class DeckController {

	private final CreateDeckUseCase createDeck;
	private final GetDeckUseCase getDeck;
	private final ListMyDecksUseCase listMyDecks;

	private final UpdateDeckUseCase updateDeck;
	private final DeleteDeckUseCase deleteDeck;
	private final SearchPublicDecksUseCase searchPublicDecks;
	private final JoinDeckUseCase joinDeck;

	public DeckController(CreateDeckUseCase createDeck, GetDeckUseCase getDeck, ListMyDecksUseCase listMyDecks,
			UpdateDeckUseCase updateDeck, DeleteDeckUseCase deleteDeck, SearchPublicDecksUseCase searchPublicDecks,
			JoinDeckUseCase joinDeck) {
		this.createDeck = createDeck;
		this.getDeck = getDeck;
		this.listMyDecks = listMyDecks;
		this.updateDeck = updateDeck;
		this.deleteDeck = deleteDeck;
		this.searchPublicDecks = searchPublicDecks;
		this.joinDeck = joinDeck;
	}

	@PostMapping
	public ResponseEntity<DeckResponse> create(@AuthenticationPrincipal AuthenticatedUser user,
			@Valid @RequestBody CreateDeckRequest request) {
		Deck deck = createDeck.execute(request.toCommand(user.id()));
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(deck.getId())
				.toUri();
		return ResponseEntity.created(location).body(DeckResponse.ownedBy(deck));
	}

	@GetMapping
	public List<DeckResponse> listMine(@AuthenticationPrincipal AuthenticatedUser user) {
		return listMyDecks.execute(user.id()).stream().map(DeckResponse::from).toList();
	}

	@GetMapping("/public")
	public List<DeckResponse> searchPublic(@AuthenticationPrincipal AuthenticatedUser user,
			@RequestParam(defaultValue = "") String search) {
		return searchPublicDecks.execute(user.id(), search).stream().map(DeckResponse::from).toList();
	}

	@PostMapping("/{id}/members")
	@ResponseStatus(HttpStatus.CREATED)
	public DeckResponse join(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID id) {
		return DeckResponse.from(joinDeck.execute(id, user.id()));
	}

	@GetMapping("/{id}")
	public DeckResponse findById(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID id) {
		return DeckResponse.from(getDeck.execute(id, user.id()));
	}

	@PutMapping("/{id}")
	public DeckResponse update(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID id,
			@Valid @RequestBody UpdateDeckRequest request) {
		return DeckResponse.ownedBy(updateDeck.execute(request.toCommand(user.id(), id)));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID id) {
		deleteDeck.execute(id, user.id());
		return ResponseEntity.noContent().build();
	}
}
