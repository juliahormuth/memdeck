import { FriendshipNotFoundError, InvalidFriendRequestError, TooManySearchesError } from '../domain/errors';

export const requestSentMessage = (name: string) => `Pedido de amizade enviado para ${name}.`;

export const relationLabels = {
	SELF: 'Este é você',
	FRIEND: 'Vocês já são amigos',
	REQUEST_SENT: 'Pedido já enviado',
	REQUEST_RECEIVED: 'Essa pessoa já enviou um pedido para você',
} as const;

export function messageForSearchError(error: unknown): string {
	if (error instanceof TooManySearchesError) {
		return error.retryAfterSeconds
			? `Muitas pesquisas. Tente novamente em ${Math.ceil(error.retryAfterSeconds / 60)} minuto(s).`
			: 'Muitas pesquisas. Tente novamente mais tarde.';
	}
	return 'Não foi possível pesquisar. Tente novamente.';
}

export function messageForSendError(error: unknown): string {
	if (error instanceof InvalidFriendRequestError) {
		return 'Confira o nome de usuário: ele não pode ser o seu.';
	}
	return 'Não foi possível enviar o pedido. Tente novamente.';
}

export function messageForAnswerError(error: unknown): string {
	if (error instanceof FriendshipNotFoundError) {
		return 'Esse pedido não existe mais.';
	}
	return 'Não foi possível concluir a ação. Tente novamente.';
}

export function messageForRemoveFriendError(error: unknown): string {
	if (error instanceof FriendshipNotFoundError) {
		return 'Essa pessoa já não é sua amiga.';
	}
	return 'Não foi possível remover a amizade. Tente novamente.';
}
