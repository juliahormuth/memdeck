import { describe, expect, it } from 'vitest';
import { InvalidRegistrationError } from '../domain/errors';
import { FakeAuthGateway } from './FakeAuthGateway';
import { registerUser } from './registerUser';

describe('registerUser', () => {
	it('sends the trimmed account to the gateway', async () => {
		const gateway = new FakeAuthGateway();

		await registerUser(gateway, { name: '  Julia ', email: ' julia@example.com ', password: 'correct-horse' });

		expect(gateway.registered).toEqual([{ name: 'Julia', email: 'julia@example.com', password: 'correct-horse' }]);
	});

	it('does not touch the gateway when the data is invalid', async () => {
		const gateway = new FakeAuthGateway();

		await expect(registerUser(gateway, { name: '', email: 'nope', password: 'short' })).rejects.toMatchObject({
			fields: { name: 'blank', email: 'invalid', password: 'too_short' },
		});

		expect(gateway.registered).toHaveLength(0);
	});

	it('throws a domain error that carries the invalid fields', async () => {
		await expect(
			registerUser(new FakeAuthGateway(), { name: 'Julia', email: 'julia@example.com', password: 'short' }),
		).rejects.toBeInstanceOf(InvalidRegistrationError);
	});
});
