import type { ReviewGrade } from '../domain/ReviewGrade';
import type { ReviewResult } from '../domain/ReviewResult';
import type { RankingEntry } from '../domain/RankingEntry';
import type { StudyCard } from '../domain/StudyCard';

export interface ReviewGateway {
	listStudyQueue(deckId: string): Promise<StudyCard[]>;
	getRanking(deckId: string): Promise<RankingEntry[]>;
	recordReview(deckId: string, flashcardId: string, grade: ReviewGrade): Promise<ReviewResult>;
}
