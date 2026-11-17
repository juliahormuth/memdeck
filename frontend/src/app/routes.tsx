import { Navigate } from 'react-router';
import type { RouteObject } from 'react-router';
import { LoginPage } from '../features/auth/presentation/LoginPage';
import { RedirectIfAuthenticated } from '../features/auth/presentation/RedirectIfAuthenticated';
import { RegisterPage } from '../features/auth/presentation/RegisterPage';
import { RequireAuth } from '../features/auth/presentation/RequireAuth';
import { SessionExpiryBoundary } from '../features/auth/presentation/SessionExpiryBoundary';
import { EditDeckPage } from '../features/deck/presentation/EditDeckPage';
import { DeckPage } from '../features/deck/presentation/DeckPage';
import { HomePage } from '../features/deck/presentation/HomePage';
import { NewDeckPage } from '../features/deck/presentation/NewDeckPage';
import { FlashcardEditorPage } from '../features/flashcard/presentation/FlashcardEditorPage';
import { MainLayout } from './layouts/MainLayout';
import { PublicLayout } from './layouts/PublicLayout';

export const routes: RouteObject[] = [
	{
		element: <SessionExpiryBoundary />,
		children: [
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
						children: [
							{ path: '/', element: <HomePage /> },
							{ path: '/decks/new', element: <NewDeckPage /> },
							{ path: '/decks/:id', element: <DeckPage /> },
							{ path: '/decks/:id/edit', element: <EditDeckPage /> },
							{ path: '/decks/:deckId/flashcards/new', element: <FlashcardEditorPage /> },
							{ path: '/decks/:deckId/flashcards/:flashcardId/edit', element: <FlashcardEditorPage /> },
						],
					},
				],
			},
			{ path: '*', element: <Navigate to="/" replace /> },
		],
	},
];
