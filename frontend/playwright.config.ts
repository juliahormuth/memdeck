import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
	testDir: './e2e',
	workers: 1,
	retries: 0,
	reporter: [['list'], ['json', { outputFile: 'test-results/playwright.json' }]],
	use: {
		baseURL: process.env.E2E_BASE_URL ?? 'http://localhost:5174',
		trace: 'retain-on-failure',
		screenshot: 'only-on-failure',
	},
	projects: [
		{ name: 'desktop', use: { ...devices['Desktop Chrome'] } },
		{ name: 'mobile', use: { ...devices['Pixel 7'] } },
	],
	webServer: {
		command: 'npm run dev -- --port 5174 --strictPort',
		url: 'http://localhost:5174',
		reuseExistingServer: false,
		timeout: 60_000,
	},
});
