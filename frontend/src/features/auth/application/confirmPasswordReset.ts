import type { AuthGateway, PasswordReset } from './AuthGateway';

export function confirmPasswordReset(gateway: AuthGateway, reset: PasswordReset): Promise<void> {
	return gateway.confirmPasswordReset({ ...reset, email: reset.email.trim(), code: reset.code.trim() });
}
