import { useMutation, useQuery } from '@tanstack/react-query';
import type { RecordReviewCommand } from '../application/recordReview';
import { reviewUseCases } from '../composition';

export function useStudyQueue(deckId: string) {
	return useQuery({
		queryKey: ['study', deckId],
		queryFn: () => reviewUseCases.listStudyQueue(deckId),
		retry: false,
		// Fixed during a session (no reshuffling); gcTime 0 refetches it when the page opens again.
		staleTime: Infinity,
		gcTime: 0,
		refetchOnWindowFocus: false,
	});
}

export function useRecordReview() {
	return useMutation({
		mutationFn: (command: RecordReviewCommand) => reviewUseCases.recordReview(command),
	});
}
