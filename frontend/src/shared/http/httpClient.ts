import { ApiError } from './ApiError';

const CSRF_COOKIE = 'XSRF-TOKEN';
const CSRF_HEADER = 'X-XSRF-TOKEN';

type Method = 'GET' | 'POST' | 'PUT' | 'DELETE';
type UnauthorizedHandler = (path: string) => void;

// a 401 on these routes is a credential problem, not an expired session
const CREDENTIAL_PATHS = new Set(['/api/auth/login', '/api/auth/register', '/api/auth/logout']);

let unauthorizedHandler: UnauthorizedHandler | null = null;

export function onUnauthorized(handler: UnauthorizedHandler): () => void {
	unauthorizedHandler = handler;
	return () => {
		if (unauthorizedHandler === handler) {
			unauthorizedHandler = null;
		}
	};
}

function readCookie(name: string): string | null {
	const entry = document.cookie.split('; ').find((cookie) => cookie.startsWith(`${name}=`));
	return entry ? decodeURIComponent(entry.slice(name.length + 1)) : null;
}

async function csrfToken(): Promise<string | null> {
	const existing = readCookie(CSRF_COOKIE);
	if (existing) {
		return existing;
	}
	// the API hands the CSRF cookie out on any response, even a 401
	await fetch(new URL('/api/auth/me', window.location.origin), { credentials: 'same-origin' });
	return readCookie(CSRF_COOKIE);
}

async function send(method: Method, path: string, body?: unknown): Promise<Response> {
	const headers: Record<string, string> = { Accept: 'application/json' };
	if (body !== undefined) {
		headers['Content-Type'] = 'application/json';
	}
	if (method !== 'GET') {
		const token = await csrfToken();
		if (token) {
			headers[CSRF_HEADER] = token;
		}
	}
	return fetch(new URL(path, window.location.origin), {
		method,
		headers,
		credentials: 'same-origin',
		body: body === undefined ? undefined : JSON.stringify(body),
	});
}

async function toApiError(response: Response): Promise<ApiError> {
	let message = response.statusText;
	let reason: string | null = null;
	try {
		const problem = (await response.json()) as { detail?: string; error?: string; title?: string; reason?: string };
		message = problem.detail ?? problem.error ?? problem.title ?? message;
		reason = problem.reason ?? null;
	} catch {
		// the body is not JSON; keep the status text
	}
	const retryAfter = Number(response.headers.get('Retry-After'));
	const wait = Number.isFinite(retryAfter) && retryAfter > 0 ? retryAfter : null;
	return new ApiError(response.status, message, wait, reason);
}

async function execute<T>(method: Method, path: string, body?: unknown): Promise<T> {
	const response = await send(method, path, body);
	if (!response.ok) {
		if (response.status === 401 && !CREDENTIAL_PATHS.has(path)) {
			unauthorizedHandler?.(path);
		}
		throw await toApiError(response);
	}
	const text = await response.text();
	return (text ? JSON.parse(text) : undefined) as T;
}

export const httpClient = {
	get: <T>(path: string) => execute<T>('GET', path),
	post: <T>(path: string, body?: unknown) => execute<T>('POST', path, body),
	put: <T>(path: string, body?: unknown) => execute<T>('PUT', path, body),
	delete: (path: string) => execute<void>('DELETE', path),
};
