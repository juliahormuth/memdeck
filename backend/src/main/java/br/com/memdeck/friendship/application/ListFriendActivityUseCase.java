package br.com.memdeck.friendship.application;

import java.util.List;
import java.util.UUID;

public interface ListFriendActivityUseCase {

	List<FriendActivity> execute(UUID userId);
}
