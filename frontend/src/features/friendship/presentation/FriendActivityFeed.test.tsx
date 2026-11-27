import { screen, within } from '@testing-library/react';
import { http, HttpResponse } from 'msw';
import { beforeEach, describe, expect, it } from 'vitest';
import { renderApp } from '../../../test/renderApp';
import { server } from '../../../test/server';

const path = '*/api/users/me/friends/activity';

describe('friend activity on the home page', () => {
	beforeEach(() => {
		server.use(http.get('*/api/auth/me', () => HttpResponse.json({ id: 'u1', email: 'julia@example.com' })));
	});

	it('tells who reviewed how many flashcards and when', async () => {
		const today = new Date().toISOString().slice(0, 10);
		server.use(
			http.get(path, () =>
				HttpResponse.json([
					{ userId: 'u2', name: 'Márcia', avatar: 'DOG', day: today, reviewedFlashcards: 5 },
					{ userId: 'u2', name: 'Márcia', avatar: 'DOG', day: '2020-01-02', reviewedFlashcards: 1 },
				]),
			),
		);
		renderApp('/');

		const list = await screen.findByRole('list', { name: 'Atividade dos amigos' });
		const [first, second] = within(list).getAllByRole('listitem');

		expect(first).toHaveTextContent('Márcia revisou 5 flashcards');
		expect(first).toHaveTextContent('Hoje');
		expect(second).toHaveTextContent('Márcia revisou 1 flashcard');
		expect(second).toHaveTextContent('2 de janeiro');
	});

	it('says when there is nothing recent', async () => {
		renderApp('/');

		expect(await screen.findByText('Nenhuma atividade recente.')).toBeInTheDocument();
	});

	it('warns when the activity cannot be loaded', async () => {
		server.use(http.get(path, () => HttpResponse.json({}, { status: 500 })));
		renderApp('/');

		expect(await screen.findByRole('alert')).toHaveTextContent('Não foi possível carregar a atividade dos amigos.');
	});
});
