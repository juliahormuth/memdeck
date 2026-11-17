import { Link } from 'react-router';
import { LayersIcon } from '../../../shared/ui/icons';
import type { Deck } from '../domain/Deck';
import styles from './DeckCard.module.css';
import { VisibilityBadge } from './VisibilityBadge';

export function DeckCard({ deck }: { deck: Deck }) {
	return (
		<article className={styles.card}>
			<div className={styles.top}>
				<span className={styles.icon}>
					<LayersIcon size={24} />
				</span>
				<VisibilityBadge visibility={deck.visibility} />
			</div>
			<h2 className={styles.title}>
				<Link to={`/decks/${deck.id}`} className={styles.link}>
					{deck.name}
				</Link>
			</h2>
			{deck.description ? (
				<p className={styles.description}>{deck.description}</p>
			) : (
				<p className={styles.empty}>Sem descrição</p>
			)}
			<p className={styles.footer}>
				Criado em {new Date(deck.createdAt).toLocaleDateString('pt-BR', { dateStyle: 'medium' })}
			</p>
		</article>
	);
}
