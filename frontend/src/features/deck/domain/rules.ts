export const DECK_NAME_MAX_LENGTH = 100;
export const DECK_DESCRIPTION_MAX_LENGTH = 500;

export type DeckField = 'name' | 'description';
export type DeckFieldErrorCode = 'blank' | 'too_long';
export type DeckFieldErrors = Partial<Record<DeckField, DeckFieldErrorCode>>;

export function validateDeckName(name: string): DeckFieldErrorCode | null {
	const trimmed = name.trim();
	if (trimmed.length === 0) {
		return 'blank';
	}
	return trimmed.length > DECK_NAME_MAX_LENGTH ? 'too_long' : null;
}

export function validateDeckDescription(description: string): DeckFieldErrorCode | null {
	return description.length > DECK_DESCRIPTION_MAX_LENGTH ? 'too_long' : null;
}

export function validateNewDeck(deck: { name: string; description: string }): DeckFieldErrors {
	const errors: DeckFieldErrors = {};
	const name = validateDeckName(deck.name);
	const description = validateDeckDescription(deck.description);
	if (name) errors.name = name;
	if (description) errors.description = description;
	return errors;
}
