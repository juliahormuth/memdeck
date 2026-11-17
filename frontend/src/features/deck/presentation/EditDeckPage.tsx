import { useNavigate, useParams } from 'react-router';
import { ButtonLink } from '../../../shared/ui/ButtonLink';
import { DeckForm } from './DeckForm';
import styles from './DeckFormPage.module.css';
import { messageForUpdateDeckError } from './deckMessages';
import { useDeck, useUpdateDeck } from './deckQueries';

export function EditDeckPage() {
	const { id = '' } = useParams();
	const navigate = useNavigate();
	const { data: deck, isPending, isError } = useDeck(id);
	const updateDeck = useUpdateDeck();

	if (isPending) {
		return <p role="status">Carregando...</p>;
	}
	if (isError || !deck) {
		return (
			<div className={styles.blocked}>
				<h1 className={styles.title}>Baralho não encontrado</h1>
				<p>Esse baralho não existe ou você não tem acesso a ele.</p>
				<ButtonLink to="/">Voltar ao início</ButtonLink>
			</div>
		);
	}
	if (!deck.ownedByMe) {
		return (
			<div className={styles.blocked}>
				<h1 className={styles.title}>Editar baralho</h1>
				<p>Só o dono do baralho pode editá-lo.</p>
				<ButtonLink to={`/decks/${deck.id}`}>Voltar ao baralho</ButtonLink>
			</div>
		);
	}

	return (
		<section className={styles.page}>
			<h1 className={styles.title}>Editar baralho</h1>
			<p className={styles.subtitle}>Mude o nome, a descrição ou quem pode acessar o baralho.</p>
			<DeckForm
				initialValues={{ name: deck.name, description: deck.description ?? '', visibility: deck.visibility }}
				submitLabel="Salvar alterações"
				cancelTo={`/decks/${deck.id}`}
				pending={updateDeck.isPending}
				error={updateDeck.isError ? messageForUpdateDeckError(updateDeck.error) : undefined}
				onSubmit={(values) =>
					updateDeck.mutate({ id: deck.id, ...values }, { onSuccess: () => navigate(`/decks/${deck.id}`) })
				}
			/>
		</section>
	);
}
