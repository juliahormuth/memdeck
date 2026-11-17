import { expect, test } from '@playwright/test';
import type { Page } from '@playwright/test';
import { createDeckThroughTheUi, registerThroughTheUi, uniqueEmail } from './helpers';

async function addFlashcard(page: Page, front: string, back: string): Promise<void> {
	await page.getByRole('link', { name: 'Novo flashcard' }).click();
	await page.getByLabel('Frente (pergunta, conceito ou palavra)').fill(front);
	await page.getByLabel('Verso (resposta ou tradução)').fill(back);
	await page.getByRole('button', { name: 'Salvar flashcard' }).click();
	await expect(page.getByRole('listitem').filter({ hasText: front })).toBeVisible();
}

test('the owner studies a deck: reveals each answer, grades it, and the studied cards leave today queue', async ({ page }) => {
	await registerThroughTheUi(page, uniqueEmail());
	await createDeckThroughTheUi(page, 'Anatomia humana');
	await addFlashcard(page, 'Qual é o osso mais longo do braço?', 'Úmero');
	await addFlashcard(page, 'Qual osso protege o cérebro?', 'Crânio');

	await page.getByRole('link', { name: 'Estudar' }).click();
	await expect(page.getByText('Flashcard 1 de 2 · novo')).toBeVisible();
	await expect(page.getByText('Qual é o osso mais longo do braço?')).toBeVisible();
	await expect(page.getByText('Úmero')).toBeHidden();

	await page.getByRole('button', { name: 'Mostrar resposta' }).click();
	await expect(page.getByText('Úmero')).toBeVisible();
	await page.getByRole('button', { name: /Bom/ }).click();

	await expect(page.getByText('Flashcard 2 de 2 · novo')).toBeVisible();
	await page.getByRole('button', { name: 'Mostrar resposta' }).click();
	await page.getByRole('button', { name: /Não lembro/ }).click();

	await expect(page.getByRole('heading', { name: 'Sessão concluída' })).toBeVisible();
	await page.getByRole('link', { name: 'Voltar ao baralho' }).click();
	await page.getByRole('link', { name: 'Estudar' }).click();
	await expect(page.getByRole('heading', { name: 'Nada para estudar agora' })).toBeVisible();
});

test('a deck without flashcards has nothing to study', async ({ page }) => {
	await registerThroughTheUi(page, uniqueEmail());
	await createDeckThroughTheUi(page, 'Vazio');

	await page.getByRole('link', { name: 'Estudar' }).click();

	await expect(page.getByRole('heading', { name: 'Nada para estudar agora' })).toBeVisible();
});

test('another user studies a public deck with their own progress, and a private deck stays hidden', async ({ page, browser }) => {
	await registerThroughTheUi(page, uniqueEmail());
	await createDeckThroughTheUi(page, 'Público de estudo', { visibility: 'Público' });
	const publicUrl = page.url();
	await addFlashcard(page, 'Aberto?', 'Sim');
	await page.getByRole('link', { name: 'Estudar' }).click();
	await page.getByRole('button', { name: 'Mostrar resposta' }).click();
	await page.getByRole('button', { name: /Fácil/ }).click();
	await expect(page.getByRole('heading', { name: 'Sessão concluída' })).toBeVisible();
	await createDeckThroughTheUi(page, 'Privado de estudo');
	const privateStudyUrl = `${page.url()}/study`;

	const strangerContext = await browser.newContext();
	const stranger = await strangerContext.newPage();
	await registerThroughTheUi(stranger, uniqueEmail());
	await stranger.goto(`${publicUrl}/study`);
	await expect(stranger.getByText('Aberto?')).toBeVisible();
	await stranger.goto(privateStudyUrl);
	await expect(stranger.getByRole('heading', { name: 'Baralho não encontrado' })).toBeVisible();
	await strangerContext.close();
});
