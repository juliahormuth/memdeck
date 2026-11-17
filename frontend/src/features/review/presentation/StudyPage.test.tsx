import { screen } from '@testing-library/react';
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
const first = { id: 'f1', front: 'Úmero?', back: 'Osso do braço', newCard: true };
const second = { id: 'f2', front: 'Rádio?', back: 'Osso do antebraço', newCard: false };

function useQueue(cards: unknown[]) {
	server.use(http.get('*/api/decks/d1/study', () => HttpResponse.json(cards)));
}

describe('StudyPage', () => {
	beforeEach(() => {
		server.use(
			http.get('*/api/auth/me', () => HttpResponse.json({ id: 'u1', email: 'julia@example.com' })),
			http.get('*/api/decks/d1', () => HttpResponse.json(deckDto)),
		);
	});

	it('shows the front first and hides the back until the answer is revealed', async () => {
		useQueue([first, second]);
		renderApp('/decks/d1/study');

		expect(await screen.findByText('Úmero?')).toBeInTheDocument();
		expect(screen.getByText('Flashcard 1 de 2 · novo')).toBeInTheDocument();
		expect(screen.queryByText('Osso do braço')).not.toBeInTheDocument();
		expect(screen.queryByRole('button', { name: /Bom/ })).not.toBeInTheDocument();

		await userEvent.click(screen.getByRole('button', { name: 'Mostrar resposta' }));

		expect(screen.getByText('Osso do braço')).toBeInTheDocument();
		expect(screen.getByRole('button', { name: /Não lembro/ })).toBeInTheDocument();
		expect(screen.getByRole('button', { name: /Difícil/ })).toBeInTheDocument();
		expect(screen.getByRole('button', { name: /Bom/ })).toBeInTheDocument();
		expect(screen.getByRole('button', { name: /Fácil/ })).toBeInTheDocument();
	});

	it('sends the grade, moves to the next flashcard and finishes the session', async () => {
		useQueue([first, second]);
		const sent: unknown[] = [];
		server.use(
			http.post('*/api/decks/d1/flashcards/:flashcardId/reviews', async ({ request, params }) => {
				sent.push({ id: params.flashcardId, ...(await request.json() as object) });
				return HttpResponse.json({ dueDate: '2026-01-02', intervalDays: 1 });
			}),
		);
		renderApp('/decks/d1/study');

		await userEvent.click(await screen.findByRole('button', { name: 'Mostrar resposta' }));
		await userEvent.click(screen.getByRole('button', { name: /Bom/ }));

		expect(await screen.findByText('Rádio?')).toBeInTheDocument();
		expect(screen.getByText('Flashcard 2 de 2')).toBeInTheDocument();
		expect(screen.queryByText('Osso do antebraço')).not.toBeInTheDocument();

		await userEvent.click(screen.getByRole('button', { name: 'Mostrar resposta' }));
		await userEvent.click(screen.getByRole('button', { name: /Não lembro/ }));

		expect(await screen.findByRole('heading', { name: 'Sessão concluída' })).toBeInTheDocument();
		expect(screen.getByText(/Você estudou 2 flashcards/)).toBeInTheDocument();
		expect(sent).toEqual([
			{ id: 'f1', grade: 'GOOD' },
			{ id: 'f2', grade: 'AGAIN' },
		]);
		expect(screen.getByRole('link', { name: 'Voltar ao baralho' })).toHaveAttribute('href', '/decks/d1');
	});

	it('tells when there is nothing to study', async () => {
		useQueue([]);
		renderApp('/decks/d1/study');

		expect(await screen.findByRole('heading', { name: 'Nada para estudar agora' })).toBeInTheDocument();
		expect(screen.getByRole('link', { name: 'Voltar ao baralho' })).toBeInTheDocument();
	});

	it('keeps the same flashcard and shows an error when saving the answer fails', async () => {
		useQueue([first, second]);
		server.use(
			http.post('*/api/decks/d1/flashcards/f1/reviews', () => HttpResponse.json({ detail: 'x' }, { status: 500 })),
		);
		renderApp('/decks/d1/study');

		await userEvent.click(await screen.findByRole('button', { name: 'Mostrar resposta' }));
		await userEvent.click(screen.getByRole('button', { name: /Fácil/ }));

		expect(await screen.findByRole('alert')).toHaveTextContent('Não foi possível salvar sua resposta');
		expect(screen.getByText('Úmero?')).toBeInTheDocument();
		expect(screen.getByRole('button', { name: /Fácil/ })).toBeEnabled();
	});

	it('skips a flashcard that no longer exists', async () => {
		useQueue([first, second]);
		server.use(
			http.post('*/api/decks/d1/flashcards/f1/reviews', () => HttpResponse.json({ detail: 'x' }, { status: 404 })),
		);
		renderApp('/decks/d1/study');

		await userEvent.click(await screen.findByRole('button', { name: 'Mostrar resposta' }));
		await userEvent.click(screen.getByRole('button', { name: /Bom/ }));

		expect(await screen.findByText('Rádio?')).toBeInTheDocument();
		expect(screen.queryByRole('alert')).not.toBeInTheDocument();
	});

	it('says the deck was not found for a missing or private deck', async () => {
		server.use(
			http.get('*/api/decks/d1', () => HttpResponse.json({ detail: 'x' }, { status: 404 })),
			http.get('*/api/decks/d1/study', () => HttpResponse.json({ detail: 'x' }, { status: 404 })),
		);
		renderApp('/decks/d1/study');

		expect(await screen.findByRole('heading', { name: 'Baralho não encontrado' })).toBeInTheDocument();
	});

	it('is reachable from the deck page for the owner', async () => {
		useQueue([]);
		renderApp('/decks/d1');

		expect(await screen.findByRole('link', { name: 'Estudar' })).toHaveAttribute('href', '/decks/d1/study');
	});

	it('offers the study button to someone who does not own the deck', async () => {
		server.use(http.get('*/api/decks/d1', () => HttpResponse.json({ ...deckDto, ownedByMe: false, visibility: 'PUBLIC' })));
		renderApp('/decks/d1');

		expect(await screen.findByRole('link', { name: 'Estudar' })).toBeInTheDocument();
		expect(screen.queryByRole('link', { name: 'Editar' })).not.toBeInTheDocument();
	});
});
