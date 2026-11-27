export class InvalidFriendRequestError extends Error {
	constructor() {
		super('Invalid friend request');
		this.name = 'InvalidFriendRequestError';
	}
}

export class TooManySearchesError extends Error {
	readonly retryAfterSeconds: number | null;

	constructor(retryAfterSeconds: number | null) {
		super('Too many searches');
		this.name = 'TooManySearchesError';
		this.retryAfterSeconds = retryAfterSeconds;
	}
}

export class FriendshipNotFoundError extends Error {
	constructor() {
		super('Friendship or friend request not found');
		this.name = 'FriendshipNotFoundError';
	}
}
