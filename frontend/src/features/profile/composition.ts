import type { ProfileChanges } from './application/ProfileGateway';
import { getProfile, updateProfile } from './application/profileUseCases';
import { HttpProfileGateway } from './infrastructure/HttpProfileGateway';

const gateway = new HttpProfileGateway();

export const profileUseCases = {
	getProfile: () => getProfile(gateway),
	updateProfile: (changes: ProfileChanges) => updateProfile(gateway, changes),
};
