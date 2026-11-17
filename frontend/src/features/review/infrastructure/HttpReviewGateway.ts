import { ApiError } from '../../../shared/http/ApiError';
import { httpClient } from '../../../shared/http/httpClient';
import type { ReviewGateway } from '../application/ReviewGateway';
import { StudyDeckNotFoundError, StudyFlashcardNotFoundError } from '../domain/errors';
import type { ReviewGrade } from '../domain/ReviewGrade';
import type { ReviewResult } from '../domain/ReviewResult';
import type { StudyCard } from '../domain/StudyCard';

interface StudyCardDto {
	id: string;
	front: string;
	back: string;
	newCard: boolean;
}

interface ReviewDto {
	dueDate: string;
	intervalDays: number;
}

const deckPath = (deckId: string) => `/api/decks/${encodeURIComponent(deckId)}`;

export class HttpReviewGateway implements ReviewGateway {
	async listStudyQueue(deckId: string): Promise<StudyCard[]> {
		try {
			const cards = await httpClient.get<StudyCardDto[]>(`${deckPath(deckId)}/study`);
			return cards.map((card) => ({ id: card.id, front: card.front, back: card.back, newCard: card.newCard }));
		} catch (error) {
			if (error instanceof ApiError && (error.status === 404 || error.status === 400)) {
				throw new StudyDeckNotFoundError();
			}
			throw error;
		}
	}

	async recordReview(deckId: string, flashcardId: string, grade: ReviewGrade): Promise<ReviewResult> {
		try {
			const result = await httpClient.post<ReviewDto>(
				`${deckPath(deckId)}/flashcards/${encodeURIComponent(flashcardId)}/reviews`,
				{ grade },
			);
			return { dueDate: result.dueDate, intervalDays: result.intervalDays };
		} catch (error) {
			if (error instanceof ApiError && error.status === 404) {
				throw new StudyFlashcardNotFoundError();
			}
			throw error;
		}
	}
}
