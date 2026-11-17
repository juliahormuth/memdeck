import { useState } from 'react';
import { useNavigate, useParams } from 'react-router';
import { Alert } from '../../../shared/ui/Alert';
import { Button } from '../../../shared/ui/Button';
import { ButtonLink } from '../../../shared/ui/ButtonLink';
import { ConfirmDialog } from '../../../shared/ui/ConfirmDialog';
import { PencilIcon, TrashIcon } from '../../../shared/ui/icons';
import { FlashcardList } from '../../flashcard/presentation/FlashcardList';
import { DeckNotFoundError } from '../domain/errors';
import { messageForDeleteDeckError } from './deckMessages';
import { useDeck, useDeleteDeck } from './deckQueries';
import styles from './DeckPage.module.css';
import { VisibilityBadge } from './VisibilityBadge';

export function DeckPage() {
	const { id = '' } = useParams();
	const navigate = useNavigate();
	const { data: deck, isPending, error } = useDeck(id);
	const deleteDeck = useDeleteDeck();
	const [confirmingDelete, setConfirmingDelete] = useState(false);

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
				<div className={styles.titleRow}>
					<h1 className={styles.title}>{deck.name}</h1>
					{deck.ownedByMe && (
						<div className={styles.actions}>
							<ButtonLink to={`/decks/${deck.id}/edit`} variant="secondary">
								<PencilIcon size={18} />
								Editar
							</ButtonLink>
							<Button type="button" variant="ghost" onClick={() => setConfirmingDelete(true)}>
								<TrashIcon size={18} />
								Excluir
							</Button>
						</div>
					)}
				</div>
				<div className={styles.meta}>
					<VisibilityBadge visibility={deck.visibility} />
					<span>Criado em {new Date(deck.createdAt).toLocaleDateString('pt-BR', { dateStyle: 'long' })}</span>
				</div>
			</header>
			{deck.description ? (
				<p className={styles.description}>{deck.description}</p>
			) : (
				<p className={styles.empty}>Este baralho não tem descrição.</p>
			)}
			<FlashcardList deckId={deck.id} canEdit={deck.ownedByMe} />
			<ConfirmDialog
				open={confirmingDelete}
				title="Excluir este baralho?"
				description={`"${deck.name}" e todos os flashcards dele serão apagados. Essa ação não pode ser desfeita.`}
				confirmLabel="Excluir baralho"
				pending={deleteDeck.isPending}
				error={deleteDeck.isError ? messageForDeleteDeckError(deleteDeck.error) : undefined}
				onConfirm={() => deleteDeck.mutate(deck.id, { onSuccess: () => navigate('/', { replace: true }) })}
				onCancel={() => {
					if (!deleteDeck.isPending) {
						deleteDeck.reset();
						setConfirmingDelete(false);
					}
				}}
			/>
		</article>
	);
}
