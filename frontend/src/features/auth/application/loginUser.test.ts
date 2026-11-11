import { describe, expect, it } from 'vitest';
import { InvalidCredentialsError } from '../domain/errors';
import { FakeAuthGateway } from './FakeAuthGateway';
import { getCurrentUser } from './getCurrentUser';
import { loginUser } from './loginUser';
import { logoutUser } from './logoutUser';

describe('loginUser', () => {
	it('logs in with the trimmed email and keeps the password untouched', async () => {
		const gateway = new FakeAuthGateway();

		const user = await loginUser(gateway, { email: ' julia@example.com ', password: ' pass with spaces ' });

		expect(user.email).toBe('julia@example.com');
		expect(gateway.loggedIn).toEqual([{ email: 'julia@example.com', password: ' pass with spaces ' }]);
	});

	it.each([
		{ email: '', password: 'correct-horse' },
		{ email: '   ', password: 'correct-horse' },
		{ email: 'julia@example.com', password: '' },
	])('rejects blank credentials without calling the gateway: %j', async (credentials) => {
		const gateway = new FakeAuthGateway();

		await expect(loginUser(gateway, credentials)).rejects.toBeInstanceOf(InvalidCredentialsError);

		expect(gateway.loggedIn).toHaveLength(0);
	});
});

describe('session use cases', () => {
	it('returns the current user and null after logout', async () => {
		const gateway = new FakeAuthGateway();
		await loginUser(gateway, { email: 'julia@example.com', password: 'correct-horse' });

		expect(await getCurrentUser(gateway)).toEqual({ id: 'user-1', email: 'julia@example.com' });

		await logoutUser(gateway);

		expect(await getCurrentUser(gateway)).toBeNull();
		expect(gateway.loggedOut).toBe(1);
	});
});
