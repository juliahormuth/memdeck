import { createDeck } from './application/createDeck';
import type { CreateDeckCommand } from './application/createDeck';
import { getDeck } from './application/getDeck';
import { HttpDeckGateway } from './infrastructure/HttpDeckGateway';

const gateway = new HttpDeckGateway();

export const deckUseCases = {
	createDeck: (command: CreateDeckCommand) => createDeck(gateway, command),
	getDeck: (id: string) => getDeck(gateway, id),
};
