import { expect, test } from '@playwright/test';
import type { Page } from '@playwright/test';
import { loginThroughTheUi, logoutThroughTheUi, PASSWORD, registerThroughTheUi, uniqueEmail } from './helpers';

async function expireTheSession(page: Page): Promise<void> {
	await page.context().clearCookies({ name: 'SESSION' });
}

async function comeBackToTheTab(page: Page): Promise<void> {
	await page.evaluate(() => document.dispatchEvent(new Event('visibilitychange')));
}

test('saving after the session expired shows the notice, and signing in again returns to the form', async ({ page }) => {
	const email = uniqueEmail();
	await registerThroughTheUi(page, email);
	await page.getByRole('link', { name: 'Novo baralho', exact: true }).click();
	await page.getByLabel('Nome').fill('Baralho que não foi salvo');
	await expireTheSession(page);

	await page.getByRole('button', { name: 'Criar baralho' }).click();

	const dialog = page.getByRole('alertdialog', { name: 'Sua sessão expirou' });
	await expect(dialog).toBeVisible();
	await expect(dialog.getByRole('button', { name: 'Entrar novamente' })).toBeFocused();

	await dialog.getByRole('button', { name: 'Entrar novamente' }).click();

	await expect(page.getByRole('heading', { name: 'Entrar' })).toBeVisible();
	await expect(page).toHaveURL(/\/login\?redirect=%2Fdecks%2Fnew$/);

	await loginThroughTheUi(page, email, PASSWORD);

	await expect(page.getByRole('heading', { name: 'Novo baralho' })).toBeVisible();
	await expect(page).toHaveURL(/\/decks\/new$/);
});

test('the notice appears as soon as the user comes back to the tab', async ({ page }) => {
	await registerThroughTheUi(page, uniqueEmail());

	await expireTheSession(page);
	await comeBackToTheTab(page);

	await expect(page.getByRole('alertdialog', { name: 'Sua sessão expirou' })).toBeVisible();
});

test('Escape does the same as the button', async ({ page }) => {
	await registerThroughTheUi(page, uniqueEmail());
	await expireTheSession(page);
	await comeBackToTheTab(page);
	await expect(page.getByRole('alertdialog')).toBeVisible();

	await page.keyboard.press('Escape');

	await expect(page.getByRole('heading', { name: 'Entrar' })).toBeVisible();
});

test('the expired session leaves no deck of the previous user on the screen', async ({ page }) => {
	await registerThroughTheUi(page, uniqueEmail());
	await page.goto('/decks/new');
	await page.getByLabel('Nome').fill('Privado da sessão antiga');
	await page.getByRole('button', { name: 'Criar baralho' }).click();
	await expect(page.getByRole('heading', { name: 'Privado da sessão antiga' })).toBeVisible();

	await expireTheSession(page);
	await comeBackToTheTab(page);
	await page.getByRole('button', { name: 'Entrar novamente' }).click();

	await expect(page.getByRole('heading', { name: 'Entrar' })).toBeVisible();
	await expect(page.getByText('Privado da sessão antiga')).toHaveCount(0);
});

test('opening a protected link as a visitor goes to the login page and then to that link', async ({ page }) => {
	const email = uniqueEmail();
	await registerThroughTheUi(page, email);
	await logoutThroughTheUi(page);

	await page.goto('/decks/new');

	await expect(page).toHaveURL(/\/login\?redirect=%2Fdecks%2Fnew$/);
	await loginThroughTheUi(page, email, PASSWORD);
	await expect(page.getByRole('heading', { name: 'Novo baralho' })).toBeVisible();
});

test('a wrong password does not show the expired session notice', async ({ page }) => {
	const email = uniqueEmail();
	await registerThroughTheUi(page, email);
	await logoutThroughTheUi(page);

	await loginThroughTheUi(page, email, 'wrong-password-123');

	await expect(page.getByRole('alert')).toHaveText('E-mail ou senha inválidos.');
	await expect(page.getByRole('alertdialog')).toHaveCount(0);
});
