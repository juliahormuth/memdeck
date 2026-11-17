import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import type { CreateFlashcardCommand } from '../application/createFlashcard';
import type { UpdateFlashcardCommand } from '../application/updateFlashcard';
import { flashcardUseCases } from '../composition';

const flashcardsKey = (deckId: string) => ['flashcards', deckId] as const;

export function useCreateFlashcard() {
	const queryClient = useQueryClient();
	return useMutation({
		mutationFn: (command: CreateFlashcardCommand) => flashcardUseCases.createFlashcard(command),
		onSuccess: (flashcard) => {
			void queryClient.invalidateQueries({ queryKey: flashcardsKey(flashcard.deckId) });
		},
	});
}

export function useDeckFlashcards(deckId: string) {
	return useQuery({
		queryKey: flashcardsKey(deckId),
		queryFn: () => flashcardUseCases.listDeckFlashcards(deckId),
		retry: false,
	});
}

export function useUpdateFlashcard() {
	const queryClient = useQueryClient();
	return useMutation({
		mutationFn: (command: UpdateFlashcardCommand) => flashcardUseCases.updateFlashcard(command),
		onSuccess: (flashcard) => {
			void queryClient.invalidateQueries({ queryKey: flashcardsKey(flashcard.deckId) });
		},
	});
}

export function useDeleteFlashcard(deckId: string) {
	const queryClient = useQueryClient();
	return useMutation({
		mutationFn: (id: string) => flashcardUseCases.deleteFlashcard(deckId, id),
		onSuccess: () => {
			void queryClient.invalidateQueries({ queryKey: flashcardsKey(deckId) });
		},
	});
}
