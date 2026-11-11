import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { describe, expect, it } from 'vitest';
import { renderApp } from '../../../test/renderApp';
import { server } from '../../../test/server';

async function fillAndSubmit(name: string, email: string, password: string) {
	const nameField = await screen.findByLabelText('Nome');
	if (name) await userEvent.type(nameField, name);
	if (email) await userEvent.type(screen.getByLabelText('E-mail'), email);
	if (password) await userEvent.type(screen.getByLabelText('Senha'), password);
	await userEvent.click(screen.getByRole('button', { name: 'Criar conta' }));
}

describe('RegisterPage', () => {
	it('validates every field before sending anything', async () => {
		renderApp('/register');

		await fillAndSubmit('', 'not-an-email', 'short');

		expect(await screen.findByText('Informe seu nome.')).toBeInTheDocument();
		expect(screen.getByText('Informe um e-mail válido.')).toBeInTheDocument();
		expect(screen.getByText('A senha deve ter pelo menos 8 caracteres.')).toBeInTheDocument();
	});

	it('warns when the email is already registered', async () => {
		server.use(http.post('*/api/auth/register', () => HttpResponse.json({ detail: 'x' }, { status: 409 })));
		renderApp('/register');

		await fillAndSubmit('Julia', 'julia@example.com', 'correct-horse');

		expect(await screen.findByRole('alert')).toHaveTextContent('Este e-mail já está cadastrado.');
	});

	it('creates the account, signs in and lands on the home page', async () => {
		const calls: string[] = [];
		server.use(
			http.post('*/api/auth/register', () => {
				calls.push('register');
				return HttpResponse.json({ id: 'u1', name: 'Julia', email: 'julia@example.com' }, { status: 201 });
			}),
			http.post('*/api/auth/login', () => {
				calls.push('login');
				return HttpResponse.json({ id: 'u1', email: 'julia@example.com' });
			}),
		);
		renderApp('/register');

		await fillAndSubmit('Julia', 'julia@example.com', 'correct-horse');

		expect(await screen.findByRole('heading', { name: 'Início' })).toBeInTheDocument();
		expect(calls).toEqual(['register', 'login']);
	});

	it('links back to the login page', async () => {
		renderApp('/register');

		await userEvent.click(await screen.findByRole('link', { name: 'Entrar' }));

		expect(await screen.findByRole('heading', { name: 'Entrar' })).toBeInTheDocument();
	});
});
