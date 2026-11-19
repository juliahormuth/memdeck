import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import type { InviteToDeckCommand } from '../application/inviteToDeck';
import { invitationUseCases } from '../composition';

const myInvitationsKey = ['invitations', 'mine'] as const;
const deckInvitationsKey = (deckId: string) => ['deck', deckId, 'invitations'] as const;

export function useMyInvitations() {
	return useQuery({
		queryKey: myInvitationsKey,
		queryFn: () => invitationUseCases.listMyInvitations(),
		retry: false,
	});
}

export function useDeckInvitations(deckId: string) {
	return useQuery({
		queryKey: deckInvitationsKey(deckId),
		queryFn: () => invitationUseCases.listDeckInvitations(deckId),
		retry: false,
	});
}

export function useInviteToDeck() {
	const queryClient = useQueryClient();
	return useMutation({
		mutationFn: (command: InviteToDeckCommand) => invitationUseCases.inviteToDeck(command),
		onSuccess: (_result, command) => {
			void queryClient.invalidateQueries({ queryKey: deckInvitationsKey(command.deckId) });
		},
	});
}

export function useCancelInvitation(deckId: string) {
	const queryClient = useQueryClient();
	return useMutation({
		mutationFn: (invitationId: string) => invitationUseCases.cancelInvitation(deckId, invitationId),
		onSuccess: () => {
			void queryClient.invalidateQueries({ queryKey: deckInvitationsKey(deckId) });
		},
	});
}

export function useAcceptInvitation() {
	const queryClient = useQueryClient();
	return useMutation({
		mutationFn: (invitationId: string) => invitationUseCases.acceptInvitation(invitationId),
		onSuccess: (accepted) => {
			void queryClient.invalidateQueries({ queryKey: myInvitationsKey });
			void queryClient.invalidateQueries({ queryKey: ['decks'] });
			void queryClient.invalidateQueries({ queryKey: ['deck', accepted.deckId] });
		},
	});
}

export function useDeclineInvitation() {
	const queryClient = useQueryClient();
	return useMutation({
		mutationFn: (invitationId: string) => invitationUseCases.declineInvitation(invitationId),
		onSuccess: () => {
			void queryClient.invalidateQueries({ queryKey: myInvitationsKey });
		},
	});
}
