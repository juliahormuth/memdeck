import { expect, test } from '@playwright/test';
import type { Browser, Page } from '@playwright/test';
import { createDeckThroughTheUi, registerThroughTheUi, uniqueEmail, usernameFor } from './helpers';

const FRONT = 'Frente (pergunta, conceito ou palavra)';
const BACK = 'Verso (resposta ou tradução)';

async function newUser(browser: Browser): Promise<{ page: Page; email: string; close: () => Promise<void> }> {
	const context = await browser.newContext();
	const page = await context.newPage();
	const email = uniqueEmail();
	await registerThroughTheUi(page, email);
	return { page, email, close: () => context.close() };
}

async function invite(page: Page, email: string, role: 'Leitor' | 'Editor'): Promise<void> {
	await page.getByRole('button', { name: 'Adicionar participante' }).click();
	const dialog = page.getByRole('dialog', { name: 'Adicionar participante' });
	await dialog.getByLabel('Papel').selectOption({ label: role });
	await dialog.getByLabel('Nome de usuário').fill(usernameFor(email));
	await dialog.getByRole('button', { name: 'Pesquisar' }).click();
	await dialog.getByRole('button', { name: 'Convidar' }).click();
	await expect(page.getByText(/Convite enviado para/)).toBeVisible();
}

async function acceptFirstInvitation(page: Page): Promise<void> {
	await page.goto('/invitations');
	await page.getByRole('button', { name: 'Aceitar' }).click();
}

test('an invited editor accepts, adds flashcards to a private deck, and the owner later removes them', async ({ page, browser }) => {
	await registerThroughTheUi(page, uniqueEmail());
	await createDeckThroughTheUi(page, 'Equipe de anatomia');
	const deckUrl = page.url();
	const guest = await newUser(browser);

	await invite(page, guest.email, 'Editor');
	await expect(page.getByRole('list', { name: 'Convites pendentes' }).getByText('Pessoa de Teste')).toBeVisible();

	await guest.page.goto(deckUrl);
	await expect(guest.page.getByRole('heading', { name: 'Baralho não encontrado' })).toBeVisible();
	await guest.page.getByRole('navigation', { name: 'Menu principal' }).getByRole('link', { name: /Convites/ }).click();
	await expect(guest.page.getByText(/convidou você para o baralho/)).toBeVisible();
	await guest.page.getByRole('button', { name: 'Aceitar' }).click();

	await expect(guest.page.getByRole('heading', { name: 'Equipe de anatomia' })).toBeVisible();
	await expect(guest.page.getByText('Você participa como Editor')).toBeVisible();
	await expect(guest.page.getByRole('link', { name: /^Editar$/ })).toHaveCount(0);
	await guest.page.getByRole('link', { name: 'Novo flashcard' }).click();
	await guest.page.getByLabel(FRONT).fill('Qual osso é a clavícula?');
	await guest.page.getByLabel(BACK).fill('Osso do ombro');
	await guest.page.getByRole('button', { name: 'Salvar flashcard' }).click();
	await expect(guest.page.getByRole('listitem').filter({ hasText: 'Qual osso é a clavícula?' })).toBeVisible();

	await page.reload();
	await page.getByRole('button', { name: 'Exibir flashcards' }).click();
	await expect(page.getByRole('listitem').filter({ hasText: 'Qual osso é a clavícula?' })).toBeVisible();
	await page.getByRole('button', { name: 'Remover Pessoa de Teste' }).click();
	await page.getByRole('alertdialog').getByRole('button', { name: 'Remover' }).click();
	await expect(page.getByText('Apenas você')).toBeVisible();

	await guest.page.goto(deckUrl);
	await expect(guest.page.getByRole('heading', { name: 'Baralho não encontrado' })).toBeVisible();
	await guest.close();
});

test('an invited reader studies the private deck, cannot change it, and leaves it', async ({ page, browser }) => {
	await registerThroughTheUi(page, uniqueEmail());
	await createDeckThroughTheUi(page, 'Leitura privada');
	await addFlashcard(page);
	const deckUrl = page.url();
	const guest = await newUser(browser);
	await invite(page, guest.email, 'Leitor');

	await acceptFirstInvitation(guest.page);

	await expect(guest.page.getByText('Você participa como Leitor')).toBeVisible();
	await expect(guest.page.getByText('Pergunta')).toBeHidden();
	await guest.page.getByRole('button', { name: 'Exibir flashcards' }).click();
	await expect(guest.page.getByRole('listitem').filter({ hasText: 'Pergunta' })).toBeVisible();
	await expect(guest.page.getByRole('link', { name: 'Novo flashcard' })).toHaveCount(0);
	await guest.page.getByRole('link', { name: 'Estudar' }).click();
	await expect(guest.page.getByText('Pergunta')).toBeVisible();
	await guest.page.goto(deckUrl);
	await guest.page.getByRole('button', { name: 'Sair do baralho' }).click();
	await guest.page.getByRole('alertdialog').getByRole('button', { name: 'Sair do baralho' }).click();
	await expect(guest.page.getByRole('heading', { name: 'Início' })).toBeVisible();
	await expect(guest.page.getByText('Leitura privada')).toHaveCount(0);
	await guest.page.goto(deckUrl);
	await expect(guest.page.getByRole('heading', { name: 'Baralho não encontrado' })).toBeVisible();
	await guest.close();
});

test('declining keeps the guest out, and an unknown username is reported without creating an invitation', async ({ page, browser }) => {
	await registerThroughTheUi(page, uniqueEmail());
	await createDeckThroughTheUi(page, 'Recusas');
	const guest = await newUser(browser);
	await invite(page, guest.email, 'Leitor');
	await page.getByRole('button', { name: 'Adicionar participante' }).click();
	const dialog = page.getByRole('dialog', { name: 'Adicionar participante' });
	await dialog.getByLabel('Nome de usuário').fill('ninguem_aqui');
	await dialog.getByRole('button', { name: 'Pesquisar' }).click();
	await expect(dialog.getByText('Nenhum usuário encontrado.')).toBeVisible();
	await dialog.getByRole('button', { name: 'Cancelar' }).click();
	await expect(page.getByRole('list', { name: 'Convites pendentes' }).getByRole('listitem')).toHaveCount(1);

	await guest.page.goto('/invitations');
	await guest.page.getByRole('button', { name: 'Recusar' }).click();

	await expect(guest.page.getByText('Você não tem convites pendentes.')).toBeVisible();
	await page.reload();
	await expect(page.getByRole('list', { name: 'Convites pendentes' })).toHaveCount(0);
	await guest.close();
});

test('the owner cancels a pending invitation and it disappears for the guest', async ({ page, browser }) => {
	await registerThroughTheUi(page, uniqueEmail());
	await createDeckThroughTheUi(page, 'Cancelamento');
	const guest = await newUser(browser);
	await invite(page, guest.email, 'Editor');

	await page.getByRole('button', { name: /Cancelar convite de/ }).click();
	await expect(page.getByRole('list', { name: 'Convites pendentes' })).toHaveCount(0);

	await guest.page.goto('/invitations');
	await expect(guest.page.getByText('Você não tem convites pendentes.')).toBeVisible();
	await guest.close();
});

async function addFlashcard(page: Page): Promise<void> {
	await page.getByRole('link', { name: 'Novo flashcard' }).click();
	await page.getByLabel(FRONT).fill('Pergunta');
	await page.getByLabel(BACK).fill('Resposta');
	await page.getByRole('button', { name: 'Salvar flashcard' }).click();
	await expect(page.getByRole('listitem').filter({ hasText: 'Pergunta' })).toBeVisible();
}
