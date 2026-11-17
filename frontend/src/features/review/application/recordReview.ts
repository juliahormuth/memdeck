import type { ReviewGrade } from '../domain/ReviewGrade';
import type { ReviewResult } from '../domain/ReviewResult';
import type { ReviewGateway } from './ReviewGateway';

export interface RecordReviewCommand {
	deckId: string;
	flashcardId: string;
	grade: ReviewGrade;
}

export function recordReview(gateway: ReviewGateway, command: RecordReviewCommand): Promise<ReviewResult> {
	return gateway.recordReview(command.deckId, command.flashcardId, command.grade);
}
