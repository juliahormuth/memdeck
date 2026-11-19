package br.com.memdeck.invitation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.memdeck.deck.domain.DeckRole;

class InvitationTest {

	private static final UUID DECK = UUID.randomUUID();
	private static final UUID INVITER = UUID.randomUUID();
	private static final UUID INVITEE = UUID.randomUUID();

	@Test
	void startsPendingWithTheOfferedRole() {
		Invitation invitation = Invitation.create(DECK, INVITER, INVITEE, DeckRole.EDITOR);

		assertThat(invitation.isPending()).isTrue();
		assertThat(invitation.getRole()).isEqualTo(DeckRole.EDITOR);
		assertThat(invitation.getRespondedAt()).isNull();
	}

	@Test
	void cannotOfferTheOwnerRole() {
		assertThatThrownBy(() -> Invitation.create(DECK, INVITER, INVITEE, DeckRole.OWNER))
				.isInstanceOf(InvalidInvitationException.class);
	}

	@Test
	void cannotInviteYourself() {
		assertThatThrownBy(() -> Invitation.create(DECK, INVITER, INVITER, DeckRole.READER))
				.isInstanceOf(InvalidInvitationException.class);
	}

	@Test
	void acceptingAndDecliningRecordTheAnswer() {
		Invitation invitation = Invitation.create(DECK, INVITER, INVITEE, DeckRole.READER);

		Invitation accepted = invitation.accept();
		Invitation declined = invitation.decline();

		assertThat(accepted.getStatus()).isEqualTo(InvitationStatus.ACCEPTED);
		assertThat(declined.getStatus()).isEqualTo(InvitationStatus.DECLINED);
		assertThat(accepted.getRespondedAt()).isNotNull();
		assertThat(accepted.isPending()).isFalse();
	}

	@Test
	void anAnsweredInvitationCannotBeAnsweredAgain() {
		Invitation accepted = Invitation.create(DECK, INVITER, INVITEE, DeckRole.READER).accept();

		assertThatThrownBy(accepted::accept).isInstanceOf(InvitationNotPendingException.class);
		assertThatThrownBy(accepted::decline).isInstanceOf(InvitationNotPendingException.class);
	}
}
