import { useParams } from 'react-router';
import { Alert } from '../../../shared/ui/Alert';
import { ButtonLink } from '../../../shared/ui/ButtonLink';
import { DeckNotFoundError } from '../domain/errors';
import { visibilityLabels } from './deckMessages';
import { useDeck } from './deckQueries';
import styles from './DeckPage.module.css';

export function DeckPage() {
	const { id = '' } = useParams();
	const { data: deck, isPending, error } = useDeck(id);

	if (isPending) {
		return <p role="status">Carregando...</p>;
	}
	if (error instanceof DeckNotFoundError) {
		return (
			<div className={styles.notFound}>
				<h1 className={styles.title}>Baralho não encontrado</h1>
				<p>Esse baralho não existe ou você não tem acesso a ele.</p>
				<ButtonLink to="/">Voltar ao início</ButtonLink>
			</div>
		);
	}
	if (error || !deck) {
		return <Alert>Não foi possível carregar o baralho. Tente novamente.</Alert>;
	}

	return (
		<article className={styles.page}>
			<header>
				<h1 className={styles.title}>{deck.name}</h1>
				<div className={styles.meta}>
					<span className={styles.badge}>{visibilityLabels[deck.visibility]}</span>
					<span>Criado em {new Date(deck.createdAt).toLocaleDateString('pt-BR', { dateStyle: 'long' })}</span>
				</div>
			</header>
			{deck.description ? (
				<p className={styles.description}>{deck.description}</p>
			) : (
				<p className={styles.empty}>Este baralho não tem descrição.</p>
			)}
		</article>
	);
}
