import { createDeck } from './application/createDeck';
import type { CreateDeckCommand } from './application/createDeck';
import { deleteDeck } from './application/deleteDeck';
import { getDeck } from './application/getDeck';
import { joinDeck } from './application/joinDeck';
import { leaveDeck } from './application/leaveDeck';
import { listDeckMembers } from './application/listDeckMembers';
import { listMyDecks } from './application/listMyDecks';
import { removeDeckMember } from './application/removeDeckMember';
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
	leaveDeck: (id: string) => leaveDeck(gateway, id),
	listDeckMembers: (id: string) => listDeckMembers(gateway, id),
	removeDeckMember: (id: string, userId: string) => removeDeckMember(gateway, id, userId),
};
