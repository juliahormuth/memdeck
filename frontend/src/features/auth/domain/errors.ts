import type { FieldErrors } from './rules';

export class InvalidCredentialsError extends Error {
	constructor() {
		super('Invalid email or password');
		this.name = 'InvalidCredentialsError';
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

export class InvalidRegistrationError extends Error {
	readonly fields: FieldErrors;

	constructor(fields: FieldErrors) {
		super('Invalid registration data');
		this.name = 'InvalidRegistrationError';
		this.fields = fields;
	}
}
