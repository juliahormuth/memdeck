import type { Profile } from '../domain/Profile';
import type { ProfileChanges, ProfileGateway } from './ProfileGateway';

export class FakeProfileGateway implements ProfileGateway {
	readonly updates: ProfileChanges[] = [];

	constructor(private profile: Profile) {}

	async get(): Promise<Profile> {
		return this.profile;
	}

	async update(changes: ProfileChanges): Promise<Profile> {
		this.updates.push(changes);
		this.profile = { ...this.profile, ...changes };
		return this.profile;
	}
}
