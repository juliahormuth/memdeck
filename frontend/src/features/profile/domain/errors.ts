export class InvalidProfileError extends Error {
	constructor() {
		super('Invalid profile data');
		this.name = 'InvalidProfileError';
	}
}
