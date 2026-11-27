import type { FieldErrors } from './rules';

export class InvalidCredentialsError extends Error {
	constructor() {
		super('Invalid email or password');
		this.name = 'InvalidCredentialsError';
	}
}

export class InvalidResetCodeError extends Error {
	constructor() {
		super('Invalid or expired code');
		this.name = 'InvalidResetCodeError';
	}
}

export class TooManyLoginAttemptsError extends Error {
	readonly retryAfterSeconds: number | null;

	constructor(retryAfterSeconds: number | null) {
		super('Too many failed login attempts');
		this.name = 'TooManyLoginAttemptsError';
		this.retryAfterSeconds = retryAfterSeconds;
	}
}

export class EmailAlreadyRegisteredError extends Error {
	constructor() {
		super('Email is already registered');
		this.name = 'EmailAlreadyRegisteredError';
	}
}

export class UsernameAlreadyTakenError extends Error {
	constructor() {
		super('Username is already taken');
		this.name = 'UsernameAlreadyTakenError';
	}
}

export class InvalidRegistrationError extends Error {
	readonly fields: FieldErrors;

	constructor(fields: FieldErrors) {
		super('Invalid registration data');
		this.name = 'InvalidRegistrationError';
		this.fields = fields;
	}
}
