import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import type { ProfileChanges } from '../application/ProfileGateway';
import { profileUseCases } from '../composition';

export const profileKey = ['profile'] as const;

export function useProfile() {
	return useQuery({
		queryKey: profileKey,
		queryFn: () => profileUseCases.getProfile(),
		retry: false,
	});
}

export function useUpdateProfile() {
	const queryClient = useQueryClient();
	return useMutation({
		mutationFn: (changes: ProfileChanges) => profileUseCases.updateProfile(changes),
		onSuccess: (profile) => {
			queryClient.setQueryData(profileKey, profile);
		},
	});
}
