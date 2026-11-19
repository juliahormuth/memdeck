import type { RankingEntry } from '../domain/RankingEntry';
import type { ReviewGrade } from '../domain/ReviewGrade';
import type { ReviewResult } from '../domain/ReviewResult';
import type { StudyCard } from '../domain/StudyCard';
import type { ReviewGateway } from './ReviewGateway';

export class FakeReviewGateway implements ReviewGateway {
	readonly recorded: { deckId: string; flashcardId: string; grade: ReviewGrade }[] = [];

	constructor(private readonly queue: StudyCard[] = []) {}

	async listStudyQueue(): Promise<StudyCard[]> {
		return this.queue;
	}

	async getRanking(): Promise<RankingEntry[]> {
		return [];
	}

	async recordReview(deckId: string, flashcardId: string, grade: ReviewGrade): Promise<ReviewResult> {
		this.recorded.push({ deckId, flashcardId, grade });
		return { dueDate: '2026-01-02', intervalDays: 1 };
	}
}
