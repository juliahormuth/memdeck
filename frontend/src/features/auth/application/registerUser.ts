import { InvalidRegistrationError } from '../domain/errors';
import { validateRegistration } from '../domain/rules';
import type { AuthGateway, NewAccount } from './AuthGateway';

export async function registerUser(gateway: AuthGateway, account: NewAccount): Promise<void> {
	const errors = validateRegistration(account);
	if (Object.keys(errors).length > 0) {
		throw new InvalidRegistrationError(errors);
	}
	await gateway.register({ name: account.name.trim(), email: account.email.trim(), password: account.password });
}
