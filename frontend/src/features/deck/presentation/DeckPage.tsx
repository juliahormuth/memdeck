import { useState } from 'react';
import { useNavigate, useParams } from 'react-router';
import { Alert } from '../../../shared/ui/Alert';
import { Button } from '../../../shared/ui/Button';
import { ButtonLink } from '../../../shared/ui/ButtonLink';
import { ConfirmDialog } from '../../../shared/ui/ConfirmDialog';
import { LogoutIcon, PencilIcon, PlayIcon, PlusIcon, TrashIcon } from '../../../shared/ui/icons';
import { FlashcardList } from '../../flashcard/presentation/FlashcardList';
import { DeckRanking } from '../../review/presentation/DeckRanking';
import { DeckTeamPanel } from './DeckTeamPanel';
import { canEditFlashcards } from '../domain/Deck';
import { DeckNotFoundError } from '../domain/errors';
import { messageForDeleteDeckError, messageForJoinDeckError, messageForLeaveDeckError, roleLabels } from './deckMessages';
import { useDeck, useDeleteDeck, useJoinDeck, useLeaveDeck } from './deckQueries';
import styles from './DeckPage.module.css';
import { VisibilityBadge } from './VisibilityBadge';

export function DeckPage() {
	const { id = '' } = useParams();
	const navigate = useNavigate();
	const { data: deck, isPending, error } = useDeck(id);
	const deleteDeck = useDeleteDeck();
	const joinDeck = useJoinDeck();
	const leaveDeck = useLeaveDeck();
	const [confirmingLeave, setConfirmingLeave] = useState(false);
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
						{(deck.role === 'EDITOR' || deck.role === 'READER') && (
							<Button type="button" variant="ghost" onClick={() => setConfirmingLeave(true)}>
								<LogoutIcon size={18} />
								Sair do baralho
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
			<FlashcardList deckId={deck.id} canEdit={canEditFlashcards(deck)} />
			{deck.role && <DeckRanking deckId={deck.id} />}
			{deck.ownedByMe && <DeckTeamPanel deckId={deck.id} />}
			{(deck.role === 'EDITOR' || deck.role === 'READER') && (
				<ConfirmDialog
					open={confirmingLeave}
					title="Sair deste baralho?"
					description={`Você deixa de participar de "${deck.name}". ${deck.visibility === 'PRIVATE' ? 'Como ele é privado, só volta a ver o baralho se for convidado de novo.' : 'Como ele é público, você pode entrar de novo quando quiser.'}`}
					confirmLabel="Sair do baralho"
					pending={leaveDeck.isPending}
					error={leaveDeck.isError ? messageForLeaveDeckError(leaveDeck.error) : undefined}
					onConfirm={() => leaveDeck.mutate(deck.id, { onSuccess: () => navigate('/', { replace: true }) })}
					onCancel={() => {
						if (!leaveDeck.isPending) {
							leaveDeck.reset();
							setConfirmingLeave(false);
						}
					}}
				/>
			)}
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
