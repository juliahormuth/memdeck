import type { User } from '../domain/User';
import type { AuthGateway, Credentials, NewAccount } from './AuthGateway';

export class FakeAuthGateway implements AuthGateway {
	registered: NewAccount[] = [];
	loggedIn: Credentials[] = [];
	loggedOut = 0;
	user: User | null = null;

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
