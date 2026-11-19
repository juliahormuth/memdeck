package br.com.memdeck.invitation.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import br.com.memdeck.invitation.application.InvitationGateway;
import br.com.memdeck.invitation.domain.Invitation;
import br.com.memdeck.invitation.domain.InvitationStatus;

@Component
class InvitationGatewayJpa implements InvitationGateway {

	private final InvitationJpaRepository repository;

	InvitationGatewayJpa(InvitationJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public Invitation save(Invitation invitation) {
		return toDomain(repository.save(new InvitationJpaModel(invitation.getId(), invitation.getDeckId(),
				invitation.getInviteeId(), invitation.getInviterId(), invitation.getRole(), invitation.getStatus(),
				invitation.getCreatedAt(), invitation.getRespondedAt())));
	}

	@Override
	public Optional<Invitation> findById(UUID id) {
		return repository.findById(id).map(InvitationGatewayJpa::toDomain);
	}

	@Override
	public void deleteById(UUID id) {
		repository.deleteById(id);
	}

	@Override
	public List<Invitation> findPendingByDeck(UUID deckId) {
		return repository.findByDeckIdAndStatus(deckId, InvitationStatus.PENDING).stream()
				.map(InvitationGatewayJpa::toDomain).toList();
	}

	@Override
	public List<Invitation> findPendingByInvitee(UUID inviteeId) {
		return repository.findByInviteeIdAndStatus(inviteeId, InvitationStatus.PENDING).stream()
				.map(InvitationGatewayJpa::toDomain).toList();
	}

	@Override
	public Optional<Invitation> findPendingByDeckAndInvitee(UUID deckId, UUID inviteeId) {
		return repository.findByDeckIdAndInviteeIdAndStatus(deckId, inviteeId, InvitationStatus.PENDING)
				.map(InvitationGatewayJpa::toDomain);
	}

	private static Invitation toDomain(InvitationJpaModel model) {
		return Invitation.restore(model.getId(), model.getDeckId(), model.getInviteeId(), model.getInviterId(),
				model.getRole(), model.getStatus(), model.getCreatedAt(), model.getRespondedAt());
	}
}
