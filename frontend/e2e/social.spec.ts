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

async function addFlashcard(page: Page, front: string): Promise<void> {
	await page.getByRole('link', { name: 'Novo flashcard' }).click();
	await page.getByLabel(FRONT).fill(front);
	await page.getByLabel(BACK).fill('Resposta');
	await page.getByRole('button', { name: 'Salvar flashcard' }).click();
	await expect(page.getByRole('listitem').filter({ hasText: front })).toBeVisible();
}

async function studyAll(page: Page, deckUrl: string): Promise<void> {
	await page.goto(`${deckUrl}/study`);
	const reveal = page.getByRole('button', { name: 'Mostrar resposta' });
	const finished = page.getByRole('heading', { name: /Sessão concluída|Nada para estudar agora/ });
	for (;;) {
		await expect(reveal.or(finished)).toBeVisible();
		if (await finished.isVisible()) return;
		await reveal.click();
		await page.getByRole('button', { name: /Bom/ }).click();
	}
}

test('the user edits the name and the icon on the profile and sees the activity after studying', async ({ page }) => {
	await registerThroughTheUi(page, uniqueEmail());
	await page.getByRole('link', { name: 'Meu perfil' }).click();
	await expect(page.getByRole('heading', { name: 'Pessoa de Teste' })).toBeVisible();
	const stats = page.getByRole('list', { name: 'Sua atividade' });
	await expect(stats.getByText('dias revisados')).toBeVisible();

	await page.getByLabel('Nome', { exact: true }).fill('Julia Hormuth');
	await page.getByLabel('Cachorro').check({ force: true });
	await page.getByRole('button', { name: 'Salvar alterações' }).click();
	await expect(page.getByText('Perfil salvo.')).toBeVisible();
	await expect(page.getByRole('heading', { name: 'Julia Hormuth' })).toBeVisible();
	await page.reload();
	await expect(page.getByRole('heading', { name: 'Julia Hormuth' })).toBeVisible();
	await expect(page.getByLabel('Cachorro')).toBeChecked();

	await createDeckThroughTheUi(page, 'Atividade');
	const deckUrl = page.url();
	await addFlashcard(page, 'Pergunta um');
	await studyAll(page, deckUrl);
	await page.goto('/profile');
	await expect(stats.getByText('dia revisado')).toBeVisible();
	await expect(stats.getByText('dia seguido')).toBeVisible();
});

test('a friend request is sent by e-mail, accepted by the other person, and the friendship can be removed', async ({ page, browser }) => {
	await registerThroughTheUi(page, uniqueEmail());
	const friend = await newUser(browser);
	await friend.page.getByRole('link', { name: 'Meu perfil' }).click();
	await friend.page.getByLabel('Nome', { exact: true }).fill('Amiga Carla');
	await friend.page.getByRole('button', { name: 'Salvar alterações' }).click();
	await expect(friend.page.getByText('Perfil salvo.')).toBeVisible();

	await page.getByRole('navigation', { name: 'Menu principal' }).getByRole('link', { name: /Amigos/ }).click();
	await page.getByLabel('Adicionar amigo').fill(`ninguem_${Date.now().toString(36)}`);
	await page.getByRole('button', { name: 'Pesquisar' }).click();
	await expect(page.getByText('Nenhum usuário encontrado.')).toBeVisible();
	await page.getByLabel('Adicionar amigo').fill(usernameFor(friend.email));
	await page.getByRole('button', { name: 'Pesquisar' }).click();
	await expect(page.getByRole('group', { name: 'Usuário encontrado' })).toContainText('Amiga Carla');
	await page.getByRole('button', { name: 'Enviar pedido' }).click();
	await expect(page.getByText('Pedido de amizade enviado para Amiga Carla.')).toBeVisible();
	await expect(page.getByRole('list', { name: 'Pedidos enviados' }).getByRole('listitem')).toHaveCount(1);

	await friend.page.reload();
	await expect(friend.page.getByRole('link', { name: /Amigos/ }).getByLabel('1 pendentes')).toBeVisible();
	await friend.page.getByRole('link', { name: /Amigos/ }).click();
	await friend.page.getByRole('button', { name: /Aceitar pedido de/ }).click();
	await expect(friend.page.getByRole('list', { name: 'Seus amigos' }).getByRole('listitem')).toHaveCount(1);

	await page.reload();
	await expect(page.getByRole('list', { name: 'Seus amigos' }).getByText('Amiga Carla')).toBeVisible();
	await page.goto('/profile');
	await expect(page.getByRole('list', { name: 'Sua atividade' }).getByText('amigo', { exact: true })).toBeVisible();

	await createDeckThroughTheUi(friend.page, 'Segredo da Carla');
	const deckUrl = friend.page.url();
	await addFlashcard(friend.page, 'Pergunta secreta');
	await studyAll(friend.page, deckUrl);
	await page.goto('/');
	const activity = page.getByRole('list', { name: 'Atividade dos amigos' });
	await expect(activity.getByRole('listitem')).toContainText('Amiga Carla revisou 1 flashcard');
	await expect(activity.getByRole('listitem')).toContainText('Hoje');
	await expect(page.getByText('Segredo da Carla')).toHaveCount(0);
	await friend.page.goto('/friends');

	await friend.page.getByRole('button', { name: /Remover .* dos amigos/ }).click();
	await friend.page.getByRole('alertdialog').getByRole('button', { name: 'Remover' }).click();
	await expect(friend.page.getByText('Você ainda não tem amigos adicionados.')).toBeVisible();
	await page.goto('/friends');
	await expect(page.getByText('Você ainda não tem amigos adicionados.')).toBeVisible();
	await friend.close();
});

test('declining or cancelling a friend request leaves no friendship', async ({ page, browser }) => {
	await registerThroughTheUi(page, uniqueEmail());
	const other = await newUser(browser);
	await page.goto('/friends');
	await page.getByLabel('Adicionar amigo').fill(usernameFor(other.email));
	await page.getByRole('button', { name: 'Pesquisar' }).click();
	await page.getByRole('button', { name: 'Enviar pedido' }).click();
	await expect(page.getByRole('list', { name: 'Pedidos enviados' })).toBeVisible();

	await other.page.goto('/friends');
	await other.page.getByRole('button', { name: /Recusar pedido de/ }).click();
	await expect(other.page.getByText('Nenhum pedido recebido.')).toBeVisible();

	await page.reload();
	await expect(page.getByText('Nenhum pedido pendente.')).toBeVisible();
	await expect(page.getByText('Você ainda não tem amigos adicionados.')).toBeVisible();
	await other.close();
});

test('the ranking shows the participants of the deck by reviewed flashcards and is hidden from outsiders', async ({ page, browser }) => {
	await registerThroughTheUi(page, uniqueEmail());
	await createDeckThroughTheUi(page, 'Ranking de teste', { visibility: 'Público' });
	const deckUrl = page.url();
	await addFlashcard(page, 'Primeira');
	await addFlashcard(page, 'Segunda');
	const reader = await newUser(browser);
	const outsider = await newUser(browser);

	await outsider.page.goto(deckUrl);
	await expect(outsider.page.getByRole('heading', { name: 'Ranking de teste' })).toBeVisible();
	await expect(outsider.page.getByRole('heading', { name: 'Ranking', exact: true })).toHaveCount(0);

	await reader.page.goto(deckUrl);
	await reader.page.getByRole('button', { name: 'Participar' }).click();
	await expect(reader.page.getByText('Você participa como Leitor')).toBeVisible();
	await studyAll(reader.page, deckUrl);
	await reader.page.goto(deckUrl);

	const ranking = reader.page.getByRole('list', { name: 'Ranking do baralho' });
	await expect(ranking.getByRole('listitem')).toHaveCount(2);
	await expect(ranking.getByRole('listitem').first()).toContainText('1º');
	await expect(ranking.getByRole('listitem').first()).toContainText('(você)');
	await expect(ranking.getByRole('listitem').first()).toContainText('2 flashcards revisados');
	await expect(ranking.getByRole('listitem').last()).toContainText('0 flashcards revisados');

	await page.reload();
	await expect(page.getByRole('list', { name: 'Ranking do baralho' }).getByRole('listitem')).toHaveCount(2);
	await reader.close();
	await outsider.close();
});
