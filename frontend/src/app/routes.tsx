import { Navigate } from 'react-router';
import type { RouteObject } from 'react-router';
import { LoginPage } from '../features/auth/presentation/LoginPage';
import { RedirectIfAuthenticated } from '../features/auth/presentation/RedirectIfAuthenticated';
import { RegisterPage } from '../features/auth/presentation/RegisterPage';
import { RequireAuth } from '../features/auth/presentation/RequireAuth';
import { HomePage } from '../features/deck/presentation/HomePage';
import { MainLayout } from './layouts/MainLayout';
import { PublicLayout } from './layouts/PublicLayout';

export const routes: RouteObject[] = [
	{
		element: <RedirectIfAuthenticated />,
		children: [
			{
				element: <PublicLayout />,
				children: [
					{ path: '/login', element: <LoginPage /> },
					{ path: '/register', element: <RegisterPage /> },
				],
			},
		],
	},
	{
		element: <RequireAuth />,
		children: [
			{
				element: <MainLayout />,
				children: [{ path: '/', element: <HomePage /> }],
			},
		],
	},
	{ path: '*', element: <Navigate to="/" replace /> },
];
