import type { User } from '../domain/User';

export interface NewAccount {
	name: string;
	email: string;
	password: string;
}

export interface Credentials {
	email: string;
	password: string;
}

export interface AuthGateway {
	register(account: NewAccount): Promise<void>;
	login(credentials: Credentials): Promise<User>;
	currentUser(): Promise<User | null>;
	logout(): Promise<void>;
}
