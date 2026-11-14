import type { Deck, Visibility } from '../domain/Deck';
import { InvalidDeckError } from '../domain/errors';
import { validateNewDeck } from '../domain/rules';
import type { DeckGateway } from './DeckGateway';

export interface CreateDeckCommand {
	name: string;
	description: string;
	visibility: Visibility;
}

export async function createDeck(gateway: DeckGateway, command: CreateDeckCommand): Promise<Deck> {
	const errors = validateNewDeck(command);
	if (Object.keys(errors).length > 0) {
		throw new InvalidDeckError(errors);
	}
	const description = command.description.trim();
	return gateway.create({
		name: command.name.trim(),
		description: description.length > 0 ? description : undefined,
		visibility: command.visibility,
	});
}
