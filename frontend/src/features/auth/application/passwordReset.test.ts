import { describe, expect, it } from 'vitest';
import { confirmPasswordReset } from './confirmPasswordReset';
import { FakeAuthGateway } from './FakeAuthGateway';
import { requestPasswordReset } from './requestPasswordReset';

describe('password reset use cases', () => {
	it('asks for a code with the trimmed email', async () => {
		const gateway = new FakeAuthGateway();

		await requestPasswordReset(gateway, ' julia@example.com ');

		expect(gateway.resetRequests).toEqual(['julia@example.com']);
	});

	it('trims the email and the code and keeps the password untouched', async () => {
		const gateway = new FakeAuthGateway();

		await confirmPasswordReset(gateway, {
			email: ' julia@example.com ',
			code: ' 123456 ',
			newPassword: ' pass with spaces ',
		});

		expect(gateway.resets).toEqual([
			{ email: 'julia@example.com', code: '123456', newPassword: ' pass with spaces ' },
		]);
	});
});
