import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import type { CreateDeckCommand } from '../application/createDeck';
import { deckUseCases } from '../composition';

const deckKey = (id: string) => ['deck', id] as const;

export function useCreateDeck() {
	const queryClient = useQueryClient();
	return useMutation({
		mutationFn: (command: CreateDeckCommand) => deckUseCases.createDeck(command),
		onSuccess: (deck) => queryClient.setQueryData(deckKey(deck.id), deck),
	});
}

export function useDeck(id: string) {
	return useQuery({
		queryKey: deckKey(id),
		queryFn: () => deckUseCases.getDeck(id),
		retry: false,
	});
}
