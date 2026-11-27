import {
	acceptFriendRequest,
	cancelFriendRequest,
	declineFriendRequest,
	listFriendActivity,
	listFriendRequests,
	listFriends,
	removeFriend,
	sendFriendRequest,
} from './application/friendshipUseCases';
import { HttpFriendshipGateway } from './infrastructure/HttpFriendshipGateway';

const gateway = new HttpFriendshipGateway();

export const friendshipUseCases = {
	listFriends: () => listFriends(gateway),
	listActivity: () => listFriendActivity(gateway),
	removeFriend: (userId: string) => removeFriend(gateway, userId),
	listRequests: () => listFriendRequests(gateway),
	sendRequest: (email: string) => sendFriendRequest(gateway, email),
	accept: (requestId: string) => acceptFriendRequest(gateway, requestId),
	decline: (requestId: string) => declineFriendRequest(gateway, requestId),
	cancel: (requestId: string) => cancelFriendRequest(gateway, requestId),
};
