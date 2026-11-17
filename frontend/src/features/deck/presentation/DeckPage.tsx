import { useState } from 'react';
import { useNavigate, useParams } from 'react-router';
import { Alert } from '../../../shared/ui/Alert';
import { Button } from '../../../shared/ui/Button';
import { ButtonLink } from '../../../shared/ui/ButtonLink';
import { ConfirmDialog } from '../../../shared/ui/ConfirmDialog';
import { PencilIcon, PlayIcon, PlusIcon, TrashIcon } from '../../../shared/ui/icons';
import { FlashcardList } from '../../flashcard/presentation/FlashcardList';
import { DeckNotFoundError } from '../domain/errors';
import { messageForDeleteDeckError, messageForJoinDeckError, roleLabels } from './deckMessages';
import { useDeck, useDeleteDeck, useJoinDeck } from './deckQueries';
import styles from './DeckPage.module.css';
import { VisibilityBadge } from './VisibilityBadge';

export function DeckPage() {
	const { id = '' } = useParams();
	const navigate = useNavigate();
	const { data: deck, isPending, error } = useDeck(id);
	const deleteDeck = useDeleteDeck();
	const joinDeck = useJoinDeck();
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
					<div className={styles.actions}>
						<ButtonLink to={`/decks/${deck.id}/study`}>
							<PlayIcon size={18} />
							Estudar
						</ButtonLink>
						{!deck.ownedByMe && !deck.role && (
							<Button type="button" variant="secondary" loading={joinDeck.isPending} onClick={() => joinDeck.mutate(deck.id)}>
								<PlusIcon size={18} />
								Participar
							</Button>
						)}
						{deck.ownedByMe && (
							<>
								<ButtonLink to={`/decks/${deck.id}/edit`} variant="secondary">
									<PencilIcon size={18} />
									Editar
								</ButtonLink>
								<Button type="button" variant="ghost" onClick={() => setConfirmingDelete(true)}>
									<TrashIcon size={18} />
									Excluir
								</Button>
							</>
						)}
					</div>
				</div>
				<div className={styles.meta}>
					<VisibilityBadge visibility={deck.visibility} />
					{deck.role && deck.role !== 'OWNER' && <span>Você participa como {roleLabels[deck.role]}</span>}
					<span>Criado em {new Date(deck.createdAt).toLocaleDateString('pt-BR', { dateStyle: 'long' })}</span>
				</div>
			</header>
			{joinDeck.isError && <Alert>{messageForJoinDeckError(joinDeck.error)}</Alert>}
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
