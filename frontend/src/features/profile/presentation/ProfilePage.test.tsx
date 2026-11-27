import { screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { beforeEach, describe, expect, it } from 'vitest';
import { renderApp } from '../../../test/renderApp';
import { server } from '../../../test/server';

const profile = { id: 'u1', name: 'Julia', username: 'julia', email: 'julia@example.com', avatar: 'CAT', reviewedDays: 12, streakDays: 3, friends: 2 };

describe('ProfilePage', () => {
	beforeEach(() => {
		server.use(
			http.get('*/api/auth/me', () => HttpResponse.json({ id: 'u1', email: 'julia@example.com' })),
			http.get('*/api/users/me', () => HttpResponse.json(profile)),
		);
	});

	it('is reached from the avatar in the menu and shows name, e-mail and activity', async () => {
		renderApp('/');

		await userEvent.click(await screen.findByRole('link', { name: 'Meu perfil' }));

		expect(await screen.findByRole('heading', { name: 'Julia' })).toBeInTheDocument();
		expect(screen.getAllByText('julia@example.com').length).toBeGreaterThan(0);
		const stats = screen.getByRole('list', { name: 'Sua atividade' });
		expect(within(stats).getByText('dias revisados').previousSibling).toHaveTextContent('12');
		expect(within(stats).getByText('dias seguidos').previousSibling).toHaveTextContent('3');
		expect(within(stats).getByRole('link', { name: /amigos/ })).toHaveAttribute('href', '/friends');
	});

	it('uses the singular for one day and one friend', async () => {
		server.use(http.get('*/api/users/me', () => HttpResponse.json({ ...profile, reviewedDays: 1, streakDays: 1, friends: 1 })));
		renderApp('/profile');

		const stats = await screen.findByRole('list', { name: 'Sua atividade' });

		expect(within(stats).getByText('dia revisado')).toBeInTheDocument();
		expect(within(stats).getByText('dia seguido')).toBeInTheDocument();
		expect(within(stats).getByText('amigo')).toBeInTheDocument();
	});

	it('changes the name and the icon in one save', async () => {
		let body: unknown;
		server.use(
			http.put('*/api/users/me', async ({ request }) => {
				body = await request.json();
				return HttpResponse.json({ ...profile, name: 'Julia Hormuth', avatar: 'DOG' });
			}),
		);
		renderApp('/profile');

		const name = await screen.findByLabelText('Nome');
		expect(screen.getByLabelText('Gato')).toBeChecked();
		await userEvent.clear(name);
		await userEvent.type(name, '  Julia Hormuth ');
		await userEvent.click(screen.getByLabelText('Cachorro'));
		await userEvent.click(screen.getByRole('button', { name: 'Salvar alterações' }));

		expect(await screen.findByText('Perfil salvo.')).toBeInTheDocument();
		expect(body).toEqual({ name: 'Julia Hormuth', avatar: 'DOG' });
		expect(await screen.findByRole('heading', { name: 'Julia Hormuth' })).toBeInTheDocument();
		expect(screen.getByLabelText('Cachorro')).toBeChecked();
	});

	it('offers every platform icon', async () => {
		renderApp('/profile');

		const group = await screen.findByRole('group', { name: 'Ícone do perfil' });

		expect(within(group).getAllByRole('radio')).toHaveLength(8);
	});

	it('blocks a blank name without calling the server', async () => {
		let called = false;
		server.use(http.put('*/api/users/me', () => ((called = true), HttpResponse.json(profile))));
		renderApp('/profile');

		await userEvent.clear(await screen.findByLabelText('Nome'));
		await userEvent.click(screen.getByRole('button', { name: 'Salvar alterações' }));

		expect(await screen.findByText('Informe seu nome.')).toBeInTheDocument();
		expect(called).toBe(false);
	});

	it('shows an error when saving fails', async () => {
		server.use(http.put('*/api/users/me', () => HttpResponse.json({ detail: 'x' }, { status: 400 })));
		renderApp('/profile');

		await screen.findByLabelText('Nome');
		await userEvent.click(screen.getByRole('button', { name: 'Salvar alterações' }));

		expect(await screen.findByRole('alert')).toHaveTextContent('Os dados do perfil são inválidos');
	});

	it('shows the chosen icon in the menu avatar after saving', async () => {
		renderApp('/profile');
		expect(await screen.findByLabelText('Nome')).toBeInTheDocument();
		expect(screen.getByRole('link', { name: 'Meu perfil' })).toBeInTheDocument();
	});
});
