package br.com.memdeck.review.application;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.memdeck.deck.application.DeckAccess;
import br.com.memdeck.deck.application.DeckAccessFinder;
import br.com.memdeck.deck.application.DeckMemberGateway;
import br.com.memdeck.deck.domain.DeckMember;
import br.com.memdeck.deck.domain.NotDeckParticipantException;
import br.com.memdeck.user.application.UserGateway;
import br.com.memdeck.user.domain.User;

@Service
@Transactional(readOnly = true)
public class GetDeckRankingUseCaseImpl implements GetDeckRankingUseCase {

	private final DeckAccessFinder access;
	private final DeckMemberGateway members;
	private final UserGateway users;
	private final ReviewLogGateway logs;

	public GetDeckRankingUseCaseImpl(DeckAccessFinder access, DeckMemberGateway members, UserGateway users,
			ReviewLogGateway logs) {
		this.access = access;
		this.members = members;
		this.users = users;
		this.logs = logs;
	}

	@Override
	public List<RankingEntry> execute(UUID deckId, UUID requesterId) {
		DeckAccess found = access.viewable(deckId, requesterId);
		if (found.role() == null) {
			throw new NotDeckParticipantException(deckId);
		}
		Set<UUID> participantIds = new HashSet<>(members.findByDeck(deckId).stream().map(DeckMember::getUserId).toList());
		participantIds.add(found.deck().getOwnerId());
		Map<UUID, Long> counts = logs.countReviewedByUser(deckId);
		List<User> participants = users.findAllById(participantIds).stream()
				.sorted(Comparator.comparing((User user) -> counts.getOrDefault(user.getId(), 0L)).reversed()
						.thenComparing(User::getName, String.CASE_INSENSITIVE_ORDER))
				.toList();
		List<RankingEntry> ranking = new ArrayList<>();
		for (int index = 0; index < participants.size(); index++) {
			User user = participants.get(index);
			long count = counts.getOrDefault(user.getId(), 0L);
			boolean tiesWithPrevious = index > 0 && ranking.get(index - 1).reviewedFlashcards() == count;
			int position = tiesWithPrevious ? ranking.get(index - 1).position() : index + 1;
			ranking.add(new RankingEntry(position, user.getId(), user.getName(), user.getAvatar(), count));
		}
		return ranking;
	}
}
