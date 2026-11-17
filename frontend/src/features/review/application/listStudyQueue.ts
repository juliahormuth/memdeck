import type { StudyCard } from '../domain/StudyCard';
import type { ReviewGateway } from './ReviewGateway';

export function listStudyQueue(gateway: ReviewGateway, deckId: string): Promise<StudyCard[]> {
	return gateway.listStudyQueue(deckId);
}
