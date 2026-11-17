const AUTH_PAGES = ['/login', '/register'];

export function safeRedirectPath(value: string | null): string | null {
	if (!value || !value.startsWith('/') || value.startsWith('//') || value.includes('\\')) {
		return null;
	}
	const pathname = value.split(/[?#]/)[0] ?? '';
	return AUTH_PAGES.includes(pathname) ? null : value;
}

export function loginPathFor(location: { pathname: string; search: string }): string {
	const here = `${location.pathname}${location.search}`;
	return here === '/' ? '/login' : `/login?redirect=${encodeURIComponent(here)}`;
}
