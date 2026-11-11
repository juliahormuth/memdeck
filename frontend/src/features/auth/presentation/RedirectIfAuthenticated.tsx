import { Navigate, Outlet } from 'react-router';
import { FullPageSpinner } from '../../../shared/ui/FullPageSpinner';
import { useCurrentUser } from './authQueries';

export function RedirectIfAuthenticated() {
	const { data: user, isPending } = useCurrentUser();
	if (isPending) {
		return <FullPageSpinner />;
	}
	return user ? <Navigate to="/" replace /> : <Outlet />;
}
