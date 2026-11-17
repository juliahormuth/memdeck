import { Navigate, Outlet, useLocation } from 'react-router';
import { FullPageSpinner } from '../../../shared/ui/FullPageSpinner';
import { useCurrentUser } from './authQueries';
import { loginPathFor } from './redirect';

export function RequireAuth() {
	const { data: user, isPending } = useCurrentUser();
	const location = useLocation();
	if (isPending) {
		return <FullPageSpinner />;
	}
	return user ? <Outlet /> : <Navigate to={loginPathFor(location)} replace />;
}
