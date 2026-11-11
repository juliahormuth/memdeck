import { expect, test } from '@playwright/test';
import { registerThroughTheUi, uniqueEmail } from './helpers';

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
	});
});
