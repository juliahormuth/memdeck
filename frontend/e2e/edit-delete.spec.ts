import { expect, test } from '@playwright/test';
import type { Page } from '@playwright/test';
import { createDeckThroughTheUi, registerThroughTheUi, uniqueEmail } from './helpers';

const FRONT = 'Frente (pergunta, conceito ou palavra)';
const BACK = 'Verso (resposta ou tradução)';

async function addFlashcard(page: Page, front: string, back: string): Promise<void> {
	await page.getByRole('link', { name: 'Novo flashcard' }).click();
	await page.getByLabel(FRONT).fill(front);
	await page.getByLabel(BACK).fill(back);
	await page.getByRole('button', { name: 'Salvar flashcard' }).click();
	await expect(page).toHaveURL(/\/decks\/[0-9a-f-]{36}$/);
	await expect(page.getByRole('listitem').filter({ hasText: front })).toBeVisible();
}

test('the owner edits a deck and the change shows on its page and on the home list', async ({ page }) => {
	await registerThroughTheUi(page, uniqueEmail());
	await createDeckThroughTheUi(page, 'Anatomia');

	await page.getByRole('link', { name: 'Editar', exact: true }).click();
	await expect(page.getByLabel('Nome')).toHaveValue('Anatomia');
	await page.getByLabel('Nome').fill('Anatomia humana');
	await page.getByLabel('Descrição (opcional)').fill('Ossos e músculos');
	await page.getByLabel(/Público/).check();
	await page.getByRole('button', { name: 'Salvar alterações' }).click();

	await expect(page.getByRole('heading', { name: 'Anatomia humana' })).toBeVisible();
	await expect(page.getByText('Ossos e músculos')).toBeVisible();
	await expect(page.getByText('Público')).toBeVisible();
	await page.getByRole('navigation', { name: 'Menu principal' }).getByRole('link', { name: 'Início' }).click();
	await expect(page.getByRole('list', { name: 'Seus baralhos' }).getByText('Anatomia humana')).toBeVisible();
});

test('deleting a deck needs confirmation, removes it and its flashcards, and its address stops working', async ({ page }) => {
	await registerThroughTheUi(page, uniqueEmail());
	await createDeckThroughTheUi(page, 'Para apagar');
	await addFlashcard(page, 'Pergunta perdida', 'Resposta perdida');
	const deckUrl = page.url();

	await page.getByRole('button', { name: 'Excluir', exact: true }).click();
	const dialog = page.getByRole('alertdialog', { name: 'Excluir este baralho?' });
	await expect(dialog.getByRole('button', { name: 'Cancelar' })).toBeFocused();
	await dialog.getByRole('button', { name: 'Cancelar' }).click();
	await expect(dialog).toBeHidden();
	await expect(page.getByRole('heading', { name: 'Para apagar' })).toBeVisible();

	await page.getByRole('button', { name: 'Excluir', exact: true }).click();
	await page.getByRole('alertdialog').getByRole('button', { name: 'Excluir baralho' }).click();

	await expect(page.getByRole('heading', { name: 'Início' })).toBeVisible();
	await expect(page.getByText('Para apagar')).toHaveCount(0);
	await page.goto(deckUrl);
	await expect(page.getByRole('heading', { name: 'Baralho não encontrado' })).toBeVisible();
});

test('the owner edits and deletes a flashcard', async ({ page }) => {
	await registerThroughTheUi(page, uniqueEmail());
	await createDeckThroughTheUi(page, 'Anatomia humana');
	await addFlashcard(page, 'Úmero?', 'Osso do braço');
	await addFlashcard(page, 'Rádio?', 'Antebraço');

	await page.getByRole('listitem').filter({ hasText: 'Úmero?' }).getByRole('link', { name: 'Editar flashcard' }).click();
	await expect(page.getByRole('heading', { name: 'Editar flashcard' })).toBeVisible();
	await expect(page.getByLabel(FRONT)).toHaveValue('Úmero?');
	await page.getByLabel(BACK).fill('Osso longo do braço');
	await page.getByRole('button', { name: 'Salvar alterações' }).click();
	await expect(page.getByText('Osso longo do braço')).toBeVisible();
	await expect(page.getByRole('heading', { name: 'Flashcards (2)' })).toBeVisible();

	await page.getByRole('listitem').filter({ hasText: 'Rádio?' }).getByRole('button', { name: 'Excluir flashcard' }).click();
	await page.getByRole('alertdialog').getByRole('button', { name: 'Excluir flashcard' }).click();

	await expect(page.getByRole('heading', { name: 'Flashcards (1)' })).toBeVisible();
	await expect(page.getByText('Rádio?')).toHaveCount(0);
	await page.reload();
	await expect(page.getByRole('heading', { name: 'Flashcards (1)' })).toBeVisible();
	await expect(page.getByText('Osso longo do braço')).toBeVisible();
});

test('another user sees no edit or delete controls on a public deck and cannot open the edit pages', async ({ page, browser }) => {
	await registerThroughTheUi(page, uniqueEmail());
	await createDeckThroughTheUi(page, 'Público alheio', { visibility: 'Público' });
	await addFlashcard(page, 'Aberto?', 'Sim');
	const deckUrl = page.url();
	const flashcardEdit = await page
		.getByRole('link', { name: 'Editar flashcard' })
		.getAttribute('href');

	const strangerContext = await browser.newContext({ baseURL: new URL(deckUrl).origin });
	const stranger = await strangerContext.newPage();
	await registerThroughTheUi(stranger, uniqueEmail());

	await stranger.goto(deckUrl);
	await expect(stranger.getByText('Aberto?')).toBeVisible();
	await expect(stranger.getByRole('link', { name: 'Editar', exact: true })).toHaveCount(0);
	await expect(stranger.getByRole('button', { name: 'Excluir', exact: true })).toHaveCount(0);
	await expect(stranger.getByRole('button', { name: 'Excluir flashcard' })).toHaveCount(0);
	await stranger.goto(`${deckUrl}/edit`);
	await expect(stranger.getByText('Só o dono do baralho pode editá-lo.')).toBeVisible();
	await stranger.goto(flashcardEdit ?? '');
	await expect(stranger.getByText('Só o dono ou um editor do baralho pode editar flashcards.')).toBeVisible();
	await strangerContext.close();
});
