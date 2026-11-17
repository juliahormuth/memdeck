import { expect, test } from '@playwright/test';
import { createDeckThroughTheUi, registerThroughTheUi, uniqueEmail } from './helpers';

function uniqueName(prefix: string): string {
	return `${prefix} ${Math.random().toString(36).slice(2, 8)}`;
}

test('a user finds a public deck of someone else, joins it and sees it on the home list as a reader', async ({ page, browser }) => {
	const name = uniqueName('Inglês');
	await registerThroughTheUi(page, uniqueEmail());
	await createDeckThroughTheUi(page, name, { description: 'Vocabulário do dia a dia', visibility: 'Público' });
	await createDeckThroughTheUi(page, uniqueName('Segredo'));
	await page.getByRole('navigation', { name: 'Menu principal' }).getByRole('link', { name: 'Explorar' }).click();
	await expect(page.getByRole('list', { name: 'Baralhos públicos' })).toHaveCount(0);

	const readerContext = await browser.newContext();
	const reader = await readerContext.newPage();
	await registerThroughTheUi(reader, uniqueEmail());
	await reader.getByRole('navigation', { name: 'Menu principal' }).getByRole('link', { name: 'Explorar' }).click();
	await reader.getByLabel('Pesquisar baralhos públicos').fill(name);
	await reader.getByRole('button', { name: 'Pesquisar' }).click();

	const results = reader.getByRole('list', { name: 'Baralhos públicos' });
	await expect(results.getByRole('link', { name })).toBeVisible();
	await expect(results.getByRole('listitem')).toHaveCount(1);
	await results.getByRole('button', { name: 'Participar' }).click();
	await expect(results.getByText('Você participa como Leitor')).toBeVisible();

	await reader.getByRole('navigation', { name: 'Menu principal' }).getByRole('link', { name: 'Início' }).click();
	const mine = reader.getByRole('list', { name: 'Seus baralhos' });
	await expect(mine.getByText(name)).toBeVisible();
	await expect(mine.getByText('Leitor')).toBeVisible();

	await mine.getByRole('link', { name }).click();
	await expect(reader.getByText('Você participa como Leitor')).toBeVisible();
	await expect(reader.getByRole('link', { name: 'Estudar' })).toBeVisible();
	await expect(reader.getByRole('link', { name: 'Editar', exact: true })).toHaveCount(0);
	await expect(reader.getByRole('button', { name: 'Participar' })).toHaveCount(0);
	await readerContext.close();
});

test('the search ignores private decks, matches the description and says when nothing is found', async ({ page, browser }) => {
	const publicName = uniqueName('Biologia');
	const privateName = uniqueName('Genética');
	await registerThroughTheUi(page, uniqueEmail());
	await createDeckThroughTheUi(page, publicName, { description: 'Células e tecidos', visibility: 'Público' });
	await createDeckThroughTheUi(page, privateName, { description: 'Células secretas' });

	const readerContext = await browser.newContext();
	const reader = await readerContext.newPage();
	await registerThroughTheUi(reader, uniqueEmail());
	await reader.goto('/explore');
	await reader.getByLabel('Pesquisar baralhos públicos').fill(publicName.split(' ')[1]!);
	await reader.getByRole('button', { name: 'Pesquisar' }).click();
	await expect(reader.getByRole('link', { name: publicName })).toBeVisible();

	await reader.getByLabel('Pesquisar baralhos públicos').fill(privateName);
	await reader.getByRole('button', { name: 'Pesquisar' }).click();
	await expect(reader.getByText(`Nenhum baralho público encontrado para "${privateName}".`)).toBeVisible();
	await readerContext.close();
});

test('someone who opens a public deck by its address can join it from the deck page', async ({ page, browser }) => {
	const name = uniqueName('Aberto');
	await registerThroughTheUi(page, uniqueEmail());
	await createDeckThroughTheUi(page, name, { visibility: 'Público' });
	const deckUrl = page.url();

	const readerContext = await browser.newContext();
	const reader = await readerContext.newPage();
	await registerThroughTheUi(reader, uniqueEmail());
	await reader.goto(deckUrl);
	await reader.getByRole('button', { name: 'Participar' }).click();

	await expect(reader.getByText('Você participa como Leitor')).toBeVisible();
	await expect(reader.getByRole('button', { name: 'Participar' })).toHaveCount(0);
	await readerContext.close();
});
