import type { Friend, FriendRequest } from '../domain/Friendship';
import type { FriendshipGateway } from './FriendshipGateway';

export function listFriends(gateway: FriendshipGateway): Promise<Friend[]> {
	return gateway.listFriends();
}

export function removeFriend(gateway: FriendshipGateway, userId: string): Promise<void> {
	return gateway.removeFriend(userId);
}

export function listFriendRequests(gateway: FriendshipGateway): Promise<FriendRequest[]> {
	return gateway.listRequests();
}

export function sendFriendRequest(gateway: FriendshipGateway, email: string): Promise<void> {
	return gateway.sendRequest(email.trim());
}

export function acceptFriendRequest(gateway: FriendshipGateway, requestId: string): Promise<void> {
	return gateway.accept(requestId);
}

export function declineFriendRequest(gateway: FriendshipGateway, requestId: string): Promise<void> {
	return gateway.decline(requestId);
}

export function cancelFriendRequest(gateway: FriendshipGateway, requestId: string): Promise<void> {
	return gateway.cancel(requestId);
}
