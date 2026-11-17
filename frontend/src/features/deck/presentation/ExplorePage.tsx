import { useState } from 'react';
import type { FormEvent } from 'react';
import { Alert } from '../../../shared/ui/Alert';
import { Button } from '../../../shared/ui/Button';
import { TextField } from '../../../shared/ui/TextField';
import { PlusIcon } from '../../../shared/ui/icons';
import { DeckCard } from './DeckCard';
import { messageForJoinDeckError, roleLabels } from './deckMessages';
import { useJoinDeck, useSearchPublicDecks } from './deckQueries';
import styles from './ExplorePage.module.css';

export function ExplorePage() {
	const [typed, setTyped] = useState('');
	const [term, setTerm] = useState('');
	const { data: decks, isPending, isError, refetch } = useSearchPublicDecks(term);
	const joinDeck = useJoinDeck();

	const search = (event: FormEvent) => {
		event.preventDefault();
		setTerm(typed.trim());
	};

	return (
		<div className={styles.page}>
			<header>
				<h1 className={styles.title}>Explorar</h1>
				<p className={styles.subtitle}>
					{term ? `Baralhos públicos com "${term}".` : 'Baralhos públicos de outras pessoas, os mais recentes primeiro.'}
				</p>
			</header>
			<form role="search" className={styles.search} onSubmit={search}>
				<div className={styles.field}>
					<TextField
						label="Pesquisar baralhos públicos"
						type="search"
						placeholder="Nome ou descrição"
						maxLength={100}
						value={typed}
						onChange={(event) => setTyped(event.target.value)}
					/>
				</div>
				<Button type="submit">Pesquisar</Button>
			</form>
			{joinDeck.isError && <Alert>{messageForJoinDeckError(joinDeck.error)}</Alert>}
			{isPending && <p role="status">Pesquisando...</p>}
			{isError && (
				<div className={styles.error}>
					<Alert>Não foi possível pesquisar os baralhos.</Alert>
					<Button type="button" variant="ghost" onClick={() => void refetch()}>
						Tentar novamente
					</Button>
				</div>
			)}
			{decks && decks.length === 0 && (
				<p className={styles.empty}>
					{term ? `Nenhum baralho público encontrado para "${term}".` : 'Ainda não há baralhos públicos de outras pessoas.'}
				</p>
			)}
			{decks && decks.length > 0 && (
				<ul className={styles.grid} aria-label="Baralhos públicos">
					{decks.map((deck) => (
						<li key={deck.id}>
							<DeckCard
								deck={deck}
								action={
									deck.role ? (
										<p className={styles.joined}>Você participa como {roleLabels[deck.role]}</p>
									) : (
										<Button
											type="button"
											variant="secondary"
											fullWidth
											loading={joinDeck.isPending && joinDeck.variables === deck.id}
											onClick={() => joinDeck.mutate(deck.id)}
										>
											<PlusIcon size={18} />
											Participar
										</Button>
									)
								}
							/>
						</li>
					))}
				</ul>
			)}
		</div>
	);
}
