import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { describe, expect, it } from 'vitest';
import { renderApp } from '../../../test/renderApp';
import { server } from '../../../test/server';

async function fillAndSubmit(email: string, password: string) {
	await userEvent.type(await screen.findByLabelText('E-mail'), email);
	await userEvent.type(screen.getByLabelText('Senha'), password);
	await userEvent.click(screen.getByRole('button', { name: 'Entrar' }));
}

describe('LoginPage', () => {
	it('asks for the email and the password before sending anything', async () => {
		renderApp('/login');

		await userEvent.click(await screen.findByRole('button', { name: 'Entrar' }));

		expect(await screen.findByText('Informe o e-mail.')).toBeInTheDocument();
		expect(screen.getByText('Informe a senha.')).toBeInTheDocument();
	});

	it('shows a generic message for wrong credentials', async () => {
		server.use(http.post('*/api/auth/login', () => HttpResponse.json({ detail: 'x' }, { status: 401 })));
		renderApp('/login');

		await fillAndSubmit('julia@example.com', 'wrong-password');

		expect(await screen.findByRole('alert')).toHaveTextContent('E-mail ou senha inválidos.');
	});

	it('tells how long to wait when the account is temporarily blocked', async () => {
		server.use(
			http.post('*/api/auth/login', () =>
				HttpResponse.json({ detail: 'x' }, { status: 429, headers: { 'Retry-After': '900' } }),
			),
		);
		renderApp('/login');

		await fillAndSubmit('julia@example.com', 'correct-horse');

		expect(await screen.findByRole('alert')).toHaveTextContent('Tente novamente em 15 minuto(s).');
	});

	it('enters the home page after a successful login', async () => {
		server.use(http.post('*/api/auth/login', () => HttpResponse.json({ id: 'u1', email: 'julia@example.com' })));
		renderApp('/login');

		await fillAndSubmit('julia@example.com', 'correct-horse');

		expect(await screen.findByRole('heading', { name: 'Início' })).toBeInTheDocument();
	});

	it('links to the registration page', async () => {
		renderApp('/login');

		await userEvent.click(await screen.findByRole('link', { name: 'Cadastre-se' }));

		expect(await screen.findByRole('heading', { name: 'Criar conta' })).toBeInTheDocument();
	});
});
