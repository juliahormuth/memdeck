export const NAME_MAX_LENGTH = 100;
export const EMAIL_MAX_LENGTH = 254;
export const PASSWORD_MIN_LENGTH = 8;
export const PASSWORD_MAX_LENGTH = 128;

const EMAIL_PATTERN = /^[^@\s]+@[^@\s]+\.[^@\s]+$/;

export type RegistrationField = 'name' | 'email' | 'password';
export type FieldErrorCode = 'blank' | 'too_long' | 'too_short' | 'invalid';
export type FieldErrors = Partial<Record<RegistrationField, FieldErrorCode>>;

export function validateName(name: string): FieldErrorCode | null {
	const trimmed = name.trim();
	if (trimmed.length === 0) {
		return 'blank';
	}
	return trimmed.length > NAME_MAX_LENGTH ? 'too_long' : null;
}

export function validateEmail(email: string): FieldErrorCode | null {
	const trimmed = email.trim();
	if (trimmed.length === 0) {
		return 'blank';
	}
	return trimmed.length > EMAIL_MAX_LENGTH || !EMAIL_PATTERN.test(trimmed) ? 'invalid' : null;
}

export function validatePassword(password: string): FieldErrorCode | null {
	if (password.length < PASSWORD_MIN_LENGTH) {
		return 'too_short';
	}
	return password.length > PASSWORD_MAX_LENGTH ? 'too_long' : null;
}

export function validateRegistration(registration: { name: string; email: string; password: string }): FieldErrors {
	const errors: FieldErrors = {};
	const name = validateName(registration.name);
	const email = validateEmail(registration.email);
	const password = validatePassword(registration.password);
	if (name) errors.name = name;
	if (email) errors.email = email;
	if (password) errors.password = password;
	return errors;
}
