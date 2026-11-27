import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { friendshipUseCases } from '../composition';

const friendsKey = ['friends'] as const;
const requestsKey = ['friend-requests'] as const;

export function useFriends() {
	return useQuery({ queryKey: friendsKey, queryFn: () => friendshipUseCases.listFriends(), retry: false });
}

export function useFriendActivity() {
	return useQuery({
		queryKey: [...friendsKey, 'activity'],
		queryFn: () => friendshipUseCases.listActivity(),
		retry: false,
	});
}

export function useFriendRequests() {
	return useQuery({ queryKey: requestsKey, queryFn: () => friendshipUseCases.listRequests(), retry: false });
}

// a mutation, so the throttled search only runs when the person asks for it
export function useSearchUser() {
	return useMutation({ mutationFn: (username: string) => friendshipUseCases.searchUser(username) });
}

export function useSendFriendRequest() {
	const queryClient = useQueryClient();
	return useMutation({
		mutationFn: (username: string) => friendshipUseCases.sendRequest(username),
		onSuccess: () => {
			void queryClient.invalidateQueries({ queryKey: requestsKey });
		},
	});
}

function useAnswer(action: (id: string) => Promise<void>, alsoFriends: boolean) {
	const queryClient = useQueryClient();
	return useMutation({
		mutationFn: action,
		onSuccess: () => {
			void queryClient.invalidateQueries({ queryKey: requestsKey });
			if (alsoFriends) {
				void queryClient.invalidateQueries({ queryKey: friendsKey });
				void queryClient.invalidateQueries({ queryKey: ['profile'] });
			}
		},
	});
}

export function useAcceptFriendRequest() {
	return useAnswer((id) => friendshipUseCases.accept(id), true);
}

export function useDeclineFriendRequest() {
	return useAnswer((id) => friendshipUseCases.decline(id), false);
}

export function useCancelFriendRequest() {
	return useAnswer((id) => friendshipUseCases.cancel(id), false);
}

export function useRemoveFriend() {
	const queryClient = useQueryClient();
	return useMutation({
		mutationFn: (userId: string) => friendshipUseCases.removeFriend(userId),
		onSuccess: () => {
			void queryClient.invalidateQueries({ queryKey: friendsKey });
			void queryClient.invalidateQueries({ queryKey: ['profile'] });
		},
	});
}
