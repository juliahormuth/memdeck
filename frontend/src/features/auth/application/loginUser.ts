import { InvalidCredentialsError } from '../domain/errors';
import type { User } from '../domain/User';
import type { AuthGateway, Credentials } from './AuthGateway';

export async function loginUser(gateway: AuthGateway, credentials: Credentials): Promise<User> {
	const email = credentials.email.trim();
	if (email.length === 0 || credentials.password.length === 0) {
		throw new InvalidCredentialsError();
	}
	return gateway.login({ email, password: credentials.password });
}
