import { useNavigate } from 'react-router';
import { Alert } from '../../../shared/ui/Alert';
import { Button } from '../../../shared/ui/Button';
import { roleDescriptions, roleLabels } from '../../deck/presentation/deckMessages';
import { messageForAnswerInvitationError } from './invitationMessages';
import { useAcceptInvitation, useDeclineInvitation, useMyInvitations } from './invitationQueries';
import styles from './InvitationsPage.module.css';

export function InvitationsPage() {
	const navigate = useNavigate();
	const { data: invitations, isPending, isError, refetch } = useMyInvitations();
	const accept = useAcceptInvitation();
	const decline = useDeclineInvitation();
	const answering = accept.isPending || decline.isPending;
	const answerError = accept.isError ? accept.error : decline.isError ? decline.error : null;

	return (
		<div className={styles.page}>
			<header>
				<h1 className={styles.title}>Convites</h1>
				<p className={styles.subtitle}>Pessoas que convidaram você para participar de um baralho.</p>
			</header>
			{isPending && <p role="status">Carregando convites...</p>}
			{isError && (
				<div className={styles.error}>
					<Alert>Não foi possível carregar os convites.</Alert>
					<Button type="button" variant="ghost" onClick={() => void refetch()}>
						Tentar novamente
					</Button>
				</div>
			)}
			{answerError && <Alert>{messageForAnswerInvitationError(answerError)}</Alert>}
			{invitations && invitations.length === 0 && <p className={styles.empty}>Você não tem convites pendentes.</p>}
			{invitations && invitations.length > 0 && (
				<ul className={styles.list} aria-label="Convites pendentes">
					{invitations.map((invitation) => (
						<li key={invitation.id} className={styles.card}>
							<div className={styles.text}>
								<p>
									<strong>{invitation.inviterName}</strong> convidou você para o baralho <strong>{invitation.deckName}</strong> como{' '}
									<strong>{roleLabels[invitation.role]}</strong>.
								</p>
								<p className={styles.hint}>{roleDescriptions[invitation.role]}</p>
							</div>
							<div className={styles.actions}>
								<Button
									type="button"
									disabled={answering}
									onClick={() =>
										accept.mutate(invitation.id, { onSuccess: (accepted) => navigate(`/decks/${accepted.deckId}`) })
									}
								>
									Aceitar
								</Button>
								<Button type="button" variant="ghost" disabled={answering} onClick={() => decline.mutate(invitation.id)}>
									Recusar
								</Button>
							</div>
						</li>
					))}
				</ul>
			)}
		</div>
	);
}
