import type { AvatarKey, Profile } from '../domain/Profile';

export interface ProfileChanges {
	name: string;
	avatar: AvatarKey;
}

export interface ProfileGateway {
	get(): Promise<Profile>;
	update(changes: ProfileChanges): Promise<Profile>;
}
