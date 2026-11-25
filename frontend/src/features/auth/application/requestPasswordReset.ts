import type { AuthGateway } from './AuthGateway';

export function requestPasswordReset(gateway: AuthGateway, email: string): Promise<void> {
	return gateway.requestPasswordReset(email.trim());
}
