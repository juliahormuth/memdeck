import { InvalidDeckError } from '../domain/errors';
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
