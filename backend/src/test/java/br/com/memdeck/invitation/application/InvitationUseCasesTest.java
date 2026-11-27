package br.com.memdeck.invitation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.memdeck.deck.application.DeckAccessFinder;
import br.com.memdeck.deck.application.DeckView;
import br.com.memdeck.deck.application.InMemoryDeckGateway;
import br.com.memdeck.deck.application.InMemoryDeckMemberGateway;
import br.com.memdeck.deck.domain.Deck;
import br.com.memdeck.deck.domain.DeckMember;
import br.com.memdeck.deck.domain.DeckNotFoundException;
import br.com.memdeck.deck.domain.DeckRole;
import br.com.memdeck.deck.domain.NotDeckOwnerException;
import br.com.memdeck.deck.domain.Visibility;
import br.com.memdeck.invitation.domain.InvalidInvitationException;
import br.com.memdeck.invitation.domain.Invitation;
import br.com.memdeck.invitation.domain.InvitationNotFoundException;
import br.com.memdeck.invitation.domain.InvitationNotPendingException;
import br.com.memdeck.user.application.InMemoryUserGateway;
import br.com.memdeck.user.domain.User;

class InvitationUseCasesTest {

	private final InMemoryDeckGateway decks = new InMemoryDeckGateway();
	private final InMemoryDeckMemberGateway members = new InMemoryDeckMemberGateway();
	private final InMemoryUserGateway users = new InMemoryUserGateway();
	private final InMemoryInvitationGateway invitations = new InMemoryInvitationGateway();
	private final DeckAccessFinder access = new DeckAccessFinder(decks, members);

	private final InviteToDeckUseCase invite = new InviteToDeckUseCaseImpl(access, members, users, invitations);
	private final ListDeckInvitationsUseCase listForDeck = new ListDeckInvitationsUseCaseImpl(access, users, invitations);
	private final CancelInvitationUseCase cancel = new CancelInvitationUseCaseImpl(access, invitations);
	private final ListMyInvitationsUseCase listMine = new ListMyInvitationsUseCaseImpl(decks, users, invitations);
	private final AnswerInvitationUseCase answer = new AnswerInvitationUseCaseImpl(invitations, decks, members);

	private final User owner = users.save(User.register("Olivia", "olivia", "olivia@example.com", "hash"));
	private final User guest = users.save(User.register("Gabriel", "gabriel", "gabriel@example.com", "hash"));
	private final User other = users.save(User.register("Otto", "otto", "otto@example.com", "hash"));
	private final Deck deck = decks.save(Deck.create(owner.getId(), "Anatomy", null, Visibility.PRIVATE));

	private void inviteGuest(DeckRole role) {
		invite.execute(new InviteToDeckCommand(owner.getId(), deck.getId(), "gabriel", role));
	}

	@Test
	void theOwnerInvitesARegisteredUserByEmailIgnoringCaseAndSpaces() {
		invite.execute(new InviteToDeckCommand(owner.getId(), deck.getId(), "  Gabriel ", DeckRole.EDITOR));

		List<Invitation> pending = invitations.findPendingByInvitee(guest.getId());
		assertThat(pending).hasSize(1);
		assertThat(pending.get(0).getRole()).isEqualTo(DeckRole.EDITOR);
		assertThat(pending.get(0).getInviterId()).isEqualTo(owner.getId());
	}

	@Test
	void anUnknownUsernameEndsSilentlyWithoutRevealingThatThereIsNoAccount() {
		invite.execute(new InviteToDeckCommand(owner.getId(), deck.getId(), "nobody", DeckRole.READER));

		assertThat(invitations.size()).isZero();
	}

	@Test
	void aMalformedEmailOrTheOwnerRoleIsRejectedBeforeAnyLookup() {
		assertThatThrownBy(() -> invite.execute(new InviteToDeckCommand(owner.getId(), deck.getId(), "not a username", DeckRole.READER)))
				.isInstanceOf(InvalidInvitationException.class);
		assertThatThrownBy(() -> invite.execute(new InviteToDeckCommand(owner.getId(), deck.getId(), "nobody", DeckRole.OWNER)))
				.isInstanceOf(InvalidInvitationException.class);
	}

	@Test
	void theOwnerCannotInviteTheirOwnEmail() {
		assertThatThrownBy(() -> invite.execute(new InviteToDeckCommand(owner.getId(), deck.getId(), "olivia", DeckRole.READER)))
				.isInstanceOf(InvalidInvitationException.class);
	}

	@Test
	void onlyTheOwnerInvites() {
		members.save(DeckMember.join(deck.getId(), guest.getId(), DeckRole.EDITOR));

		assertThatThrownBy(() -> invite.execute(new InviteToDeckCommand(guest.getId(), deck.getId(), "otto", DeckRole.READER)))
				.isInstanceOf(NotDeckOwnerException.class);
		assertThatThrownBy(() -> invite.execute(new InviteToDeckCommand(other.getId(), deck.getId(), "otto", DeckRole.READER)))
				.isInstanceOf(DeckNotFoundException.class);
	}

	@Test
	void repeatedInvitationsAndInvitationsToTheCurrentRoleEndSilently() {
		inviteGuest(DeckRole.READER);
		inviteGuest(DeckRole.EDITOR);
		assertThat(invitations.size()).isEqualTo(1);

		Invitation pending = invitations.findPendingByInvitee(guest.getId()).get(0);
		answer.accept(pending.getId(), guest.getId());
		inviteGuest(DeckRole.READER);

		assertThat(invitations.findPendingByInvitee(guest.getId())).isEmpty();
	}

	@Test
	void aReaderCanBeInvitedToBecomeAnEditor() {
		members.save(DeckMember.joinAsReader(deck.getId(), guest.getId()));

		inviteGuest(DeckRole.EDITOR);

		assertThat(invitations.findPendingByInvitee(guest.getId())).hasSize(1);
	}

	@Test
	void theOwnerSeesThePendingInvitationsOfTheDeckWithNames() {
		inviteGuest(DeckRole.READER);

		assertThat(listForDeck.execute(deck.getId(), owner.getId()))
				.extracting(DeckInvitationView::inviteeName, DeckInvitationView::role)
				.containsExactly(org.assertj.core.groups.Tuple.tuple("Gabriel", DeckRole.READER));
		assertThatThrownBy(() -> listForDeck.execute(deck.getId(), guest.getId())).isInstanceOf(DeckNotFoundException.class);
	}

	@Test
	void theOwnerCancelsAPendingInvitation() {
		inviteGuest(DeckRole.READER);
		UUID id = invitations.findPendingByInvitee(guest.getId()).get(0).getId();

		cancel.execute(deck.getId(), id, owner.getId());

		assertThat(invitations.size()).isZero();
	}

	@Test
	void cancelingAnUnknownOrAnsweredInvitationFails() {
		inviteGuest(DeckRole.READER);
		UUID id = invitations.findPendingByInvitee(guest.getId()).get(0).getId();
		answer.decline(id, guest.getId());

		assertThatThrownBy(() -> cancel.execute(deck.getId(), id, owner.getId()))
				.isInstanceOf(InvitationNotFoundException.class);
		assertThatThrownBy(() -> cancel.execute(deck.getId(), UUID.randomUUID(), owner.getId()))
				.isInstanceOf(InvitationNotFoundException.class);
	}

	@Test
	void theGuestSeesTheirPendingInvitationsWithDeckAndInviterNames() {
		inviteGuest(DeckRole.EDITOR);

		assertThat(listMine.execute(guest.getId()))
				.extracting(MyInvitationView::deckName, MyInvitationView::inviterName, MyInvitationView::role)
				.containsExactly(org.assertj.core.groups.Tuple.tuple("Anatomy", "Olivia", DeckRole.EDITOR));
		assertThat(listMine.execute(other.getId())).isEmpty();
	}

	@Test
	void acceptingMakesTheGuestAMemberWithTheOfferedRole() {
		inviteGuest(DeckRole.EDITOR);
		UUID id = invitations.findPendingByInvitee(guest.getId()).get(0).getId();

		DeckView view = answer.accept(id, guest.getId());

		assertThat(view.role()).isEqualTo(DeckRole.EDITOR);
		assertThat(members.findByDeckAndUser(deck.getId(), guest.getId()).orElseThrow().getRole()).isEqualTo(DeckRole.EDITOR);
		assertThat(listMine.execute(guest.getId())).isEmpty();
	}

	@Test
	void acceptingChangesTheRoleOfSomeoneWhoAlreadyJoined() {
		members.save(DeckMember.joinAsReader(deck.getId(), guest.getId()));
		inviteGuest(DeckRole.EDITOR);
		UUID id = invitations.findPendingByInvitee(guest.getId()).get(0).getId();

		answer.accept(id, guest.getId());

		assertThat(members.size()).isEqualTo(1);
		assertThat(members.findByDeckAndUser(deck.getId(), guest.getId()).orElseThrow().getRole()).isEqualTo(DeckRole.EDITOR);
	}

	@Test
	void decliningLeavesTheGuestOutside() {
		inviteGuest(DeckRole.READER);
		UUID id = invitations.findPendingByInvitee(guest.getId()).get(0).getId();

		answer.decline(id, guest.getId());

		assertThat(members.size()).isZero();
		assertThat(listMine.execute(guest.getId())).isEmpty();
	}

	@Test
	void onlyTheGuestAnswersAndOnlyOnce() {
		inviteGuest(DeckRole.READER);
		UUID id = invitations.findPendingByInvitee(guest.getId()).get(0).getId();

		assertThatThrownBy(() -> answer.accept(id, other.getId())).isInstanceOf(InvitationNotFoundException.class);
		assertThatThrownBy(() -> answer.decline(id, owner.getId())).isInstanceOf(InvitationNotFoundException.class);
		answer.accept(id, guest.getId());
		assertThatThrownBy(() -> answer.accept(id, guest.getId())).isInstanceOf(InvitationNotPendingException.class);
		assertThatThrownBy(() -> answer.decline(id, guest.getId())).isInstanceOf(InvitationNotPendingException.class);
	}
}
