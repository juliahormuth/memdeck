import { expect, test } from '@playwright/test';
import { createDeckThroughTheUi, registerThroughTheUi, uniqueEmail } from './helpers';

test('the owner adds flashcards to a deck and sees them listed, newest first', async ({ page }) => {
	await registerThroughTheUi(page, uniqueEmail());
	await createDeckThroughTheUi(page, 'Anatomia humana');
	await expect(page.getByText('Este baralho ainda não tem flashcards. Crie o primeiro.')).toBeVisible();

	await page.getByRole('link', { name: 'Novo flashcard' }).click();
	await expect(page.getByRole('heading', { name: 'Novo flashcard' })).toBeVisible();
	await page.getByLabel('Frente (pergunta, conceito ou palavra)').fill('Qual é o osso mais longo do braço?');
	await page.getByLabel('Verso (resposta ou tradução)').fill('Úmero');
	await page.getByRole('button', { name: 'Salvar e criar outro' }).click();

	await expect(page.getByText('Flashcard salvo. Escreva o próximo.')).toBeVisible();
	await expect(page.getByLabel('Frente (pergunta, conceito ou palavra)')).toHaveValue('');
	await expect(page.getByLabel('Frente (pergunta, conceito ou palavra)')).toBeFocused();
	await page.getByLabel('Frente (pergunta, conceito ou palavra)').fill('Qual osso protege o cérebro?');
	await page.getByLabel('Verso (resposta ou tradução)').fill('Crânio');
	await page.getByRole('button', { name: 'Salvar flashcard' }).click();

	await expect(page.getByRole('heading', { name: 'Flashcards (2)' })).toBeVisible();
	const rows = page.getByRole('listitem').filter({ hasText: 'Frente' });
	await expect(rows.first()).toContainText('Qual osso protege o cérebro?');
	await expect(rows.last()).toContainText('Úmero');
	await page.reload();
	await expect(page.getByRole('heading', { name: 'Flashcards (2)' })).toBeVisible();
});

test('the form blocks empty text and the preview follows what is typed', async ({ page }) => {
	await registerThroughTheUi(page, uniqueEmail());
	await createDeckThroughTheUi(page, 'Inglês básico');
	await page.getByRole('link', { name: 'Novo flashcard' }).click();

	await page.getByRole('button', { name: 'Salvar flashcard' }).click();
	await expect(page.getByText('Escreva a frente do flashcard.')).toBeVisible();
	await expect(page.getByText('Escreva o verso do flashcard.')).toBeVisible();

	await page.getByLabel('Frente (pergunta, conceito ou palavra)').fill('Hello');
	await page.getByLabel('Verso (resposta ou tradução)').fill('Olá');
	const preview = page.getByLabel('Pré-visualização');
	await expect(preview.getByText('Hello')).toBeVisible();
	await preview.getByRole('button', { name: 'Verso' }).click();
	await expect(preview.getByText('Olá')).toBeVisible();
});

test('the Baralho field moves the flashcard to another deck of the user', async ({ page }) => {
	await registerThroughTheUi(page, uniqueEmail());
	await createDeckThroughTheUi(page, 'Anatomia humana');
	await createDeckThroughTheUi(page, 'Inglês básico');
	await page.getByRole('link', { name: 'Novo flashcard' }).click();

	await page.getByLabel('Baralho', { exact: true }).selectOption({ label: 'Anatomia humana' });
	await page.getByLabel('Frente (pergunta, conceito ou palavra)').fill('Femur');
	await page.getByLabel('Verso (resposta ou tradução)').fill('Osso da coxa');
	await page.getByRole('button', { name: 'Salvar flashcard' }).click();

	await expect(page.getByRole('heading', { name: 'Anatomia humana' })).toBeVisible();
	await expect(page.getByRole('heading', { name: 'Flashcards (1)' })).toBeVisible();
});

test('another user can read a public deck flashcards but cannot add to it; a private deck stays hidden', async ({
	page,
	browser,
}) => {
	await registerThroughTheUi(page, uniqueEmail());
	await createDeckThroughTheUi(page, 'Público de teste', { visibility: 'Público' });
	const publicUrl = page.url();
	await page.getByRole('link', { name: 'Novo flashcard' }).click();
	await page.getByLabel('Frente (pergunta, conceito ou palavra)').fill('Aberto?');
	await page.getByLabel('Verso (resposta ou tradução)').fill('Sim');
	await page.getByRole('button', { name: 'Salvar flashcard' }).click();
	await expect(page.getByRole('heading', { name: 'Flashcards (1)' })).toBeVisible();
	await createDeckThroughTheUi(page, 'Privado de teste');
	const privateUrl = page.url();

	const strangerContext = await browser.newContext({ baseURL: new URL(page.url()).origin });
	const stranger = await strangerContext.newPage();
	await registerThroughTheUi(stranger, uniqueEmail());

	await stranger.goto(publicUrl);
	await expect(stranger.getByText('Aberto?')).toBeVisible();
	await expect(stranger.getByRole('link', { name: 'Novo flashcard' })).toHaveCount(0);
	await stranger.goto(`${publicUrl}/flashcards/new`);
	await expect(stranger.getByText('Só o dono do baralho pode adicionar flashcards.')).toBeVisible();
	await stranger.goto(privateUrl);
	await expect(stranger.getByRole('heading', { name: 'Baralho não encontrado' })).toBeVisible();
	await strangerContext.close();
});
