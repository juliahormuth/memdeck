import { useQueryClient } from '@tanstack/react-query';
import { useEffect, useState } from 'react';
import { Outlet, useLocation, useNavigate } from 'react-router';
import { onUnauthorized } from '../../../shared/http/httpClient';
import { authUseCases } from '../composition';
import { clearSessionData, currentUserKey } from './authQueries';
import { loginPathFor } from './redirect';
import { SessionExpiredDialog } from './SessionExpiredDialog';

export function SessionExpiryBoundary() {
	const queryClient = useQueryClient();
	const navigate = useNavigate();
	const location = useLocation();
	const [expired, setExpired] = useState(false);

	const wasSignedIn = () => Boolean(queryClient.getQueryData(currentUserKey));

	useEffect(
		() =>
			onUnauthorized(() => {
				if (queryClient.getQueryData(currentUserKey)) {
					setExpired(true);
				}
			}),
		[queryClient],
	);

	useEffect(() => {
		function checkSessionOnReturn() {
			if (document.visibilityState === 'visible' && queryClient.getQueryData(currentUserKey)) {
				authUseCases.getCurrentUser().catch(() => undefined);
			}
		}
		document.addEventListener('visibilitychange', checkSessionOnReturn);
		return () => document.removeEventListener('visibilitychange', checkSessionOnReturn);
	}, [queryClient]);

	function signInAgain() {
		setExpired(false);
		if (wasSignedIn()) {
			clearSessionData(queryClient);
		}
		navigate(loginPathFor(location), { replace: true });
	}

	return (
		<>
			<Outlet />
			<SessionExpiredDialog open={expired} onSignInAgain={signInAgain} />
		</>
	);
}
