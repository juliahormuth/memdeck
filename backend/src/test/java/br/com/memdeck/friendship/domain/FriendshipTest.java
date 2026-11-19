package br.com.memdeck.friendship.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class FriendshipTest {

	private static final UUID ME = UUID.randomUUID();
	private static final UUID THEM = UUID.randomUUID();

	@Test
	void startsPendingAndInvolvesBothPeople() {
		Friendship request = Friendship.request(ME, THEM);

		assertThat(request.isPending()).isTrue();
		assertThat(request.involves(ME)).isTrue();
		assertThat(request.involves(THEM)).isTrue();
		assertThat(request.involves(UUID.randomUUID())).isFalse();
		assertThat(request.otherUser(ME)).isEqualTo(THEM);
		assertThat(request.otherUser(THEM)).isEqualTo(ME);
	}

	@Test
	void cannotBeSentToYourself() {
		assertThatThrownBy(() -> Friendship.request(ME, ME)).isInstanceOf(InvalidFriendRequestException.class);
	}

	@Test
	void acceptingMakesItAFriendshipOnlyOnce() {
		Friendship accepted = Friendship.request(ME, THEM).accept();

		assertThat(accepted.getStatus()).isEqualTo(FriendshipStatus.ACCEPTED);
		assertThat(accepted.isPending()).isFalse();
		assertThat(accepted.getRespondedAt()).isNotNull();
		assertThatThrownBy(accepted::accept).isInstanceOf(FriendshipNotFoundException.class);
	}
}
