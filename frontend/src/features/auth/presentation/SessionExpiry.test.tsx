import { fireEvent, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { describe, expect, it } from 'vitest';
import { renderApp } from '../../../test/renderApp';
import { server } from '../../../test/server';

const julia = { id: 'u1', email: 'julia@example.com' };

function signedIn() {
	server.use(http.get('*/api/auth/me', () => HttpResponse.json(julia)));
}

function expiredOn(method: 'get' | 'post', path: string) {
	server.use(http[method](`*${path}`, () => HttpResponse.json({}, { status: 401 })));
}

describe('session expiry', () => {
	it('does not show the notice while the session is fine', async () => {
		signedIn();
		renderApp('/');

		expect(await screen.findByRole('heading', { name: 'Início' })).toBeInTheDocument();
		expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument();
	});

	it('does not show the notice to a visitor who just opened the site', async () => {
		renderApp('/');

		expect(await screen.findByRole('heading', { name: 'Entrar' })).toBeInTheDocument();
		expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument();
	});

	it('does not show the notice for a wrong password', async () => {
		server.use(http.post('*/api/auth/login', () => HttpResponse.json({ detail: 'x' }, { status: 401 })));
		renderApp('/login');

		await userEvent.type(await screen.findByLabelText('E-mail'), 'julia@example.com');
		await userEvent.type(screen.getByLabelText('Senha'), 'wrong-password');
		await userEvent.click(screen.getByRole('button', { name: 'Entrar' }));

		expect(await screen.findByRole('alert')).toHaveTextContent('E-mail ou senha inválidos.');
		expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument();
	});

	it('explains the expiry in a modal when a request is refused', async () => {
		signedIn();
		expiredOn('get', '/api/decks');
		renderApp('/');

		const dialog = await screen.findByRole('alertdialog', { name: 'Sua sessão expirou' });
		expect(dialog).toHaveTextContent('Por segurança, você foi desconectado');
		expect(screen.getByRole('button', { name: 'Entrar novamente' })).toBeInTheDocument();
	});

	it('goes to the login page when the user chooses to sign in again', async () => {
		signedIn();
		expiredOn('get', '/api/decks');
		const { router } = renderApp('/');

		await userEvent.click(await screen.findByRole('button', { name: 'Entrar novamente' }));

		expect(await screen.findByRole('heading', { name: 'Entrar' })).toBeInTheDocument();
		expect(router.state.location.pathname).toBe('/login');
		expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument();
	});

	it('treats Escape like the button, because nothing works without a session', async () => {
		signedIn();
		expiredOn('get', '/api/decks');
		renderApp('/');

		const dialog = await screen.findByRole('alertdialog');
		fireEvent(dialog, new Event('cancel', { cancelable: true }));

		expect(await screen.findByRole('heading', { name: 'Entrar' })).toBeInTheDocument();
	});

	it('brings the user back to the page they were on after signing in again', async () => {
		signedIn();
		expiredOn('post', '/api/decks');
		const { router } = renderApp('/decks/new');

		await userEvent.type(await screen.findByLabelText('Nome'), 'Anatomia');
		await userEvent.click(screen.getByRole('button', { name: 'Criar baralho' }));
		await userEvent.click(await screen.findByRole('button', { name: 'Entrar novamente' }));

		expect(await screen.findByRole('heading', { name: 'Entrar' })).toBeInTheDocument();
		expect(router.state.location.search).toBe('?redirect=%2Fdecks%2Fnew');

		server.use(http.post('*/api/auth/login', () => HttpResponse.json(julia)));
		await userEvent.type(screen.getByLabelText('E-mail'), 'julia@example.com');
		await userEvent.type(screen.getByLabelText('Senha'), 'correct-horse');
		await userEvent.click(screen.getByRole('button', { name: 'Entrar' }));

		expect(await screen.findByRole('heading', { name: 'Novo baralho' })).toBeInTheDocument();
		expect(router.state.location.pathname).toBe('/decks/new');
	});

	it('forgets the cached decks of the expired session', async () => {
		signedIn();
		server.use(
			http.get('*/api/decks', () =>
				HttpResponse.json([{ id: 'd1', name: 'Segredo', description: null, visibility: 'PRIVATE', createdAt: '2026-01-01T10:00:00Z' }]),
			),
		);
		renderApp('/');
		expect(await screen.findByRole('link', { name: 'Segredo' })).toBeInTheDocument();

		server.use(http.get('*/api/auth/me', () => HttpResponse.json({}, { status: 401 })));
		Object.defineProperty(document, 'visibilityState', { value: 'visible', configurable: true });
		document.dispatchEvent(new Event('visibilitychange'));
		await userEvent.click(await screen.findByRole('button', { name: 'Entrar novamente' }));

		expect(await screen.findByRole('heading', { name: 'Entrar' })).toBeInTheDocument();
		expect(screen.queryByText('Segredo')).not.toBeInTheDocument();
	});

	it('checks the session when the user comes back to the tab', async () => {
		signedIn();
		renderApp('/');
		expect(await screen.findByRole('heading', { name: 'Início' })).toBeInTheDocument();

		server.use(http.get('*/api/auth/me', () => HttpResponse.json({}, { status: 401 })));
		Object.defineProperty(document, 'visibilityState', { value: 'visible', configurable: true });
		document.dispatchEvent(new Event('visibilitychange'));

		expect(await screen.findByRole('alertdialog', { name: 'Sua sessão expirou' })).toBeInTheDocument();
	});
});

describe('deep links', () => {
	it('sends a visitor to the login page and back to the page that was asked for', async () => {
		const { router } = renderApp('/decks/new');

		expect(await screen.findByRole('heading', { name: 'Entrar' })).toBeInTheDocument();
		expect(router.state.location.search).toBe('?redirect=%2Fdecks%2Fnew');

		server.use(http.post('*/api/auth/login', () => HttpResponse.json(julia)));
		await userEvent.type(screen.getByLabelText('E-mail'), 'julia@example.com');
		await userEvent.type(screen.getByLabelText('Senha'), 'correct-horse');
		await userEvent.click(screen.getByRole('button', { name: 'Entrar' }));

		expect(await screen.findByRole('heading', { name: 'Novo baralho' })).toBeInTheDocument();
	});

	it('ignores a redirect to another site', async () => {
		signedIn();
		const { router } = renderApp('/login?redirect=//evil.com');

		expect(await screen.findByRole('heading', { name: 'Início' })).toBeInTheDocument();
		expect(router.state.location.pathname).toBe('/');
	});
});
