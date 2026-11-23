import { useState } from 'react';
import { useLocation } from 'react-router';
import { Alert } from '../../../shared/ui/Alert';
import { ButtonLink } from '../../../shared/ui/ButtonLink';
import { ConfirmDialog } from '../../../shared/ui/ConfirmDialog';
import { EyeIcon, EyeOffIcon, PencilIcon, PlusIcon, TrashIcon } from '../../../shared/ui/icons';
import type { Flashcard } from '../domain/Flashcard';
import { messageForDeleteFlashcardError } from './flashcardMessages';
import { useDeckFlashcards, useDeleteFlashcard } from './flashcardQueries';
import styles from './FlashcardList.module.css';

interface FlashcardListProps {
	deckId: string;
	canEdit: boolean;
}

export function FlashcardList({ deckId, canEdit }: FlashcardListProps) {
	const { data: flashcards, isPending, isError } = useDeckFlashcards(deckId);
	const deleteFlashcard = useDeleteFlashcard(deckId);
	const [toDelete, setToDelete] = useState<Flashcard | null>(null);
	// Hidden by default so studying does not spoil the answers; saving a flashcard opens the deck with the list shown.
	const location = useLocation();
	const [visible, setVisible] = useState((location.state as { showFlashcards?: boolean } | null)?.showFlashcards === true);

	const closeDialog = () => {
		if (!deleteFlashcard.isPending) {
			deleteFlashcard.reset();
			setToDelete(null);
		}
	};

	return (
		<section className={styles.section} aria-labelledby="flashcards-title">
			<div className={styles.header}>
				<h2 id="flashcards-title" className={styles.title}>
					Flashcards{flashcards && flashcards.length > 0 ? ` (${flashcards.length})` : ''}
				</h2>
				<div className={styles.tools}>
					{flashcards && flashcards.length > 0 && (
						<button
							type="button"
							className={styles.eye}
							aria-label={visible ? 'Ocultar flashcards' : 'Exibir flashcards'}
							title={visible ? 'Ocultar flashcards' : 'Exibir flashcards'}
							aria-expanded={visible}
							aria-controls="flashcards-list"
							onClick={() => setVisible((current) => !current)}
						>
							{visible ? <EyeOffIcon size={20} /> : <EyeIcon size={20} />}
						</button>
					)}
					{canEdit && (
						<ButtonLink to={`/decks/${deckId}/flashcards/new`} variant="secondary">
							<PlusIcon size={18} />
							Novo flashcard
						</ButtonLink>
					)}
				</div>
			</div>
			{isPending && <p role="status">Carregando flashcards...</p>}
			{isError && <Alert>Não foi possível carregar os flashcards.</Alert>}
			{flashcards && flashcards.length === 0 && (
				<p className={styles.empty}>
					{canEdit ? 'Este baralho ainda não tem flashcards. Crie o primeiro.' : 'Este baralho ainda não tem flashcards.'}
				</p>
			)}
			{flashcards && flashcards.length > 0 && visible && (
				<ul id="flashcards-list" className={styles.list}>
					{flashcards.map((flashcard) => (
						<li key={flashcard.id} className={styles.row}>
							<div className={styles.side}>
								<span className={styles.label}>Frente</span>
								<span className={styles.text}>{flashcard.front}</span>
							</div>
							<div className={styles.side}>
								<span className={styles.label}>Verso</span>
								<span className={styles.text}>{flashcard.back}</span>
							</div>
							{canEdit && (
								<div className={styles.actions}>
									<ButtonLink
										to={`/decks/${deckId}/flashcards/${flashcard.id}/edit`}
										variant="secondary"
										className={styles.iconButton}
										aria-label="Editar flashcard"
									>
										<PencilIcon size={18} />
									</ButtonLink>
									<button
										type="button"
										className={styles.deleteButton}
										aria-label="Excluir flashcard"
										onClick={() => setToDelete(flashcard)}
									>
										<TrashIcon size={18} />
									</button>
								</div>
							)}
						</li>
					))}
				</ul>
			)}
			<ConfirmDialog
				open={toDelete !== null}
				title="Excluir este flashcard?"
				description={toDelete ? `"${toDelete.front}" será apagado. Essa ação não pode ser desfeita.` : ''}
				confirmLabel="Excluir flashcard"
				pending={deleteFlashcard.isPending}
				error={deleteFlashcard.isError ? messageForDeleteFlashcardError(deleteFlashcard.error) : undefined}
				onConfirm={() => toDelete && deleteFlashcard.mutate(toDelete.id, { onSuccess: closeDialog })}
				onCancel={closeDialog}
			/>
		</section>
	);
}
