import { describe, expect, it } from 'vitest';
import type { Profile } from '../domain/Profile';
import { validateProfileName } from '../domain/rules';
import { FakeProfileGateway } from './FakeProfileGateway';
import { getProfile, updateProfile } from './profileUseCases';

const profile: Profile = {
	id: 'u1',
	name: 'Julia',
	email: 'julia@example.com',
	avatar: 'BOOK',
	reviewedDays: 3,
	streakDays: 2,
	friends: 1,
};

describe('profile use cases', () => {
	it('gets the profile', async () => {
		await expect(getProfile(new FakeProfileGateway(profile))).resolves.toEqual(profile);
	});

	it('trims the name when updating and sends the avatar', async () => {
		const gateway = new FakeProfileGateway(profile);

		const updated = await updateProfile(gateway, { name: '  Julia Hormuth ', avatar: 'STAR' });

		expect(gateway.updates).toEqual([{ name: 'Julia Hormuth', avatar: 'STAR' }]);
		expect(updated).toMatchObject({ name: 'Julia Hormuth', avatar: 'STAR' });
	});

	it('validates the name', () => {
		expect(validateProfileName('  ')).toBe('blank');
		expect(validateProfileName('a'.repeat(101))).toBe('too_long');
		expect(validateProfileName('Julia')).toBeNull();
	});
});
