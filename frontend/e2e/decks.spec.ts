import { expect, test } from '@playwright/test';
import { registerThroughTheUi, uniqueEmail } from './helpers';

test('creating a public deck from the menu opens its page', async ({ page }) => {
	await registerThroughTheUi(page, uniqueEmail());

	await page.getByRole('link', { name: 'Novo baralho', exact: true }).click();
	await expect(page.getByRole('heading', { name: 'Novo baralho' })).toBeVisible();
	await page.getByLabel('Nome').fill('Anatomia humana');
	await page.getByLabel('Descrição (opcional)').fill('Ossos e músculos');
	await page.getByLabel(/Público/).check();
	await page.getByRole('button', { name: 'Criar baralho' }).click();

	await expect(page).toHaveURL(/\/decks\/[0-9a-f-]{36}$/);
	await expect(page.getByRole('heading', { name: 'Anatomia humana' })).toBeVisible();
	await expect(page.getByText('Público')).toBeVisible();
	await expect(page.getByText('Ossos e músculos')).toBeVisible();
});

test('a deck is private by default and its page survives a reload', async ({ page }) => {
	await registerThroughTheUi(page, uniqueEmail());
	await page.goto('/decks/new');

	await page.getByLabel('Nome').fill('Inglês básico');
	await page.getByRole('button', { name: 'Criar baralho' }).click();
	await expect(page.getByRole('heading', { name: 'Inglês básico' })).toBeVisible();
	await expect(page.getByText('Privado')).toBeVisible();
	await expect(page.getByText('Este baralho não tem descrição.')).toBeVisible();

	await page.reload();

	await expect(page.getByRole('heading', { name: 'Inglês básico' })).toBeVisible();
});

test('the form blocks an empty name and keeps the user on the page', async ({ page }) => {
	await registerThroughTheUi(page, uniqueEmail());
	await page.goto('/decks/new');

	await page.getByRole('button', { name: 'Criar baralho' }).click();

	await expect(page.getByText('Informe o nome do baralho.')).toBeVisible();
	await expect(page).toHaveURL(/\/decks\/new$/);
});

test('a deck that does not exist shows the not found page', async ({ page }) => {
	await registerThroughTheUi(page, uniqueEmail());

	await page.goto('/decks/00000000-0000-0000-0000-000000000000');

	await expect(page.getByRole('heading', { name: 'Baralho não encontrado' })).toBeVisible();
});

test('the home page invites the user to create the first deck', async ({ page }) => {
	await registerThroughTheUi(page, uniqueEmail());

	await page.getByRole('link', { name: 'Criar baralho' }).click();

	await expect(page.getByRole('heading', { name: 'Novo baralho' })).toBeVisible();
});

test('a visitor cannot open the new deck form', async ({ page }) => {
	await page.goto('/decks/new');

	await expect(page.getByRole('heading', { name: 'Entrar' })).toBeVisible();
});
