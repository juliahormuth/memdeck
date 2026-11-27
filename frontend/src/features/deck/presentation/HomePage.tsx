import { Alert } from '../../../shared/ui/Alert';
import { Button } from '../../../shared/ui/Button';
import { ButtonLink } from '../../../shared/ui/ButtonLink';
import { FriendActivityFeed } from '../../friendship/presentation/FriendActivityFeed';
import { DeckCard } from './DeckCard';
import { useMyDecks } from './deckQueries';
import styles from './HomePage.module.css';

export function HomePage() {
	const { data: decks, isPending, isError, refetch } = useMyDecks();

	return (
		<div className={styles.page}>
			<header>
				<h1 className={styles.title}>Início</h1>
				<p className={styles.subtitle}>
					{decks && decks.length > 0
						? `${decks.length} ${decks.length === 1 ? 'baralho' : 'baralhos'}`
						: 'Seus baralhos de estudo.'}
				</p>
			</header>
			{isPending && <p role="status">Carregando seus baralhos...</p>}
			{isError && (
				<div className={styles.error}>
					<Alert>Não foi possível carregar seus baralhos.</Alert>
					<Button type="button" variant="ghost" onClick={() => void refetch()}>
						Tentar novamente
					</Button>
				</div>
			)}
			{decks && decks.length === 0 && (
				<section className={styles.empty} aria-labelledby="decks-empty-title">
					<img className={styles.emptyLogo} src="/logo.png" alt="" />
					<h2 id="decks-empty-title" className={styles.emptyTitle}>
						Seus baralhos aparecerão aqui
					</h2>
					<p className={styles.emptyText}>Crie um baralho para começar a estudar.</p>
					<ButtonLink to="/decks/new">Criar baralho</ButtonLink>
				</section>
			)}
			{decks && decks.length > 0 && (
				<ul className={styles.grid} aria-label="Seus baralhos">
					{decks.map((deck) => (
						<li key={deck.id}>
							<DeckCard deck={deck} />
						</li>
					))}
				</ul>
			)}
			<FriendActivityFeed />
		</div>
	);
}
