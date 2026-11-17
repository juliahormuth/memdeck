import { DeckNotFoundError, InvalidDeckError, NotDeckOwnerError } from '../domain/errors';
import { DECK_DESCRIPTION_MAX_LENGTH, DECK_NAME_MAX_LENGTH } from '../domain/rules';
import type { Visibility } from '../domain/Deck';

export const nameMessages: Record<string, string> = {
	blank: 'Informe o nome do baralho.',
	too_long: `O nome deve ter no máximo ${DECK_NAME_MAX_LENGTH} caracteres.`,
};

export const descriptionMessages: Record<string, string> = {
	too_long: `A descrição deve ter no máximo ${DECK_DESCRIPTION_MAX_LENGTH} caracteres.`,
};

export const visibilityLabels: Record<Visibility, string> = {
	PUBLIC: 'Público',
	PRIVATE: 'Privado',
};

export function messageForDeckError(error: unknown): string {
	if (error instanceof InvalidDeckError) {
		return 'Os dados do baralho são inválidos. Revise os campos.';
	}
	return 'Não foi possível criar o baralho. Tente novamente.';
}

export function messageForUpdateDeckError(error: unknown): string {
	if (error instanceof InvalidDeckError) {
		return 'Os dados do baralho são inválidos. Revise os campos.';
	}
	if (error instanceof NotDeckOwnerError) {
		return 'Só o dono do baralho pode editá-lo.';
	}
	if (error instanceof DeckNotFoundError) {
		return 'Esse baralho não existe mais.';
	}
	return 'Não foi possível salvar as alterações. Tente novamente.';
}

export function messageForDeleteDeckError(error: unknown): string {
	if (error instanceof NotDeckOwnerError) {
		return 'Só o dono do baralho pode excluí-lo.';
	}
	if (error instanceof DeckNotFoundError) {
		return 'Esse baralho já não existe.';
	}
	return 'Não foi possível excluir o baralho. Tente novamente.';
}
