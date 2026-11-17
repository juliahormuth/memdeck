import { screen, within } from '@testing-library/react';
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
	ownedByMe: true,
};
const otherDeckDto = { ...deckDto, id: 'd2', name: 'Inglês' };
const flashcardDto = { id: 'f1', deckId: 'd1', front: 'Úmero?', back: 'Osso do braço', createdAt: '2026-01-01T12:00:00Z' };

describe('FlashcardEditorPage', () => {
	beforeEach(() => {
		server.use(
			http.get('*/api/auth/me', () => HttpResponse.json({ id: 'u1', email: 'julia@example.com' })),
			http.get('*/api/decks', () => HttpResponse.json([otherDeckDto, deckDto])),
			http.get('*/api/decks/d1', () => HttpResponse.json(deckDto)),
			http.get('*/api/decks/d2', () => HttpResponse.json(otherDeckDto)),
		);
	});

	it('asks for the front and the back before sending anything', async () => {
		renderApp('/decks/d1/flashcards/new');

		await userEvent.click(await screen.findByRole('button', { name: 'Salvar flashcard' }));

		expect(await screen.findByText('Escreva a frente do flashcard.')).toBeInTheDocument();
		expect(screen.getByText('Escreva o verso do flashcard.')).toBeInTheDocument();
	});

	it('rejects a front over 500 characters and a back over 10000', async () => {
		renderApp('/decks/d1/flashcards/new');

		await userEvent.click(await screen.findByLabelText('Frente (pergunta, conceito ou palavra)'));
		await userEvent.paste('a'.repeat(501));
		await userEvent.click(screen.getByLabelText('Verso (resposta ou tradução)'));
		await userEvent.paste('b'.repeat(10001));
		await userEvent.click(screen.getByRole('button', { name: 'Salvar flashcard' }));

		expect(await screen.findByText('A frente deve ter no máximo 500 caracteres.')).toBeInTheDocument();
		expect(screen.getByText('O verso deve ter no máximo 10000 caracteres.')).toBeInTheDocument();
	});

	it('saves the flashcard in the chosen deck and goes back to the deck page', async () => {
		let body: unknown;
		server.use(
			http.post('*/api/decks/d1/flashcards', async ({ request }) => {
				body = await request.json();
				return HttpResponse.json(flashcardDto, { status: 201 });
			}),
			http.get('*/api/decks/d1/flashcards', () => HttpResponse.json([flashcardDto])),
		);
		renderApp('/decks/d1/flashcards/new');

		await userEvent.type(await screen.findByLabelText('Frente (pergunta, conceito ou palavra)'), 'Úmero?');
		await userEvent.type(screen.getByLabelText('Verso (resposta ou tradução)'), 'Osso do braço');
		await userEvent.click(screen.getByRole('button', { name: 'Salvar flashcard' }));

		expect(await screen.findByRole('heading', { name: 'Anatomia' })).toBeInTheDocument();
		expect(await screen.findByText('Úmero?')).toBeInTheDocument();
		expect(body).toEqual({ front: 'Úmero?', back: 'Osso do braço' });
	});

	it('save and create another clears the form and keeps the user on the page', async () => {
		server.use(http.post('*/api/decks/d1/flashcards', () => HttpResponse.json(flashcardDto, { status: 201 })));
		renderApp('/decks/d1/flashcards/new');

		await userEvent.type(await screen.findByLabelText('Frente (pergunta, conceito ou palavra)'), 'Úmero?');
		await userEvent.type(screen.getByLabelText('Verso (resposta ou tradução)'), 'Osso do braço');
		await userEvent.click(screen.getByRole('button', { name: 'Salvar e criar outro' }));

		expect(await screen.findByText('Flashcard salvo. Escreva o próximo.')).toBeInTheDocument();
		expect(screen.getByLabelText('Frente (pergunta, conceito ou palavra)')).toHaveValue('');
		expect(screen.getByLabelText('Verso (resposta ou tradução)')).toHaveValue('');
		expect(screen.getByRole('heading', { name: 'Novo flashcard' })).toBeInTheDocument();
	});

	it('lets the user pick another of their decks without losing what was typed', async () => {
		renderApp('/decks/d1/flashcards/new');

		await userEvent.type(await screen.findByLabelText('Frente (pergunta, conceito ou palavra)'), 'Hello');
		await userEvent.selectOptions(screen.getByLabelText('Baralho'), 'Inglês');

		expect(await screen.findByRole('link', { name: /Inglês/ })).toHaveAttribute('href', '/decks/d2');
		expect(screen.getByLabelText('Baralho')).toHaveValue('d2');
		expect(screen.getByLabelText('Frente (pergunta, conceito ou palavra)')).toHaveValue('Hello');
	});

	it('shows the text being written in the preview, front and back', async () => {
		renderApp('/decks/d1/flashcards/new');
		const preview = await screen.findByLabelText('Pré-visualização');

		await userEvent.type(screen.getByLabelText('Frente (pergunta, conceito ou palavra)'), 'Úmero?');
		await userEvent.type(screen.getByLabelText('Verso (resposta ou tradução)'), 'Osso do braço');

		expect(within(preview).getByText('Úmero?')).toBeInTheDocument();
		await userEvent.click(within(preview).getByRole('button', { name: 'Verso' }));
		expect(within(preview).getByText('Osso do braço')).toBeInTheDocument();
	});

	it('explains the failure when the server refuses', async () => {
		server.use(http.post('*/api/decks/d1/flashcards', () => HttpResponse.json({ detail: 'x' }, { status: 403 })));
		renderApp('/decks/d1/flashcards/new');

		await userEvent.type(await screen.findByLabelText('Frente (pergunta, conceito ou palavra)'), 'a');
		await userEvent.type(screen.getByLabelText('Verso (resposta ou tradução)'), 'b');
		await userEvent.click(screen.getByRole('button', { name: 'Salvar flashcard' }));

		expect(await screen.findByRole('alert')).toHaveTextContent('Só o dono do baralho pode adicionar flashcards.');
	});

	it('does not offer the form for a deck the user does not own', async () => {
		server.use(http.get('*/api/decks/d3', () => HttpResponse.json({ ...deckDto, id: 'd3', ownedByMe: false })));
		renderApp('/decks/d3/flashcards/new');

		expect(await screen.findByText('Só o dono do baralho pode adicionar flashcards.')).toBeInTheDocument();
		expect(screen.queryByRole('button', { name: 'Salvar flashcard' })).not.toBeInTheDocument();
	});

	it('shows not found for a deck that does not exist', async () => {
		server.use(http.get('*/api/decks/missing', () => HttpResponse.json({ detail: 'x' }, { status: 404 })));
		renderApp('/decks/missing/flashcards/new');

		expect(await screen.findByRole('heading', { name: 'Baralho não encontrado' })).toBeInTheDocument();
	});

	describe('editing', () => {
		const second = { ...flashcardDto, id: 'f2', front: 'Rádio?', back: 'Antebraço' };

		beforeEach(() => {
			server.use(http.get('*/api/decks/d1/flashcards', () => HttpResponse.json([second, flashcardDto])));
		});

		it('starts with the text of the flashcard and does not offer create another', async () => {
			renderApp('/decks/d1/flashcards/f1/edit');

			expect(await screen.findByLabelText('Frente (pergunta, conceito ou palavra)')).toHaveValue('Úmero?');
			expect(screen.getByLabelText('Verso (resposta ou tradução)')).toHaveValue('Osso do braço');
			expect(screen.getByRole('heading', { name: 'Editar flashcard' })).toBeInTheDocument();
			expect(screen.getByLabelText('Baralho')).toBeDisabled();
			expect(screen.queryByRole('button', { name: 'Salvar e criar outro' })).not.toBeInTheDocument();
		});

		it('saves the new text and goes back to the deck', async () => {
			let body: unknown;
			server.use(
				http.put('*/api/decks/d1/flashcards/f1', async ({ request }) => {
					body = await request.json();
					return HttpResponse.json({ ...flashcardDto, front: 'Úmero ou rádio?' });
				}),
			);
			renderApp('/decks/d1/flashcards/f1/edit');

			const front = await screen.findByLabelText('Frente (pergunta, conceito ou palavra)');
			await userEvent.clear(front);
			await userEvent.type(front, 'Úmero ou rádio?');
			await userEvent.click(screen.getByRole('button', { name: 'Salvar alterações' }));

			expect(await screen.findByRole('heading', { name: 'Anatomia' })).toBeInTheDocument();
			expect(body).toEqual({ front: 'Úmero ou rádio?', back: 'Osso do braço' });
		});

		it('does not send a blank front', async () => {
			renderApp('/decks/d1/flashcards/f1/edit');

			await userEvent.clear(await screen.findByLabelText('Frente (pergunta, conceito ou palavra)'));
			await userEvent.click(screen.getByRole('button', { name: 'Salvar alterações' }));

			expect(await screen.findByText('Escreva a frente do flashcard.')).toBeInTheDocument();
		});

		it('says so when the flashcard is not in this deck', async () => {
			renderApp('/decks/d1/flashcards/missing/edit');

			expect(await screen.findByRole('heading', { name: 'Flashcard não encontrado' })).toBeInTheDocument();
		});
	});
});
