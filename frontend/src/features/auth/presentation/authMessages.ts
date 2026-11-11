import {
	EmailAlreadyRegisteredError,
	InvalidCredentialsError,
	InvalidRegistrationError,
	TooManyLoginAttemptsError,
} from '../domain/errors';
import { NAME_MAX_LENGTH, PASSWORD_MAX_LENGTH, PASSWORD_MIN_LENGTH } from '../domain/rules';
import type { FieldErrorCode } from '../domain/rules';

export const nameMessages: Partial<Record<FieldErrorCode, string>> = {
	blank: 'Informe seu nome.',
	too_long: `O nome deve ter no máximo ${NAME_MAX_LENGTH} caracteres.`,
};

export const emailMessages: Partial<Record<FieldErrorCode, string>> = {
	blank: 'Informe o e-mail.',
	invalid: 'Informe um e-mail válido.',
};

export const passwordMessages: Partial<Record<FieldErrorCode, string>> = {
	too_short: `A senha deve ter pelo menos ${PASSWORD_MIN_LENGTH} caracteres.`,
	too_long: `A senha deve ter no máximo ${PASSWORD_MAX_LENGTH} caracteres.`,
};

export function messageForAuthError(error: unknown): string {
	if (error instanceof InvalidCredentialsError) {
		return 'E-mail ou senha inválidos.';
	}
	if (error instanceof TooManyLoginAttemptsError) {
		return error.retryAfterSeconds
			? `Muitas tentativas. Tente novamente em ${Math.ceil(error.retryAfterSeconds / 60)} minuto(s).`
			: 'Muitas tentativas. Tente novamente mais tarde.';
	}
	if (error instanceof EmailAlreadyRegisteredError) {
		return 'Este e-mail já está cadastrado.';
	}
	if (error instanceof InvalidRegistrationError) {
		return 'Os dados informados são inválidos. Revise os campos.';
	}
	return 'Não foi possível concluir a operação. Tente novamente.';
}
