import { createDeck } from './application/createDeck';
import type { CreateDeckCommand } from './application/createDeck';
import { deleteDeck } from './application/deleteDeck';
import { getDeck } from './application/getDeck';
import { joinDeck } from './application/joinDeck';
import { listMyDecks } from './application/listMyDecks';
import { searchPublicDecks } from './application/searchPublicDecks';
import { updateDeck } from './application/updateDeck';
import type { UpdateDeckCommand } from './application/updateDeck';
import { HttpDeckGateway } from './infrastructure/HttpDeckGateway';

const gateway = new HttpDeckGateway();

export const deckUseCases = {
	createDeck: (command: CreateDeckCommand) => createDeck(gateway, command),
	getDeck: (id: string) => getDeck(gateway, id),
	listMyDecks: () => listMyDecks(gateway),
	updateDeck: (command: UpdateDeckCommand) => updateDeck(gateway, command),
	deleteDeck: (id: string) => deleteDeck(gateway, id),
	searchPublicDecks: (term: string) => searchPublicDecks(gateway, term),
	joinDeck: (id: string) => joinDeck(gateway, id),
};
