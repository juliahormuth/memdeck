import type { Credentials, NewAccount } from './application/AuthGateway';
import { getCurrentUser } from './application/getCurrentUser';
import { loginUser } from './application/loginUser';
import { logoutUser } from './application/logoutUser';
import { registerUser } from './application/registerUser';
import { HttpAuthGateway } from './infrastructure/HttpAuthGateway';

const gateway = new HttpAuthGateway();

export const authUseCases = {
	registerUser: (account: NewAccount) => registerUser(gateway, account),
	loginUser: (credentials: Credentials) => loginUser(gateway, credentials),
	getCurrentUser: () => getCurrentUser(gateway),
	logoutUser: () => logoutUser(gateway),
};
