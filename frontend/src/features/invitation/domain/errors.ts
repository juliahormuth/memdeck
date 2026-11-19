export class InvalidInvitationError extends Error {
	constructor() {
		super('Invalid invitation');
		this.name = 'InvalidInvitationError';
	}
}

export class InvitationDeckNotFoundError extends Error {
	constructor() {
		super('Deck not found');
		this.name = 'InvitationDeckNotFoundError';
	}
}

export class InvitationNotFoundError extends Error {
	constructor() {
		super('Invitation not found');
		this.name = 'InvitationNotFoundError';
	}
}

export class InvitationAlreadyAnsweredError extends Error {
	constructor() {
		super('Invitation already answered');
		this.name = 'InvitationAlreadyAnsweredError';
	}
}

export class NotDeckOwnerError extends Error {
	constructor() {
		super('Only the deck owner can manage invitations');
		this.name = 'NotDeckOwnerError';
	}
}
