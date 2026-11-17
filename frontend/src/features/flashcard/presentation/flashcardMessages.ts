import { FlashcardDeckNotFoundError, FlashcardNotFoundError, InvalidFlashcardError, NotDeckOwnerError } from '../domain/errors';
import { FLASHCARD_BACK_MAX_LENGTH, FLASHCARD_FRONT_MAX_LENGTH } from '../domain/rules';

export const frontMessages: Record<string, string> = {
	blank: 'Escreva a frente do flashcard.',
	too_long: `A frente deve ter no máximo ${FLASHCARD_FRONT_MAX_LENGTH} caracteres.`,
};

export const backMessages: Record<string, string> = {
	blank: 'Escreva o verso do flashcard.',
	too_long: `O verso deve ter no máximo ${FLASHCARD_BACK_MAX_LENGTH} caracteres.`,
};

export function messageForFlashcardError(error: unknown): string {
	if (error instanceof InvalidFlashcardError) {
		return 'Os dados do flashcard são inválidos. Revise os campos.';
	}
	if (error instanceof NotDeckOwnerError) {
		return 'Só o dono do baralho pode adicionar flashcards.';
	}
	if (error instanceof FlashcardDeckNotFoundError) {
		return 'Esse baralho não existe mais ou você não tem acesso a ele.';
	}
	if (error instanceof FlashcardNotFoundError) {
		return 'Esse flashcard não existe mais.';
	}
	return 'Não foi possível salvar o flashcard. Tente novamente.';
}

export function messageForDeleteFlashcardError(error: unknown): string {
	if (error instanceof NotDeckOwnerError) {
		return 'Só o dono do baralho pode excluir flashcards.';
	}
	if (error instanceof FlashcardNotFoundError) {
		return 'Esse flashcard já não existe.';
	}
	return 'Não foi possível excluir o flashcard. Tente novamente.';
}
