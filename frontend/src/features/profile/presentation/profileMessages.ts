import { InvalidProfileError } from '../domain/errors';
import { PROFILE_NAME_MAX_LENGTH } from '../domain/rules';
import type { AvatarKey } from '../domain/Profile';

export const nameMessages: Record<string, string> = {
	blank: 'Informe seu nome.',
	too_long: `O nome deve ter no máximo ${PROFILE_NAME_MAX_LENGTH} caracteres.`,
};

export const avatarLabels: Record<AvatarKey, string> = {
	CAT: 'Gato',
	DOG: 'Cachorro',
	RABBIT: 'Coelho',
	BIRD: 'Passarinho',
	FISH: 'Peixe',
	TURTLE: 'Tartaruga',
	SQUIRREL: 'Esquilo',
	PANDA: 'Panda',
};

export function messageForProfileError(error: unknown): string {
	if (error instanceof InvalidProfileError) {
		return 'Os dados do perfil são inválidos. Revise o nome.';
	}
	return 'Não foi possível salvar o perfil. Tente novamente.';
}
