package br.com.memdeck.deck.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.memdeck.deck.domain.Deck;
import br.com.memdeck.deck.domain.DeckMember;
import br.com.memdeck.deck.domain.DeckMemberNotFoundException;
import br.com.memdeck.deck.domain.DeckNotFoundException;
import br.com.memdeck.deck.domain.DeckRole;
import br.com.memdeck.deck.domain.NotDeckOwnerException;
import br.com.memdeck.deck.domain.OwnerCannotLeaveException;
import br.com.memdeck.deck.domain.Visibility;
import br.com.memdeck.user.application.InMemoryUserGateway;
import br.com.memdeck.user.domain.User;

class DeckMembersUseCasesTest {

	private final InMemoryDeckGateway decks = new InMemoryDeckGateway();
	private final InMemoryDeckMemberGateway members = new InMemoryDeckMemberGateway();
	private final InMemoryUserGateway users = new InMemoryUserGateway();
	private final DeckAccessFinder access = new DeckAccessFinder(decks, members);
	private final ListDeckMembersUseCase list = new ListDeckMembersUseCaseImpl(access, members, users);
	private final RemoveDeckMemberUseCase remove = new RemoveDeckMemberUseCaseImpl(access, members);
	private final LeaveDeckUseCase leave = new LeaveDeckUseCaseImpl(access, members);

	private final User owner = users.save(User.register("Olivia", "olivia", "olivia@example.com", "hash"));
	private final User editor = users.save(User.register("Eduardo", "eduardo", "eduardo@example.com", "hash"));
	private final User reader = users.save(User.register("Rita", "rita", "rita@example.com", "hash"));
	private final Deck deck = decks.save(Deck.create(owner.getId(), "Anatomy", null, Visibility.PRIVATE));

	private void team() {
		members.save(DeckMember.join(deck.getId(), editor.getId(), DeckRole.EDITOR));
		members.save(DeckMember.join(deck.getId(), reader.getId(), DeckRole.READER));
	}

	@Test
	void theOwnerListsTheMembersWithNamesAndRoles() {
		team();

		assertThat(list.execute(deck.getId(), owner.getId()))
				.extracting(DeckMemberView::name, DeckMemberView::role)
				.containsExactlyInAnyOrder(org.assertj.core.groups.Tuple.tuple("Eduardo", DeckRole.EDITOR),
						org.assertj.core.groups.Tuple.tuple("Rita", DeckRole.READER));
	}

	@Test
	void onlyTheOwnerListsTheMembers() {
		team();

		assertThatThrownBy(() -> list.execute(deck.getId(), editor.getId())).isInstanceOf(NotDeckOwnerException.class);
		assertThatThrownBy(() -> list.execute(deck.getId(), UUID.randomUUID())).isInstanceOf(DeckNotFoundException.class);
	}

	@Test
	void theOwnerRemovesAMember() {
		team();

		remove.execute(deck.getId(), reader.getId(), owner.getId());

		assertThat(members.findByDeckAndUser(deck.getId(), reader.getId())).isEmpty();
		assertThat(members.findByDeckAndUser(deck.getId(), editor.getId())).isPresent();
	}

	@Test
	void removingSomeoneWhoIsNotAMemberFails() {
		assertThatThrownBy(() -> remove.execute(deck.getId(), reader.getId(), owner.getId()))
				.isInstanceOf(DeckMemberNotFoundException.class);
	}

	@Test
	void aMemberCannotRemoveAnotherMember() {
		team();

		assertThatThrownBy(() -> remove.execute(deck.getId(), reader.getId(), editor.getId()))
				.isInstanceOf(NotDeckOwnerException.class);
		assertThat(members.size()).isEqualTo(2);
	}

	@Test
	void aMemberLeavesTheDeck() {
		team();

		leave.execute(deck.getId(), editor.getId());

		assertThat(members.findByDeckAndUser(deck.getId(), editor.getId())).isEmpty();
		assertThat(members.size()).isEqualTo(1);
	}

	@Test
	void theOwnerCannotLeaveTheirDeck() {
		assertThatThrownBy(() -> leave.execute(deck.getId(), owner.getId())).isInstanceOf(OwnerCannotLeaveException.class);
	}

	@Test
	void leavingAPublicDeckWithoutBeingAMemberFails() {
		Deck open = decks.save(Deck.create(owner.getId(), "English", null, Visibility.PUBLIC));

		assertThatThrownBy(() -> leave.execute(open.getId(), reader.getId()))
				.isInstanceOf(DeckMemberNotFoundException.class);
	}

	@Test
	void leavingAPrivateDeckWithoutBeingAMemberLooksLikeAMissingDeck() {
		assertThatThrownBy(() -> leave.execute(deck.getId(), reader.getId())).isInstanceOf(DeckNotFoundException.class);
	}
}
