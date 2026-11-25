import type { Credentials, NewAccount, PasswordReset } from './application/AuthGateway';
import { confirmPasswordReset } from './application/confirmPasswordReset';
import { getCurrentUser } from './application/getCurrentUser';
import { loginUser } from './application/loginUser';
import { logoutUser } from './application/logoutUser';
import { registerUser } from './application/registerUser';
import { requestPasswordReset } from './application/requestPasswordReset';
import { HttpAuthGateway } from './infrastructure/HttpAuthGateway';

const gateway = new HttpAuthGateway();

export const authUseCases = {
	registerUser: (account: NewAccount) => registerUser(gateway, account),
	loginUser: (credentials: Credentials) => loginUser(gateway, credentials),
	getCurrentUser: () => getCurrentUser(gateway),
	logoutUser: () => logoutUser(gateway),
	requestPasswordReset: (email: string) => requestPasswordReset(gateway, email),
	confirmPasswordReset: (reset: PasswordReset) => confirmPasswordReset(gateway, reset),
};
