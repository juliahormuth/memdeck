import type { ReviewGrade } from '../domain/ReviewGrade';
import type { ReviewResult } from '../domain/ReviewResult';
import type { StudyCard } from '../domain/StudyCard';

export interface ReviewGateway {
	listStudyQueue(deckId: string): Promise<StudyCard[]>;
	recordReview(deckId: string, flashcardId: string, grade: ReviewGrade): Promise<ReviewResult>;
}
