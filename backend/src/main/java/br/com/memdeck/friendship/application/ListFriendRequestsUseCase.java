package br.com.memdeck.friendship.application;

import java.util.List;
import java.util.UUID;

public interface ListFriendRequestsUseCase {

	List<FriendRequestView> execute(UUID userId);
}
