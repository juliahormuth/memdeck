import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { authUseCases } from '../composition';
import type { Credentials, NewAccount } from '../application/AuthGateway';

export const currentUserKey = ['auth', 'current-user'] as const;

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

export function useLogout() {
	const queryClient = useQueryClient();
	return useMutation({
		mutationFn: () => authUseCases.logoutUser(),
		onSuccess: () => {
			queryClient.setQueryData(currentUserKey, null);
			queryClient.removeQueries({ predicate: (query) => query.queryKey[0] !== currentUserKey[0] });
		},
	});
}
