import { useNavigate } from 'react-router';
import { DeckForm } from './DeckForm';
import styles from './DeckFormPage.module.css';
import { messageForDeckError } from './deckMessages';
import { useCreateDeck } from './deckQueries';

export function NewDeckPage() {
	const navigate = useNavigate();
	const createDeck = useCreateDeck();

	return (
		<section className={styles.page}>
			<h1 className={styles.title}>Novo baralho</h1>
			<p className={styles.subtitle}>Escolha o nome e quem pode acessar o baralho.</p>
			<DeckForm
				initialValues={{ name: '', description: '', visibility: 'PRIVATE' }}
				submitLabel="Criar baralho"
				cancelTo="/"
				pending={createDeck.isPending}
				error={createDeck.isError ? messageForDeckError(createDeck.error) : undefined}
				onSubmit={(values) => createDeck.mutate(values, { onSuccess: (deck) => navigate(`/decks/${deck.id}`) })}
			/>
		</section>
	);
}
