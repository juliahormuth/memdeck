import { describe, expect, it } from 'vitest';
import type { Friend, FriendRequest } from '../domain/Friendship';
import { FakeFriendshipGateway } from './FakeFriendshipGateway';
import {
	acceptFriendRequest,
	cancelFriendRequest,
	declineFriendRequest,
	listFriendRequests,
	listFriends,
	removeFriend,
	sendFriendRequest,
} from './friendshipUseCases';

const friend: Friend = { userId: 'u2', name: 'Bruno', avatar: 'STAR', since: '2026-01-01T12:00:00Z' };
const request: FriendRequest = { id: 'r1', direction: 'INCOMING', userId: 'u3', name: 'Carla', avatar: 'BOOK', createdAt: '2026-01-02T12:00:00Z' };

describe('friendship use cases', () => {
	it('lists friends and requests', async () => {
		const gateway = new FakeFriendshipGateway([friend], [request]);

		await expect(listFriends(gateway)).resolves.toEqual([friend]);
		await expect(listFriendRequests(gateway)).resolves.toEqual([request]);
	});

	it('trims the e-mail when sending a request', async () => {
		const gateway = new FakeFriendshipGateway();

		await sendFriendRequest(gateway, '  bruno@example.com ');

		expect(gateway.sent).toEqual(['bruno@example.com']);
	});

	it('answers, cancels and removes', async () => {
		const gateway = new FakeFriendshipGateway();

		await acceptFriendRequest(gateway, 'r1');
		await declineFriendRequest(gateway, 'r2');
		await cancelFriendRequest(gateway, 'r3');
		await removeFriend(gateway, 'u2');

		expect(gateway.calls).toEqual(['accept:r1', 'decline:r2', 'cancel:r3', 'remove:u2']);
	});
});
