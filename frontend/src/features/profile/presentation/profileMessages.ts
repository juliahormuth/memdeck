import { InvalidProfileError } from '../domain/errors';
import { PROFILE_NAME_MAX_LENGTH } from '../domain/rules';
import type { AvatarKey } from '../domain/Profile';

export const nameMessages: Record<string, string> = {
	blank: 'Informe seu nome.',
	too_long: `O nome deve ter no máximo ${PROFILE_NAME_MAX_LENGTH} caracteres.`,
};

export const avatarLabels: Record<AvatarKey, string> = {
	BOOK: 'Livro',
	LAYERS: 'Camadas',
	STAR: 'Estrela',
	GLOBE: 'Globo',
	CLOCK: 'Relógio',
	PENCIL: 'Lápis',
	PLAY: 'Play',
	USERS: 'Pessoas',
};

export function messageForProfileError(error: unknown): string {
	if (error instanceof InvalidProfileError) {
		return 'Os dados do perfil são inválidos. Revise o nome.';
	}
	return 'Não foi possível salvar o perfil. Tente novamente.';
}
