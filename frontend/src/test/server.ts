import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';

export const server = setupServer(
	http.get('*/api/auth/me', () => HttpResponse.json({ status: 401 }, { status: 401, headers: { 'Set-Cookie': 'XSRF-TOKEN=test-token; Path=/' } })),
);
