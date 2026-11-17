import { expect } from '@playwright/test';
import type { Page } from '@playwright/test';

export const PASSWORD = 'correct-horse-battery';

export function uniqueEmail(): string {
	return `e2e-${Date.now()}-${Math.random().toString(36).slice(2, 8)}@example.com`;
}

export async function registerThroughTheUi(page: Page, email: string, password = PASSWORD): Promise<void> {
	await page.goto('/register');
	await page.getByLabel('Nome').fill('Pessoa de Teste');
	await page.getByLabel('E-mail').fill(email);
	await page.getByLabel('Senha').fill(password);
	await page.getByRole('button', { name: 'Criar conta' }).click();
	await expect(page.getByRole('heading', { name: 'Início' })).toBeVisible();
}

export async function logoutThroughTheUi(page: Page): Promise<void> {
	await page.getByRole('button', { name: 'Sair' }).click();
	await expect(page.getByRole('heading', { name: 'Entrar' })).toBeVisible();
}

export async function loginThroughTheUi(page: Page, email: string, password: string): Promise<void> {
	await page.getByLabel('E-mail').fill(email);
	await page.getByLabel('Senha').fill(password);
	await page.getByRole('button', { name: 'Entrar' }).click();
}

export async function createDeckThroughTheUi(
	page: Page,
	name: string,
	options: { description?: string; visibility?: 'Público' | 'Privado' } = {},
): Promise<void> {
	await page.goto('/decks/new');
	await page.getByLabel('Nome').fill(name);
	if (options.description) {
		await page.getByLabel('Descrição (opcional)').fill(options.description);
	}
	if (options.visibility === 'Público') {
		await page.getByLabel(/Público/).check();
	}
	await page.getByRole('button', { name: 'Criar baralho' }).click();
	await expect(page.getByRole('heading', { name })).toBeVisible();
}
