import type { User } from '../domain/User';

export interface NewAccount {
	name: string;
	username: string;
	email: string;
	password: string;
}

export interface Credentials {
	email: string;
	password: string;
}

export interface PasswordReset {
	email: string;
	code: string;
	newPassword: string;
}

export interface AuthGateway {
	requestPasswordReset(email: string): Promise<void>;
	confirmPasswordReset(reset: PasswordReset): Promise<void>;
	register(account: NewAccount): Promise<void>;
	login(credentials: Credentials): Promise<User>;
	currentUser(): Promise<User | null>;
	logout(): Promise<void>;
}
