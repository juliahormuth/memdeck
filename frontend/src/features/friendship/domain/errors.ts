export class InvalidFriendRequestError extends Error {
	constructor() {
		super('Invalid friend request');
		this.name = 'InvalidFriendRequestError';
	}
}

export class FriendshipNotFoundError extends Error {
	constructor() {
		super('Friendship or friend request not found');
		this.name = 'FriendshipNotFoundError';
	}
}
