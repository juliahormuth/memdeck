import js from '@eslint/js';
import { defineConfig } from 'eslint/config';
import boundaries from 'eslint-plugin-boundaries';
import reactHooks from 'eslint-plugin-react-hooks';
import globals from 'globals';
import tseslint from 'typescript-eslint';

const FRAMEWORK_IMPORTS = [
	{ group: ['react', 'react-dom', 'react-dom/*', 'react-router', 'react-hook-form', '@tanstack/*', 'zod', '@hookform/*'], message: 'This layer must stay framework-free.' },
];

export default defineConfig(
	{ ignores: ['dist', 'test-results', 'playwright-report', 'node_modules'] },
	js.configs.recommended,
	tseslint.configs.recommended,
	reactHooks.configs.flat['recommended-latest'],
	{
		files: ['**/*.{ts,tsx}'],
		languageOptions: { globals: { ...globals.browser, ...globals.node } },
		plugins: { boundaries },
		settings: {
			'import/resolver': { node: { extensions: ['.ts', '.tsx', '.js', '.jsx'] } },
			'boundaries/elements': [
				{ type: 'domain', pattern: 'src/features/*/domain', partialMatch: false, capture: ['feature'] },
				{ type: 'application', pattern: 'src/features/*/application', partialMatch: false, capture: ['feature'] },
				{ type: 'infrastructure', pattern: 'src/features/*/infrastructure', partialMatch: false, capture: ['feature'] },
				{ type: 'presentation', pattern: 'src/features/*/presentation', partialMatch: false, capture: ['feature'] },
				{ type: 'composition', pattern: 'src/features/*/composition', partialMatch: false, capture: ['feature'] },
				{ type: 'app', pattern: 'src/app', partialMatch: false },
				{ type: 'shared', pattern: 'src/shared', partialMatch: false },
			],
		},
		rules: {
			'boundaries/dependencies': [
				'error',
				{
					default: 'allow',
					policies: [
						{
							from: { element: { type: 'domain' } },
							disallow: { to: { element: { types: ['application', 'infrastructure', 'presentation', 'composition', 'app', 'shared'] } } },
							message: 'domain depends on nothing',
						},
						{
							from: { element: { type: 'application' } },
							disallow: { to: { element: { types: ['infrastructure', 'presentation', 'composition', 'app', 'shared'] } } },
							message: 'application depends only on domain',
						},
						{
							from: { element: { type: 'infrastructure' } },
							disallow: { to: { element: { types: ['presentation', 'composition', 'app'] } } },
							message: 'infrastructure never depends on presentation, composition or app',
						},
						{
							from: { element: { type: 'presentation' } },
							disallow: { to: { element: { types: ['infrastructure', 'app'] } } },
							message: 'presentation never depends on infrastructure or app (it uses the feature composition)',
						},
						{
							from: { element: { type: 'composition' } },
							disallow: { to: { element: { types: ['presentation', 'app'] } } },
							message: 'composition only wires application and infrastructure',
						},
						{
							from: { element: { type: 'shared' } },
							disallow: { to: { element: { types: ['domain', 'application', 'infrastructure', 'presentation', 'composition', 'app'] } } },
							message: 'shared knows no feature',
						},
						{
							from: { element: { type: 'app' } },
							disallow: { to: { element: { types: ['domain', 'application', 'infrastructure'] } } },
							message: 'app reaches a feature only through its presentation or composition',
						},
					],
				},
			],
		},
	},
	{
		files: ['src/features/*/{domain,application,infrastructure}/**/*.{ts,tsx}'],
		ignores: ['**/*.test.{ts,tsx}'],
		rules: { 'no-restricted-imports': ['error', { patterns: FRAMEWORK_IMPORTS }] },
	},
);
