import type { AuthGateway } from './AuthGateway';

export function logoutUser(gateway: AuthGateway): Promise<void> {
	return gateway.logout();
}
