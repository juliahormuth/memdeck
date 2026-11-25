import type { User } from '../domain/User';
import type { AuthGateway, Credentials, NewAccount, PasswordReset } from './AuthGateway';

export class FakeAuthGateway implements AuthGateway {
	registered: NewAccount[] = [];
	loggedIn: Credentials[] = [];
	loggedOut = 0;
	user: User | null = null;
	resetRequests: string[] = [];
	resets: PasswordReset[] = [];

	async requestPasswordReset(email: string): Promise<void> {
		this.resetRequests.push(email);
	}

	async confirmPasswordReset(reset: PasswordReset): Promise<void> {
		this.resets.push(reset);
	}

	async register(account: NewAccount): Promise<void> {
		this.registered.push(account);
	}

	async login(credentials: Credentials): Promise<User> {
		this.loggedIn.push(credentials);
		this.user = { id: 'user-1', email: credentials.email };
		return this.user;
	}

	async currentUser(): Promise<User | null> {
		return this.user;
	}

	async logout(): Promise<void> {
		this.loggedOut += 1;
		this.user = null;
	}
}
