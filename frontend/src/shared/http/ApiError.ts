export class ApiError extends Error {
	readonly status: number;
	readonly retryAfterSeconds: number | null;

	constructor(status: number, message: string, retryAfterSeconds: number | null = null) {
		super(message);
		this.name = 'ApiError';
		this.status = status;
		this.retryAfterSeconds = retryAfterSeconds;
	}
}
