import '@testing-library/jest-dom/vitest';
import { cleanup } from '@testing-library/react';
import { afterAll, afterEach, beforeAll } from 'vitest';
import { server } from './server';

// jsdom does not implement the modal methods of <dialog>
if (!HTMLDialogElement.prototype.showModal) {
	HTMLDialogElement.prototype.showModal = function showModal(this: HTMLDialogElement) {
		this.setAttribute('open', '');
	};
	HTMLDialogElement.prototype.close = function close(this: HTMLDialogElement) {
		this.removeAttribute('open');
	};
}

beforeAll(() => server.listen({ onUnhandledFrame: 'error' }));
afterEach(() => {
	cleanup();
	server.resetHandlers();
	document.cookie = 'XSRF-TOKEN=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/';
});
afterAll(() => server.close());
