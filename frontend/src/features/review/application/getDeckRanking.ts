import type { RankingEntry } from '../domain/RankingEntry';
import type { ReviewGateway } from './ReviewGateway';

export function getDeckRanking(gateway: ReviewGateway, deckId: string): Promise<RankingEntry[]> {
	return gateway.getRanking(deckId);
}
