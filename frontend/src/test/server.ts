import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';

export const server = setupServer(
	http.get('*/api/decks', () => HttpResponse.json([])),
	http.get('*/api/decks/:deckId/flashcards', () => HttpResponse.json([])),
	http.get('*/api/invitations', () => HttpResponse.json([])),
	http.get('*/api/users/me/friend-requests', () => HttpResponse.json([])),
	http.get('*/api/users/me/friends/activity', () => HttpResponse.json([])),
	http.get('*/api/users/me', () => HttpResponse.json({ id: 'u1', name: 'Julia', email: 'julia@example.com', avatar: 'CAT', reviewedDays: 0, streakDays: 0, friends: 0 })),
	http.get('*/api/decks/:deckId/ranking', () => HttpResponse.json([])),
	http.get('*/api/auth/me', () => HttpResponse.json({ status: 401 }, { status: 401, headers: { 'Set-Cookie': 'XSRF-TOKEN=test-token; Path=/' } })),
);
