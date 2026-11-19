package br.com.memdeck.invitation.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.memdeck.invitation.domain.InvitationStatus;

interface InvitationJpaRepository extends JpaRepository<InvitationJpaModel, UUID> {

	List<InvitationJpaModel> findByDeckIdAndStatus(UUID deckId, InvitationStatus status);

	List<InvitationJpaModel> findByInviteeIdAndStatus(UUID inviteeId, InvitationStatus status);

	Optional<InvitationJpaModel> findByDeckIdAndInviteeIdAndStatus(UUID deckId, UUID inviteeId, InvitationStatus status);
}
