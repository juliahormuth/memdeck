export const PROFILE_NAME_MAX_LENGTH = 100;

export type ProfileNameError = 'blank' | 'too_long';

export function validateProfileName(name: string): ProfileNameError | null {
	const trimmed = name.trim();
	if (trimmed.length === 0) {
		return 'blank';
	}
	return trimmed.length > PROFILE_NAME_MAX_LENGTH ? 'too_long' : null;
}
