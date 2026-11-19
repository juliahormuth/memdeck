import { FriendshipNotFoundError, InvalidFriendRequestError } from '../domain/errors';

export const requestSentMessage =
	'Pronto. Se esse e-mail tiver uma conta no Memdeck, a pessoa recebe o pedido, que aparece em "Pedidos enviados".';

export function messageForSendError(error: unknown): string {
	if (error instanceof InvalidFriendRequestError) {
		return 'Confira o e-mail: ele precisa ser válido e não pode ser o seu.';
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
