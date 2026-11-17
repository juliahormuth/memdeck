import type { ReactNode } from 'react';
import { Link } from 'react-router';
import { LayersIcon } from '../../../shared/ui/icons';
import type { Deck } from '../domain/Deck';
import { roleLabels } from './deckMessages';
import styles from './DeckCard.module.css';
import { VisibilityBadge } from './VisibilityBadge';

export function DeckCard({ deck, action }: { deck: Deck; action?: ReactNode }) {
	return (
		<article className={styles.card}>
			<div className={styles.top}>
				<span className={styles.icon}>
					<LayersIcon size={24} />
				</span>
				<div className={styles.badges}>
					{deck.role && deck.role !== 'OWNER' && <span className={styles.role}>{roleLabels[deck.role]}</span>}
					<VisibilityBadge visibility={deck.visibility} />
				</div>
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
			{action && <div className={styles.action}>{action}</div>}
		</article>
	);
}
