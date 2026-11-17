import { expect, test } from '@playwright/test';
import { createDeckThroughTheUi, registerThroughTheUi, uniqueEmail } from './helpers';

const captureDir = process.env.CAPTURE_DIR;

test.describe('captures for the report (set CAPTURE_DIR to save them)', () => {
	test.skip(!captureDir, 'CAPTURE_DIR is not set');

	test('login page and master layout', async ({ page }, testInfo) => {
		const name = testInfo.project.name;

		await page.goto('/login');
		await expect(page.getByRole('heading', { name: 'Entrar' })).toBeVisible();
		await page.screenshot({ path: `${captureDir}/${name}-login.png`, fullPage: true });

		await registerThroughTheUi(page, uniqueEmail());
		await page.screenshot({ path: `${captureDir}/${name}-layout-mestre.png`, fullPage: true });
		await page
			.getByRole('navigation', { name: 'Menu principal' })
			.screenshot({ path: `${captureDir}/${name}-menu.png` });

		await page.getByRole('link', { name: 'Novo baralho', exact: true }).click();
		await expect(page.getByRole('heading', { name: 'Novo baralho' })).toBeVisible();
		await page.screenshot({ path: `${captureDir}/${name}-novo-baralho.png`, fullPage: true });

		await page.getByLabel('Nome').fill('Anatomia humana');
		await page.getByLabel('Descrição (opcional)').fill('Ossos e músculos do corpo humano.');
		await page.getByLabel(/Público/).check();
		await page.getByRole('button', { name: 'Criar baralho' }).click();
		await expect(page.getByRole('heading', { name: 'Anatomia humana' })).toBeVisible();
		await page.getByRole('link', { name: 'Novo flashcard' }).click();
		await page.getByLabel('Frente (pergunta, conceito ou palavra)').fill('Qual é o osso mais longo do braço?');
		await page.getByLabel('Verso (resposta ou tradução)').fill('Úmero');
		await page.screenshot({ path: `${captureDir}/${name}-novo-flashcard.png`, fullPage: true });
		await page.getByRole('button', { name: 'Salvar flashcard' }).click();
		await expect(page.getByRole('heading', { name: 'Flashcards (1)' })).toBeVisible();
		await page.screenshot({ path: `${captureDir}/${name}-baralho.png`, fullPage: true });
		await page.getByRole('button', { name: 'Excluir', exact: true }).click();
		await expect(page.getByRole('alertdialog', { name: 'Excluir este baralho?' })).toBeVisible();
		await page.screenshot({ path: `${captureDir}/${name}-excluir-baralho.png` });
		await page.getByRole('alertdialog').getByRole('button', { name: 'Cancelar' }).click();
		await expect(page.getByRole('alertdialog')).toBeHidden();

		await createDeckThroughTheUi(page, 'Inglês básico', { description: 'Vocabulário do dia a dia.' });
		await page.getByRole('navigation', { name: 'Menu principal' }).getByRole('link', { name: 'Início' }).click();
		await expect(page.getByRole('list', { name: 'Seus baralhos' })).toBeVisible();
		await page.screenshot({ path: `${captureDir}/${name}-inicio-com-baralhos.png`, fullPage: true });

		await page.context().clearCookies({ name: 'SESSION' });
		await page.evaluate(() => document.dispatchEvent(new Event('visibilitychange')));
		await expect(page.getByRole('alertdialog', { name: 'Sua sessão expirou' })).toBeVisible();
		await page.screenshot({ path: `${captureDir}/${name}-sessao-expirada.png` });
	});
});
