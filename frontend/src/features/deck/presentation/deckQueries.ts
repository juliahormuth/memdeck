import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import type { CreateDeckCommand } from '../application/createDeck';
import type { UpdateDeckCommand } from '../application/updateDeck';
import { deckUseCases } from '../composition';

const deckKey = (id: string) => ['deck', id] as const;
export const myDecksKey = ['decks', 'mine'] as const;
const publicDecksKey = ['decks', 'public'] as const;

export function useCreateDeck() {
	const queryClient = useQueryClient();
	return useMutation({
		mutationFn: (command: CreateDeckCommand) => deckUseCases.createDeck(command),
		onSuccess: (deck) => {
			queryClient.setQueryData(deckKey(deck.id), deck);
			void queryClient.invalidateQueries({ queryKey: myDecksKey });
		},
	});
}

export function useDeck(id: string, options: { keepPreviousData?: boolean } = {}) {
	return useQuery({
		queryKey: deckKey(id),
		queryFn: () => deckUseCases.getDeck(id),
		retry: false,
		placeholderData: options.keepPreviousData ? keepPreviousData : undefined,
	});
}

export function useMyDecks() {
	return useQuery({
		queryKey: myDecksKey,
		queryFn: () => deckUseCases.listMyDecks(),
		retry: false,
	});
}

export function useUpdateDeck() {
	const queryClient = useQueryClient();
	return useMutation({
		mutationFn: (command: UpdateDeckCommand) => deckUseCases.updateDeck(command),
		onSuccess: (deck) => {
			queryClient.setQueryData(deckKey(deck.id), deck);
			void queryClient.invalidateQueries({ queryKey: myDecksKey });
		},
	});
}

export function useDeleteDeck() {
	const queryClient = useQueryClient();
	return useMutation({
		mutationFn: (id: string) => deckUseCases.deleteDeck(id),
		onSuccess: (_result, id) => {
			queryClient.removeQueries({ queryKey: deckKey(id) });
			queryClient.removeQueries({ queryKey: ['flashcards', id] });
			void queryClient.invalidateQueries({ queryKey: myDecksKey });
		},
	});
}

export function useSearchPublicDecks(term: string) {
	return useQuery({
		queryKey: [...publicDecksKey, term],
		queryFn: () => deckUseCases.searchPublicDecks(term),
		retry: false,
		placeholderData: keepPreviousData,
	});
}

export function useJoinDeck() {
	const queryClient = useQueryClient();
	return useMutation({
		mutationFn: (id: string) => deckUseCases.joinDeck(id),
		onSuccess: (deck) => {
			queryClient.setQueryData(deckKey(deck.id), deck);
			void queryClient.invalidateQueries({ queryKey: myDecksKey });
			void queryClient.invalidateQueries({ queryKey: publicDecksKey });
		},
	});
}
