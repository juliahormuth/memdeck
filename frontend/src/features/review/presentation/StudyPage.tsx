import { useEffect, useRef, useState } from 'react';
import { useParams } from 'react-router';
import { Alert } from '../../../shared/ui/Alert';
import { ButtonLink } from '../../../shared/ui/ButtonLink';
import { Button } from '../../../shared/ui/Button';
import { ArrowLeftIcon } from '../../../shared/ui/icons';
import { DeckNotFoundError } from '../../deck/domain/errors';
import { useDeck } from '../../deck/presentation/deckQueries';
import { StudyDeckNotFoundError, StudyFlashcardNotFoundError } from '../domain/errors';
import type { StudyCard } from '../domain/StudyCard';
import { gradeOptions } from './gradeOptions';
import { loadQueueErrorMessage, recordReviewErrorMessage } from './reviewMessages';
import { useRecordReview, useStudyQueue } from './reviewQueries';
import styles from './StudyPage.module.css';

export function StudyPage() {
	const { deckId = '' } = useParams();
	const deck = useDeck(deckId);
	const queue = useStudyQueue(deckId);

	if (deck.isPending || queue.isPending) {
		return <p role="status">Carregando...</p>;
	}
	if (deck.error instanceof DeckNotFoundError || queue.error instanceof StudyDeckNotFoundError) {
		return (
			<div className={styles.message}>
				<h1 className={styles.title}>Baralho não encontrado</h1>
				<p>Esse baralho não existe ou você não tem acesso a ele.</p>
				<ButtonLink to="/">Voltar ao início</ButtonLink>
			</div>
		);
	}
	if (deck.error || queue.error || !deck.data || !queue.data) {
		return <Alert>{loadQueueErrorMessage}</Alert>;
	}
	return <StudySession deckId={deck.data.id} deckName={deck.data.name} cards={queue.data} />;
}

interface StudySessionProps {
	deckId: string;
	deckName: string;
	cards: StudyCard[];
}

function StudySession({ deckId, deckName, cards }: StudySessionProps) {
	const [position, setPosition] = useState(0);
	const [revealed, setRevealed] = useState(false);
	const recordReview = useRecordReview();
	const revealButton = useRef<HTMLButtonElement>(null);
	const answer = useRef<HTMLDivElement>(null);
	const current = cards[position];

	useEffect(() => {
		if (revealed) {
			answer.current?.focus();
		} else {
			revealButton.current?.focus();
		}
	}, [position, revealed]);

	const goToNext = () => {
		recordReview.reset();
		setRevealed(false);
		setPosition((value) => value + 1);
	};

	const grade = (flashcardId: string, selected: (typeof gradeOptions)[number]['grade']) => {
		recordReview.mutate(
			{ deckId, flashcardId, grade: selected },
			{
				onSuccess: goToNext,
				onError: (error) => {
					if (error instanceof StudyFlashcardNotFoundError) goToNext();
				},
			},
		);
	};

	const backLink = (
		<ButtonLink to={`/decks/${deckId}`} variant="secondary">
			<ArrowLeftIcon size={18} />
			Voltar ao baralho
		</ButtonLink>
	);

	if (cards.length === 0) {
		return (
			<div className={styles.message}>
				<h1 className={styles.title}>Nada para estudar agora</h1>
				<p>Você está em dia com &quot;{deckName}&quot;. Volte amanhã, ou adicione novos flashcards ao baralho.</p>
				{backLink}
			</div>
		);
	}
	if (!current) {
		return (
			<div className={styles.message}>
				<h1 className={styles.title}>Sessão concluída</h1>
				<p>
					Você estudou {cards.length} {cards.length === 1 ? 'flashcard' : 'flashcards'} de &quot;{deckName}&quot;.
				</p>
				{backLink}
			</div>
		);
	}

	return (
		<section className={styles.page} aria-labelledby="study-title">
			<header className={styles.header}>
				<h1 id="study-title" className={styles.title}>
					Estudar: {deckName}
				</h1>
				<p className={styles.progressText}>
					Flashcard {position + 1} de {cards.length}
					{current.newCard ? ' · novo' : ''}
				</p>
				<progress className={styles.progress} value={position} max={cards.length} aria-label="Progresso da sessão" />
			</header>
			<article className={styles.card}>
				<span className={styles.label}>Frente</span>
				<p className={styles.text}>{current.front}</p>
				{revealed && (
					<div ref={answer} tabIndex={-1} className={styles.answer}>
						<span className={styles.label}>Verso</span>
						<p className={styles.text}>{current.back}</p>
					</div>
				)}
			</article>
			{recordReview.isError && !(recordReview.error instanceof StudyFlashcardNotFoundError) && (
				<Alert>{recordReviewErrorMessage}</Alert>
			)}
			{revealed ? (
				<div className={styles.grades} role="group" aria-label="Como foi lembrar?">
					{gradeOptions.map((option) => (
						<button
							key={option.grade}
							type="button"
							className={`${styles.grade} ${styles[option.grade]}`}
							disabled={recordReview.isPending}
							onClick={() => grade(current.id, option.grade)}
						>
							<span className={styles.gradeLabel}>{option.label}</span>
							<span className={styles.gradeHint}>{option.hint}</span>
						</button>
					))}
				</div>
			) : (
				<Button ref={revealButton} type="button" fullWidth onClick={() => setRevealed(true)}>
					Mostrar resposta
				</Button>
			)}
		</section>
	);
}
