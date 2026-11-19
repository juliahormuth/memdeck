import { Alert } from '../../../shared/ui/Alert';
import { Avatar } from '../../../shared/ui/Avatar';
import { TrophyIcon } from '../../../shared/ui/icons';
import { useDeckRanking } from './reviewQueries';
import styles from './DeckRanking.module.css';

export function DeckRanking({ deckId }: { deckId: string }) {
	const { data: ranking, isPending, isError } = useDeckRanking(deckId);

	return (
		<section className={styles.section} aria-labelledby="ranking-title">
			<h2 id="ranking-title" className={styles.title}>
				<TrophyIcon size={22} />
				Ranking
			</h2>
			{isPending && <p role="status">Carregando ranking...</p>}
			{isError && <Alert>Não foi possível carregar o ranking.</Alert>}
			{ranking && (
				<ol className={styles.list} aria-label="Ranking do baralho">
					{ranking.map((entry) => (
						<li key={entry.userId} className={[styles.row, entry.me ? styles.me : ''].filter(Boolean).join(' ')}>
							<span className={styles.position}>{entry.position}º</span>
							<Avatar avatar={entry.avatar} size={36} />
							<span className={styles.name}>
								{entry.name}
								{entry.me ? ' (você)' : ''}
							</span>
							<span className={styles.count}>
								{entry.reviewedFlashcards} {entry.reviewedFlashcards === 1 ? 'flashcard revisado' : 'flashcards revisados'}
							</span>
						</li>
					))}
				</ol>
			)}
		</section>
	);
}
