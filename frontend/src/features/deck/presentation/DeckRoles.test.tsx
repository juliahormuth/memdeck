import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { beforeEach, describe, expect, it } from 'vitest';
import { renderApp } from '../../../test/renderApp';
import { server } from '../../../test/server';

const deckDto = {
	id: 'd1',
	name: 'Anatomia',
	description: null,
	visibility: 'PRIVATE',
	createdAt: '2026-01-01T12:00:00Z',
	ownedByMe: false,
	role: 'EDITOR',
};
const flashcard = { id: 'f1', deckId: 'd1', front: 'Úmero?', back: 'Osso do braço', createdAt: '2026-01-01T12:00:00Z' };

describe('deck page by role', () => {
	beforeEach(() => {
		server.use(
			http.get('*/api/auth/me', () => HttpResponse.json({ id: 'u1', email: 'julia@example.com' })),
			http.get('*/api/decks/d1', () => HttpResponse.json(deckDto)),
			http.get('*/api/decks/d1/flashcards', () => HttpResponse.json([flashcard])),
		);
	});

	it('lets an editor manage flashcards but not the deck itself', async () => {
		renderApp('/decks/d1');

		expect(await screen.findByRole('link', { name: 'Novo flashcard' })).toBeInTheDocument();
		await userEvent.click(await screen.findByRole('button', { name: 'Exibir flashcards' }));
		expect(await screen.findByRole('link', { name: 'Editar flashcard' })).toBeInTheDocument();
		expect(screen.getByRole('button', { name: 'Excluir flashcard' })).toBeInTheDocument();
		expect(screen.getByText('Você participa como Editor')).toBeInTheDocument();
		expect(screen.queryByRole('link', { name: /^Editar$/ })).not.toBeInTheDocument();
		expect(screen.queryByRole('button', { name: /^Excluir$/ })).not.toBeInTheDocument();
		expect(screen.queryByRole('button', { name: 'Participar' })).not.toBeInTheDocument();
	});

	it('lets a reader study and read but not change anything', async () => {
		server.use(http.get('*/api/decks/d1', () => HttpResponse.json({ ...deckDto, role: 'READER' })));
		renderApp('/decks/d1');

		await userEvent.click(await screen.findByRole('button', { name: 'Exibir flashcards' }));
		await screen.findByText('Úmero?');

		expect(screen.getByRole('link', { name: 'Estudar' })).toBeInTheDocument();
		expect(screen.getByText('Você participa como Leitor')).toBeInTheDocument();
		expect(screen.queryByRole('link', { name: 'Novo flashcard' })).not.toBeInTheDocument();
		expect(screen.queryByRole('link', { name: 'Editar flashcard' })).not.toBeInTheDocument();
	});

	it('offers to leave the deck only to participants who are not the owner', async () => {
		renderApp('/decks/d1');
		expect(await screen.findByRole('button', { name: 'Sair do baralho' })).toBeInTheDocument();
	});

	it('does not offer to leave to the owner nor to someone who has not joined', async () => {
		server.use(http.get('*/api/decks/d1', () => HttpResponse.json({ ...deckDto, ownedByMe: true, role: 'OWNER' })));
		renderApp('/decks/d1');
		await screen.findByRole('heading', { name: 'Anatomia' });
		expect(screen.queryByRole('button', { name: 'Sair do baralho' })).not.toBeInTheDocument();
	});

	it('leaves after confirmation and goes back to the home page', async () => {
		let left = false;
		server.use(
			http.delete('*/api/decks/d1/members/me', () => {
				left = true;
				return new HttpResponse(null, { status: 204 });
			}),
		);
		const { router } = renderApp('/decks/d1');

		await userEvent.click(await screen.findByRole('button', { name: 'Sair do baralho' }));
		const dialog = await screen.findByRole('alertdialog', { name: 'Sair deste baralho?' });
		expect(dialog).toHaveTextContent('só volta a ver o baralho se for convidado de novo');
		await userEvent.click(screen.getAllByRole('button', { name: 'Sair do baralho' }).at(-1)!);

		expect(await screen.findByRole('heading', { name: 'Início' })).toBeInTheDocument();
		expect(left).toBe(true);
		expect(router.state.location.pathname).toBe('/');
	});

	it('lets an editor open the new flashcard form and blocks a reader', async () => {
		renderApp('/decks/d1/flashcards/new');
		expect(await screen.findByRole('heading', { name: 'Novo flashcard' })).toBeInTheDocument();
		expect(screen.getByRole('button', { name: 'Salvar flashcard' })).toBeInTheDocument();
	});

	it('blocks a reader from the new flashcard form with the right message', async () => {
		server.use(http.get('*/api/decks/d1', () => HttpResponse.json({ ...deckDto, role: 'READER' })));
		renderApp('/decks/d1/flashcards/new');

		expect(await screen.findByText('Só o dono ou um editor do baralho pode adicionar flashcards.')).toBeInTheDocument();
		expect(screen.queryByRole('button', { name: 'Salvar flashcard' })).not.toBeInTheDocument();
	});

	it('only offers decks where the user can add flashcards in the Baralho field', async () => {
		const mine = { ...deckDto, id: 'd2', name: 'Meu baralho', ownedByMe: true, role: 'OWNER' };
		const readOnly = { ...deckDto, id: 'd3', name: 'Só leitura', role: 'READER' };
		server.use(http.get('*/api/decks', () => HttpResponse.json([deckDto, mine, readOnly])));
		renderApp('/decks/d1/flashcards/new');

		const select = await screen.findByLabelText('Baralho', { selector: 'select' });

		await waitFor(() =>
			expect([...select.querySelectorAll('option')].map((option) => option.textContent)).toEqual(['Anatomia', 'Meu baralho']),
		);
	});
});
