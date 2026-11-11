import type { User } from '../domain/User';
import type { AuthGateway } from './AuthGateway';

export function getCurrentUser(gateway: AuthGateway): Promise<User | null> {
	return gateway.currentUser();
}
