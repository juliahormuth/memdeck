package br.com.memdeck.deck.application;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.memdeck.deck.domain.DeckMember;
import br.com.memdeck.deck.domain.DeckRole;

@Service
@Transactional(readOnly = true)
public class SearchPublicDecksUseCaseImpl implements SearchPublicDecksUseCase {

	public static final int MAX_RESULTS = 50;
	public static final int MAX_TERM_LENGTH = 100;

	private final DeckGateway decks;
	private final DeckMemberGateway members;

	public SearchPublicDecksUseCaseImpl(DeckGateway decks, DeckMemberGateway members) {
		this.decks = decks;
		this.members = members;
	}

	@Override
	public List<DeckView> execute(UUID requesterId, String term) {
		Map<UUID, DeckRole> myRoles = members.findByUser(requesterId).stream()
				.collect(Collectors.toMap(DeckMember::getDeckId, DeckMember::getRole));
		return decks.searchPublicNewestFirst(requesterId, normalized(term), MAX_RESULTS).stream()
				.map(deck -> new DeckView(deck, myRoles.get(deck.getId())))
				.toList();
	}

	private static String normalized(String term) {
		if (term == null) {
			return "";
		}
		String stripped = term.strip();
		return stripped.length() > MAX_TERM_LENGTH ? stripped.substring(0, MAX_TERM_LENGTH) : stripped;
	}
}
