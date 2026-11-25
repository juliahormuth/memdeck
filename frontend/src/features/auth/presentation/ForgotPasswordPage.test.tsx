import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { describe, expect, it } from 'vitest';
import { renderApp } from '../../../test/renderApp';
import { server } from '../../../test/server';

function acceptCodeRequests() {
	const requests: unknown[] = [];
	server.use(
		http.post('*/api/auth/password-reset', async ({ request }) => {
			requests.push(await request.json());
			return new HttpResponse(null, { status: 202 });
		}),
	);
	return requests;
}

async function askForCode(email: string) {
	await userEvent.type(await screen.findByLabelText('E-mail'), email);
	await userEvent.click(screen.getByRole('button', { name: 'Enviar código' }));
}

async function changePassword(code: string, password: string) {
	await userEvent.type(await screen.findByLabelText('Código'), code);
	await userEvent.type(screen.getByLabelText('Nova senha'), password);
	await userEvent.click(screen.getByRole('button', { name: 'Alterar senha' }));
}

describe('ForgotPasswordPage', () => {
	it('is reached from the login page', async () => {
		renderApp('/login');

		await userEvent.click(await screen.findByRole('link', { name: 'Esqueci minha senha' }));

		expect(await screen.findByRole('heading', { name: 'Recuperar conta' })).toBeInTheDocument();
	});

	it('asks for a code and never says whether the account exists', async () => {
		const requests = acceptCodeRequests();
		renderApp('/forgot-password');

		await askForCode('julia@example.com');

		expect(await screen.findByText(/Se houver uma conta para/)).toBeInTheDocument();
		expect(requests).toEqual([{ email: 'julia@example.com' }]);
	});

	it('does not send an invalid email', async () => {
		const requests = acceptCodeRequests();
		renderApp('/forgot-password');

		await askForCode('nope');

		expect(await screen.findByText('Informe um e-mail válido.')).toBeInTheDocument();
		expect(requests).toEqual([]);
	});

	it('changes the password and returns to the login page', async () => {
		acceptCodeRequests();
		let body: unknown;
		server.use(
			http.post('*/api/auth/password-reset/confirm', async ({ request }) => {
				body = await request.json();
				return new HttpResponse(null, { status: 204 });
			}),
		);
		renderApp('/forgot-password');
		await askForCode('julia@example.com');

		await changePassword('123456', 'brand-new-password');

		expect(await screen.findByRole('status')).toHaveTextContent('Senha alterada. Entre com a nova senha.');
		expect(screen.getByRole('heading', { name: 'Entrar' })).toBeInTheDocument();
		expect(body).toEqual({ email: 'julia@example.com', code: '123456', newPassword: 'brand-new-password' });
	});

	it('tells when the code is wrong or expired', async () => {
		acceptCodeRequests();
		server.use(
			http.post('*/api/auth/password-reset/confirm', () => HttpResponse.json({ detail: 'x' }, { status: 400 })),
		);
		renderApp('/forgot-password');
		await askForCode('julia@example.com');

		await changePassword('000000', 'brand-new-password');

		expect(await screen.findByRole('alert')).toHaveTextContent('Código inválido ou expirado.');
	});

	it('checks the code and the password before sending them', async () => {
		acceptCodeRequests();
		renderApp('/forgot-password');
		await askForCode('julia@example.com');

		await changePassword('12', 'short');

		expect(await screen.findByText('O código tem 6 números.')).toBeInTheDocument();
		expect(screen.getByText('A senha deve ter pelo menos 8 caracteres.')).toBeInTheDocument();
	});

	it('sends another code on request', async () => {
		const requests = acceptCodeRequests();
		renderApp('/forgot-password');
		await askForCode('julia@example.com');

		await userEvent.click(await screen.findByRole('button', { name: 'Reenviar código' }));

		expect(await screen.findByRole('status')).toHaveTextContent('Aguarde um minuto');
		expect(requests).toHaveLength(2);
	});
});
