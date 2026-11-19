package br.com.memdeck.invitation.application;

import java.util.List;
import java.util.UUID;

public interface ListMyInvitationsUseCase {

	List<MyInvitationView> execute(UUID userId);
}
