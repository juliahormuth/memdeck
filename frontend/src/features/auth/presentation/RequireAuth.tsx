import { Navigate, Outlet } from 'react-router';
import { FullPageSpinner } from '../../../shared/ui/FullPageSpinner';
import { useCurrentUser } from './authQueries';

export function RequireAuth() {
	const { data: user, isPending } = useCurrentUser();
	if (isPending) {
		return <FullPageSpinner />;
	}
	return user ? <Outlet /> : <Navigate to="/login" replace />;
}
