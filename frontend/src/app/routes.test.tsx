import { screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { describe, expect, it } from 'vitest';
import { renderApp } from '../test/renderApp';
import { server } from '../test/server';

const julia = { id: 'u1', email: 'julia@example.com' };

function signedIn() {
	server.use(http.get('*/api/auth/me', () => HttpResponse.json(julia)));
}

describe('route guards', () => {
	it('sends a visitor from the home page to the login page', async () => {
		renderApp('/');

		expect(await screen.findByRole('heading', { name: 'Entrar' })).toBeInTheDocument();
	});

	it('sends a signed-in user away from the login page to the home page', async () => {
		signedIn();
		renderApp('/login');

		expect(await screen.findByRole('heading', { name: 'Início' })).toBeInTheDocument();
	});

	it('redirects unknown paths to the home page', async () => {
		signedIn();
		renderApp('/nowhere');

		expect(await screen.findByRole('heading', { name: 'Início' })).toBeInTheDocument();
	});
});

describe('master layout', () => {
	it('shows the main menu with the current item marked', async () => {
		signedIn();
		renderApp('/');

		const menu = await screen.findByRole('navigation', { name: 'Menu principal' });
		expect(within(menu).getByRole('link', { name: 'Início' })).toHaveAttribute('aria-current', 'page');
	});

	it('shows the signed-in user email', async () => {
		signedIn();
		renderApp('/');

		expect(await screen.findByText('julia@example.com')).toBeInTheDocument();
	});

	it('logs out and goes back to the login page', async () => {
		signedIn();
		let loggedOut = false;
		server.use(
			http.post('*/api/auth/logout', () => {
				loggedOut = true;
				server.use(http.get('*/api/auth/me', () => HttpResponse.json({}, { status: 401 })));
				return new HttpResponse(null, { status: 204 });
			}),
		);
		renderApp('/');

		await userEvent.click(await screen.findByRole('button', { name: 'Sair' }));

		expect(await screen.findByRole('heading', { name: 'Entrar' })).toBeInTheDocument();
		expect(loggedOut).toBe(true);
	});
});
