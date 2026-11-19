package br.com.memdeck.friendship.application;

import java.util.List;
import java.util.UUID;

public interface ListFriendsUseCase {

	List<FriendView> execute(UUID userId);
}
