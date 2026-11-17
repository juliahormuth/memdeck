import { listStudyQueue } from './application/listStudyQueue';
import { recordReview } from './application/recordReview';
import type { RecordReviewCommand } from './application/recordReview';
import { HttpReviewGateway } from './infrastructure/HttpReviewGateway';

const gateway = new HttpReviewGateway();

export const reviewUseCases = {
	listStudyQueue: (deckId: string) => listStudyQueue(gateway, deckId),
	recordReview: (command: RecordReviewCommand) => recordReview(gateway, command),
};
