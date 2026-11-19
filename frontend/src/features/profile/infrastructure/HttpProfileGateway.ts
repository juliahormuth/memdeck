import { ApiError } from '../../../shared/http/ApiError';
import { httpClient } from '../../../shared/http/httpClient';
import type { ProfileChanges, ProfileGateway } from '../application/ProfileGateway';
import { InvalidProfileError } from '../domain/errors';
import type { Profile } from '../domain/Profile';

export class HttpProfileGateway implements ProfileGateway {
	async get(): Promise<Profile> {
		return httpClient.get<Profile>('/api/users/me');
	}

	async update(changes: ProfileChanges): Promise<Profile> {
		try {
			return await httpClient.put<Profile>('/api/users/me', changes);
		} catch (error) {
			if (error instanceof ApiError && error.status === 400) {
				throw new InvalidProfileError();
			}
			throw error;
		}
	}
}
