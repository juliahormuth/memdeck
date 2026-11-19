import type { Profile } from '../domain/Profile';
import type { ProfileChanges, ProfileGateway } from './ProfileGateway';

export function getProfile(gateway: ProfileGateway): Promise<Profile> {
	return gateway.get();
}

export function updateProfile(gateway: ProfileGateway, changes: ProfileChanges): Promise<Profile> {
	return gateway.update({ name: changes.name.trim(), avatar: changes.avatar });
}
