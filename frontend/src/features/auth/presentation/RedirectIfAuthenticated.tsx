import { Navigate, Outlet, useSearchParams } from 'react-router';
import { FullPageSpinner } from '../../../shared/ui/FullPageSpinner';
import { useCurrentUser } from './authQueries';
import { safeRedirectPath } from './redirect';

export function RedirectIfAuthenticated() {
	const { data: user, isPending } = useCurrentUser();
	const [searchParams] = useSearchParams();
	if (isPending) {
		return <FullPageSpinner />;
	}
	return user ? <Navigate to={safeRedirectPath(searchParams.get('redirect')) ?? '/'} replace /> : <Outlet />;
}
