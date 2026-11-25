import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import type { QueryClient } from '@tanstack/react-query';
import { authUseCases } from '../composition';
import type { Credentials, NewAccount, PasswordReset } from '../application/AuthGateway';

export const currentUserKey = ['auth', 'current-user'] as const;

export function clearSessionData(queryClient: QueryClient) {
	queryClient.setQueryData(currentUserKey, null);
	queryClient.removeQueries({ predicate: (query) => query.queryKey[0] !== currentUserKey[0] });
}

export function useCurrentUser() {
	return useQuery({
		queryKey: currentUserKey,
		queryFn: authUseCases.getCurrentUser,
		staleTime: Infinity,
		retry: false,
	});
}

export function useLogin() {
	const queryClient = useQueryClient();
	return useMutation({
		mutationFn: (credentials: Credentials) => authUseCases.loginUser(credentials),
		onSuccess: (user) => queryClient.setQueryData(currentUserKey, user),
	});
}

export function useRegister() {
	const queryClient = useQueryClient();
	return useMutation({
		mutationFn: async (account: NewAccount) => {
			await authUseCases.registerUser(account);
			return authUseCases.loginUser({ email: account.email, password: account.password });
		},
		onSuccess: (user) => queryClient.setQueryData(currentUserKey, user),
	});
}

export function useRequestPasswordReset() {
	return useMutation({ mutationFn: (email: string) => authUseCases.requestPasswordReset(email) });
}

export function useConfirmPasswordReset() {
	return useMutation({ mutationFn: (reset: PasswordReset) => authUseCases.confirmPasswordReset(reset) });
}

export function useLogout() {
	const queryClient = useQueryClient();
	return useMutation({
		mutationFn: () => authUseCases.logoutUser(),
		onSuccess: () => clearSessionData(queryClient),
	});
}
