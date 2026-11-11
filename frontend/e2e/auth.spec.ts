import { expect, test } from '@playwright/test';
import { loginThroughTheUi, logoutThroughTheUi, PASSWORD, registerThroughTheUi, uniqueEmail } from './helpers';

test('a visitor is sent to the login page', async ({ page }) => {
	await page.goto('/');

	await expect(page).toHaveURL(/\/login$/);
	await expect(page.getByRole('heading', { name: 'Entrar' })).toBeVisible();
});

test('registering creates the account and lands on the home page with the menu', async ({ page }) => {
	const email = uniqueEmail();

	await registerThroughTheUi(page, email);

	await expect(page.getByRole('navigation', { name: 'Menu principal' }).getByRole('link', { name: 'Início' })).toBeVisible();
	await expect(page.getByRole('button', { name: 'Sair' })).toBeVisible();
});

test('registration rejects invalid fields without leaving the page', async ({ page }) => {
	await page.goto('/register');

	await page.getByLabel('E-mail').fill('not-an-email');
	await page.getByLabel('Senha').fill('short');
	await page.getByRole('button', { name: 'Criar conta' }).click();

	await expect(page.getByText('Informe seu nome.')).toBeVisible();
	await expect(page.getByText('Informe um e-mail válido.')).toBeVisible();
	await expect(page.getByText('A senha deve ter pelo menos 8 caracteres.')).toBeVisible();
	await expect(page).toHaveURL(/\/register$/);
});

test('registering an email that already exists shows an error', async ({ page }) => {
	const email = uniqueEmail();
	await registerThroughTheUi(page, email);
	await logoutThroughTheUi(page);

	await page.goto('/register');
	await page.getByLabel('Nome').fill('Outra Pessoa');
	await page.getByLabel('E-mail').fill(email);
	await page.getByLabel('Senha').fill(PASSWORD);
	await page.getByRole('button', { name: 'Criar conta' }).click();

	await expect(page.getByRole('alert')).toHaveText('Este e-mail já está cadastrado.');
});

test('the session survives a reload and logout ends it', async ({ page }) => {
	await registerThroughTheUi(page, uniqueEmail());

	await page.reload();
	await expect(page.getByRole('heading', { name: 'Início' })).toBeVisible();

	await logoutThroughTheUi(page);
	await page.goto('/');
	await expect(page.getByRole('heading', { name: 'Entrar' })).toBeVisible();
});

test('logging in with the right password enters the home page', async ({ page }) => {
	const email = uniqueEmail();
	await registerThroughTheUi(page, email);
	await logoutThroughTheUi(page);

	await loginThroughTheUi(page, email, PASSWORD);

	await expect(page.getByRole('heading', { name: 'Início' })).toBeVisible();
});

test('a wrong password shows a generic error', async ({ page }) => {
	const email = uniqueEmail();
	await registerThroughTheUi(page, email);
	await logoutThroughTheUi(page);

	await loginThroughTheUi(page, email, 'wrong-password-123');

	await expect(page.getByRole('alert')).toHaveText('E-mail ou senha inválidos.');
	await expect(page).toHaveURL(/\/login$/);
});

test('five failed logins block the account even with the right password', async ({ page }) => {
	const email = uniqueEmail();
	await registerThroughTheUi(page, email);
	await logoutThroughTheUi(page);

	for (let attempt = 0; attempt < 5; attempt++) {
		await loginThroughTheUi(page, email, 'wrong-password-123');
		await expect(page.getByRole('alert')).toHaveText('E-mail ou senha inválidos.');
	}
	await page.getByLabel('Senha').fill(PASSWORD);
	await page.getByRole('button', { name: 'Entrar' }).click();

	await expect(page.getByRole('alert')).toContainText('Muitas tentativas');
	await expect(page.getByRole('heading', { name: 'Entrar' })).toBeVisible();
});
