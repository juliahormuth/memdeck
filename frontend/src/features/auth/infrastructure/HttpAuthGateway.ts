import { ApiError } from '../../../shared/http/ApiError';
import { httpClient } from '../../../shared/http/httpClient';
import type { AuthGateway, Credentials, NewAccount } from '../application/AuthGateway';
import {
	EmailAlreadyRegisteredError,
	InvalidCredentialsError,
	InvalidRegistrationError,
	TooManyLoginAttemptsError,
} from '../domain/errors';
import type { User } from '../domain/User';

interface UserDto {
	id: string;
	email: string;
}

function toUser(dto: UserDto): User {
	return { id: dto.id, email: dto.email };
}

export class HttpAuthGateway implements AuthGateway {
	async register(account: NewAccount): Promise<void> {
		try {
			await httpClient.post<UserDto>('/api/auth/register', account);
		} catch (error) {
			if (error instanceof ApiError && error.status === 409) {
				throw new EmailAlreadyRegisteredError();
			}
			if (error instanceof ApiError && error.status === 400) {
				throw new InvalidRegistrationError({});
			}
			throw error;
		}
	}

	async login(credentials: Credentials): Promise<User> {
		try {
			return toUser(await httpClient.post<UserDto>('/api/auth/login', credentials));
		} catch (error) {
			if (error instanceof ApiError && error.status === 401) {
				throw new InvalidCredentialsError();
			}
			if (error instanceof ApiError && error.status === 429) {
				throw new TooManyLoginAttemptsError(error.retryAfterSeconds);
			}
			throw error;
		}
	}

	async currentUser(): Promise<User | null> {
		try {
			return toUser(await httpClient.get<UserDto>('/api/auth/me'));
		} catch (error) {
			if (error instanceof ApiError && error.status === 401) {
				return null;
			}
			throw error;
		}
	}

	async logout(): Promise<void> {
		await httpClient.post<void>('/api/auth/logout');
	}
}
