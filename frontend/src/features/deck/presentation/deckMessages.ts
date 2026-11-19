import { AlreadyDeckMemberError, DeckNotFoundError, InvalidDeckError, NotDeckOwnerError } from '../domain/errors';
import { DECK_DESCRIPTION_MAX_LENGTH, DECK_NAME_MAX_LENGTH } from '../domain/rules';
import type { DeckRole, Visibility } from '../domain/Deck';

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

export const roleLabels: Record<DeckRole, string> = {
	OWNER: 'Dono',
	EDITOR: 'Editor',
	READER: 'Leitor',
};

export const roleDescriptions: Record<DeckRole, string> = {
	OWNER: 'Dono do baralho.',
	EDITOR: 'Pode estudar e também criar, editar e excluir flashcards.',
	READER: 'Pode estudar o baralho e ver seus flashcards.',
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

export function messageForJoinDeckError(error: unknown): string {
	if (error instanceof AlreadyDeckMemberError) {
		return 'Você já participa deste baralho.';
	}
	if (error instanceof DeckNotFoundError) {
		return 'Esse baralho não está mais disponível.';
	}
	return 'Não foi possível entrar no baralho. Tente novamente.';
}

export function messageForLeaveDeckError(error: unknown): string {
	if (error instanceof DeckNotFoundError) {
		return 'Você já não participa deste baralho.';
	}
	return 'Não foi possível sair do baralho. Tente novamente.';
}

export function messageForRemoveMemberError(error: unknown): string {
	if (error instanceof NotDeckOwnerError) {
		return 'Só o dono do baralho pode remover participantes.';
	}
	if (error instanceof DeckNotFoundError) {
		return 'Essa pessoa já não participa do baralho.';
	}
	return 'Não foi possível remover o participante. Tente novamente.';
}
