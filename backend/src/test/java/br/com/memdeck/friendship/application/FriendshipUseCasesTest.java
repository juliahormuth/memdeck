package br.com.memdeck.friendship.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.memdeck.friendship.domain.Friendship;
import br.com.memdeck.friendship.domain.FriendshipNotFoundException;
import br.com.memdeck.friendship.domain.InvalidFriendRequestException;
import br.com.memdeck.user.application.InMemoryUserGateway;
import br.com.memdeck.user.domain.User;

class FriendshipUseCasesTest {

	private final InMemoryUserGateway users = new InMemoryUserGateway();
	private final InMemoryFriendshipGateway friendships = new InMemoryFriendshipGateway();
	private final SendFriendRequestUseCase send = new SendFriendRequestUseCaseImpl(users, friendships);
	private final ListFriendRequestsUseCase listRequests = new ListFriendRequestsUseCaseImpl(users, friendships);
	private final AnswerFriendRequestUseCase answer = new AnswerFriendRequestUseCaseImpl(friendships);
	private final ListFriendsUseCase listFriends = new ListFriendsUseCaseImpl(users, friendships);
	private final RemoveFriendUseCase remove = new RemoveFriendUseCaseImpl(friendships);

	private final User ana = users.save(User.register("Ana", "ana", "ana@example.com", "hash"));
	private final User bruno = users.save(User.register("Bruno", "bruno", "bruno@example.com", "hash"));
	private final User carla = users.save(User.register("Carla", "carla", "carla@example.com", "hash"));

	private UUID pendingIdFor(User user) {
		return listRequests.execute(user.getId()).get(0).id();
	}

	@Test
	void sendingARequestShowsItAsIncomingForTheOtherPersonAndOutgoingForTheSender() {
		send.execute(ana.getId(), "  Bruno ");

		assertThat(listRequests.execute(bruno.getId())).extracting(FriendRequestView::direction, FriendRequestView::name)
				.containsExactly(org.assertj.core.groups.Tuple.tuple(FriendRequestView.Direction.INCOMING, "Ana"));
		assertThat(listRequests.execute(ana.getId())).extracting(FriendRequestView::direction, FriendRequestView::name)
				.containsExactly(org.assertj.core.groups.Tuple.tuple(FriendRequestView.Direction.OUTGOING, "Bruno"));
		assertThat(listRequests.execute(carla.getId())).isEmpty();
	}

	@Test
	void anUnknownUsernameEndsSilentlyWithoutRevealingThatThereIsNoAccount() {
		send.execute(ana.getId(), "nobody");

		assertThat(friendships.size()).isZero();
	}

	@Test
	void aMalformedUsernameAndYourOwnAreRejected() {
		assertThatThrownBy(() -> send.execute(ana.getId(), "not a username")).isInstanceOf(InvalidFriendRequestException.class);
		assertThatThrownBy(() -> send.execute(ana.getId(), "ana")).isInstanceOf(InvalidFriendRequestException.class);
	}

	@Test
	void repeatedRequestsInEitherDirectionAndRequestsToFriendsEndSilently() {
		send.execute(ana.getId(), "bruno");
		send.execute(ana.getId(), "bruno");
		send.execute(bruno.getId(), "ana");
		assertThat(friendships.size()).isEqualTo(1);

		answer.accept(pendingIdFor(bruno), bruno.getId());
		send.execute(ana.getId(), "bruno");
		assertThat(friendships.size()).isEqualTo(1);
	}

	@Test
	void theAddresseeAcceptsAndBothSeeEachOtherAsFriends() {
		send.execute(ana.getId(), "bruno");

		answer.accept(pendingIdFor(bruno), bruno.getId());

		assertThat(listFriends.execute(ana.getId())).extracting(FriendView::name).containsExactly("Bruno");
		assertThat(listFriends.execute(bruno.getId())).extracting(FriendView::name).containsExactly("Ana");
		assertThat(listRequests.execute(ana.getId())).isEmpty();
	}

	@Test
	void onlyTheAddresseeAcceptsOrDeclines() {
		send.execute(ana.getId(), "bruno");
		UUID id = pendingIdFor(ana);

		assertThatThrownBy(() -> answer.accept(id, ana.getId())).isInstanceOf(FriendshipNotFoundException.class);
		assertThatThrownBy(() -> answer.decline(id, ana.getId())).isInstanceOf(FriendshipNotFoundException.class);
		assertThatThrownBy(() -> answer.accept(id, carla.getId())).isInstanceOf(FriendshipNotFoundException.class);
	}

	@Test
	void decliningDeletesTheRequestSoItCanBeSentAgain() {
		send.execute(ana.getId(), "bruno");

		answer.decline(pendingIdFor(bruno), bruno.getId());
		assertThat(friendships.size()).isZero();

		send.execute(ana.getId(), "bruno");
		assertThat(friendships.size()).isEqualTo(1);
	}

	@Test
	void theSenderCancelsAndOnlyTheSenderCan() {
		send.execute(ana.getId(), "bruno");
		UUID id = pendingIdFor(ana);

		assertThatThrownBy(() -> answer.cancel(id, bruno.getId())).isInstanceOf(FriendshipNotFoundException.class);
		answer.cancel(id, ana.getId());

		assertThat(friendships.size()).isZero();
	}

	@Test
	void anAnsweredRequestCannotBeAnsweredOrCancelledAgain() {
		send.execute(ana.getId(), "bruno");
		UUID id = pendingIdFor(ana);
		answer.accept(id, bruno.getId());

		assertThatThrownBy(() -> answer.accept(id, bruno.getId())).isInstanceOf(FriendshipNotFoundException.class);
		assertThatThrownBy(() -> answer.cancel(id, ana.getId())).isInstanceOf(FriendshipNotFoundException.class);
	}

	@Test
	void eitherFriendRemovesTheFriendship() {
		Friendship accepted = friendships.save(Friendship.request(ana.getId(), bruno.getId()).accept());

		remove.execute(bruno.getId(), ana.getId());

		assertThat(friendships.findById(accepted.getId())).isEmpty();
	}

	@Test
	void removingSomeoneWhoIsNotAFriendOrOnlyPendingFails() {
		send.execute(ana.getId(), "bruno");

		assertThatThrownBy(() -> remove.execute(ana.getId(), bruno.getId())).isInstanceOf(FriendshipNotFoundException.class);
		assertThatThrownBy(() -> remove.execute(ana.getId(), carla.getId())).isInstanceOf(FriendshipNotFoundException.class);
	}
}
