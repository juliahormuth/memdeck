import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect, useState } from 'react';
import { useForm, useWatch } from 'react-hook-form';
import { Link, useNavigate, useParams } from 'react-router';
import { z } from 'zod';
import { validatedField } from '../../../shared/forms/validatedField';
import { Alert } from '../../../shared/ui/Alert';
import { Button } from '../../../shared/ui/Button';
import { ButtonLink } from '../../../shared/ui/ButtonLink';
import { ArrowLeftIcon, StarIcon } from '../../../shared/ui/icons';
import { Select } from '../../../shared/ui/Select';
import { TextArea } from '../../../shared/ui/TextArea';
import { canEditFlashcards } from '../../deck/domain/Deck';
import type { Deck } from '../../deck/domain/Deck';
import { useDeck, useMyDecks } from '../../deck/presentation/deckQueries';
import type { Flashcard } from '../domain/Flashcard';
import { validateFlashcardBack, validateFlashcardFront } from '../domain/rules';
import { backMessages, frontMessages, messageForFlashcardError } from './flashcardMessages';
import { useCreateFlashcard, useDeckFlashcards, useUpdateFlashcard } from './flashcardQueries';
import styles from './FlashcardEditorPage.module.css';

const schema = z.object({
	front: validatedField(validateFlashcardFront, frontMessages),
	back: validatedField(validateFlashcardBack, backMessages),
});

type NewFlashcardFormValues = z.infer<typeof schema>;

export function FlashcardEditorPage() {
	const { deckId = '', flashcardId } = useParams();
	const { data: deck, isPending, isError, isPlaceholderData } = useDeck(deckId, { keepPreviousData: true });
	const { data: flashcards, isPending: flashcardsPending } = useDeckFlashcards(deckId);

	if (isPending || (flashcardId && flashcardsPending)) {
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
	if (!canEditFlashcards(deck)) {
		return (
			<div className={styles.blocked}>
				<h1 className={styles.title}>{flashcardId ? 'Editar flashcard' : 'Novo flashcard'}</h1>
				<p>Só o dono ou um editor do baralho pode {flashcardId ? 'editar' : 'adicionar'} flashcards.</p>
				<ButtonLink to={`/decks/${deck.id}`}>Voltar ao baralho</ButtonLink>
			</div>
		);
	}
	if (!flashcardId) {
		return <FlashcardEditor deck={deck} switchingDeck={isPlaceholderData} />;
	}
	const flashcard = flashcards?.find((candidate) => candidate.id === flashcardId);
	if (!flashcard) {
		return (
			<div className={styles.blocked}>
				<h1 className={styles.title}>Flashcard não encontrado</h1>
				<p>Esse flashcard não existe mais neste baralho.</p>
				<ButtonLink to={`/decks/${deck.id}`}>Voltar ao baralho</ButtonLink>
			</div>
		);
	}
	return <FlashcardEditor deck={deck} flashcard={flashcard} switchingDeck={isPlaceholderData} />;
}

function FlashcardEditor({ deck, flashcard, switchingDeck }: { deck: Deck; flashcard?: Flashcard; switchingDeck: boolean }) {
	const editing = flashcard !== undefined;
	const navigate = useNavigate();
	const { data: myDecks } = useMyDecks();
	const createFlashcard = useCreateFlashcard();
	const updateFlashcard = useUpdateFlashcard();
	const mutation = editing ? updateFlashcard : createFlashcard;
	const [side, setSide] = useState<'front' | 'back'>('front');
	const [savedCount, setSavedCount] = useState(0);
	const [justSaved, setJustSaved] = useState(false);
	const {
		register,
		handleSubmit,
		reset,
		setFocus,
		control,
		formState: { errors },
	} = useForm<NewFlashcardFormValues>({
		resolver: zodResolver(schema),
		defaultValues: { front: flashcard?.front ?? '', back: flashcard?.back ?? '' },
	});
	const [front, back] = useWatch({ control, name: ['front', 'back'] });

	// the fields only accept focus after the re-render that follows saving
	useEffect(() => {
		if (savedCount > 0) {
			setFocus('front');
		}
	}, [savedCount, setFocus]);

	const save = (createAnother: boolean) =>
		handleSubmit((values) => {
			setJustSaved(false);
			if (flashcard) {
				updateFlashcard.mutate(
					{ deckId: deck.id, id: flashcard.id, ...values },
					{ onSuccess: () => navigate(`/decks/${deck.id}`) },
				);
				return;
			}
			createFlashcard.mutate(
				{ deckId: deck.id, ...values },
				{
					onSuccess: () => {
						if (!createAnother) {
							navigate(`/decks/${deck.id}`);
							return;
						}
						reset();
						setSide('front');
						setJustSaved(true);
						setSavedCount((count) => count + 1);
					},
				},
			);
		});

	const previewText = side === 'front' ? front : back;

	return (
		<section className={styles.page}>
			<Link to={`/decks/${deck.id}`} className={styles.back}>
				<ArrowLeftIcon size={18} />
				{deck.name}
			</Link>
			<h1 className={styles.title}>{editing ? 'Editar flashcard' : 'Novo flashcard'}</h1>
			<p className={styles.subtitle}>Escreva a pergunta na frente do cartão e a resposta no verso.</p>
			<div className={styles.layout}>
				<form className={styles.form} noValidate onSubmit={save(false)}>
					{mutation.isError && <Alert>{messageForFlashcardError(mutation.error)}</Alert>}
					{justSaved && (
						<p role="status" className={styles.saved}>
							Flashcard salvo. Escreva o próximo.
						</p>
					)}
					<Select
						label="Baralho"
						value={deck.id}
						disabled={editing}
						onChange={(event) => navigate(`/decks/${event.target.value}/flashcards/new`, { replace: true })}
					>
						{(myDecks?.filter(canEditFlashcards) ?? [deck]).map((option) => (
							<option key={option.id} value={option.id}>
								{option.name}
							</option>
						))}
					</Select>
					<TextArea
						label="Frente (pergunta, conceito ou palavra)"
						error={errors.front?.message}
						{...register('front')}
					/>
					<TextArea label="Verso (resposta ou tradução)" error={errors.back?.message} {...register('back')} />
					<p className={styles.tip}>
						<StarIcon size={18} />
						Dica: cards curtos e com uma única ideia são mais fáceis de memorizar.
					</p>
					<div className={styles.actions}>
						<ButtonLink to={`/decks/${deck.id}`} variant="ghost">
							Cancelar
						</ButtonLink>
						{!editing && (
							<Button type="button" variant="secondary" loading={createFlashcard.isPending} disabled={switchingDeck} onClick={() => void save(true)()}>
								Salvar e criar outro
							</Button>
						)}
						<Button type="submit" loading={mutation.isPending} disabled={switchingDeck}>
							{editing ? 'Salvar alterações' : 'Salvar flashcard'}
						</Button>
					</div>
				</form>
				<aside className={styles.preview} aria-label="Pré-visualização">
					<div className={styles.previewHeader}>
						<span className={styles.previewTitle}>Pré-visualização</span>
						<div className={styles.toggle}>
							<button type="button" className={styles.toggleButton} aria-pressed={side === 'front'} onClick={() => setSide('front')}>
								Frente
							</button>
							<button type="button" className={styles.toggleButton} aria-pressed={side === 'back'} onClick={() => setSide('back')}>
								Verso
							</button>
						</div>
					</div>
					<div className={styles.card}>
						<span className={styles.cardSide}>{side === 'front' ? 'Frente' : 'Verso'}</span>
						{previewText.trim() ? (
							<p className={styles.cardText}>{previewText}</p>
						) : (
							<p className={styles.cardPlaceholder}>O que você escrever aparece aqui.</p>
						)}
					</div>
				</aside>
			</div>
		</section>
	);
}
