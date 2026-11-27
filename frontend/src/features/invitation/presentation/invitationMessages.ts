import { InvalidInvitationError, InvitationAlreadyAnsweredError, InvitationDeckNotFoundError, InvitationNotFoundError, NotDeckOwnerError } from '../domain/errors';

export const inviteSentMessage = (name: string) => `Convite enviado para ${name}.`;

export function messageForInviteError(error: unknown): string {
	if (error instanceof InvalidInvitationError) {
		return 'Confira o nome de usuário: ele não pode ser o seu.';
	}
	if (error instanceof NotDeckOwnerError) {
		return 'Só o dono do baralho pode convidar pessoas.';
	}
	if (error instanceof InvitationDeckNotFoundError) {
		return 'Esse baralho não existe mais.';
	}
	return 'Não foi possível enviar o convite. Tente novamente.';
}

export function messageForCancelInvitationError(error: unknown): string {
	if (error instanceof InvitationNotFoundError) {
		return 'Esse convite já não está pendente.';
	}
	return 'Não foi possível cancelar o convite. Tente novamente.';
}

export function messageForAnswerInvitationError(error: unknown): string {
	if (error instanceof InvitationNotFoundError) {
		return 'Esse convite não existe mais.';
	}
	if (error instanceof InvitationAlreadyAnsweredError) {
		return 'Esse convite já foi respondido.';
	}
	return 'Não foi possível responder ao convite. Tente novamente.';
}
