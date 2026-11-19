package br.com.memdeck.invitation.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.memdeck.deck.application.DeckAccessFinder;
import br.com.memdeck.deck.application.DeckMemberGateway;
import br.com.memdeck.deck.domain.DeckMember;
import br.com.memdeck.invitation.domain.InvalidInvitationException;
import br.com.memdeck.invitation.domain.Invitation;
import br.com.memdeck.user.application.UserGateway;
import br.com.memdeck.user.domain.InvalidUserException;
import br.com.memdeck.user.domain.User;

@Service
@Transactional
public class InviteToDeckUseCaseImpl implements InviteToDeckUseCase {

	private final DeckAccessFinder access;
	private final DeckMemberGateway members;
	private final UserGateway users;
	private final InvitationGateway invitations;

	public InviteToDeckUseCaseImpl(DeckAccessFinder access, DeckMemberGateway members, UserGateway users,
			InvitationGateway invitations) {
		this.access = access;
		this.members = members;
		this.users = users;
		this.invitations = invitations;
	}

	// Unknown e-mails, current members and repeated invitations end silently, so the answer never reveals who has an account.
	@Override
	public void execute(InviteToDeckCommand command) {
		access.owned(command.deckId(), command.requesterId());
		Invitation.requireOfferableRole(command.role());
		String email = normalized(command.email());
		User invitee = users.findByEmail(email).orElse(null);
		if (invitee == null) {
			return;
		}
		Invitation invitation = Invitation.create(command.deckId(), command.requesterId(), invitee.getId(),
				command.role());
		boolean alreadyHasTheRole = members.findByDeckAndUser(command.deckId(), invitee.getId())
				.map(DeckMember::getRole).filter(command.role()::equals).isPresent();
		if (alreadyHasTheRole || invitations.findPendingByDeckAndInvitee(command.deckId(), invitee.getId()).isPresent()) {
			return;
		}
		invitations.save(invitation);
	}

	private static String normalized(String email) {
		try {
			return User.normalizeEmail(email);
		} catch (InvalidUserException exception) {
			throw new InvalidInvitationException("The e-mail is not valid");
		}
	}
}
