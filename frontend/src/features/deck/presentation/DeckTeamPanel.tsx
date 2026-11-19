import { useState } from 'react';
import type { FormEvent } from 'react';
import { Alert } from '../../../shared/ui/Alert';
import { Button } from '../../../shared/ui/Button';
import { ConfirmDialog } from '../../../shared/ui/ConfirmDialog';
import { Select } from '../../../shared/ui/Select';
import { TextField } from '../../../shared/ui/TextField';
import { UsersIcon } from '../../../shared/ui/icons';
import type { InvitableRole } from '../../invitation/domain/Invitation';
import { inviteSentMessage, messageForCancelInvitationError, messageForInviteError } from '../../invitation/presentation/invitationMessages';
import { useCancelInvitation, useDeckInvitations, useInviteToDeck } from '../../invitation/presentation/invitationQueries';
import type { DeckMember } from '../domain/Deck';
import { messageForRemoveMemberError, roleLabels } from './deckMessages';
import { useDeckMembers, useRemoveDeckMember } from './deckQueries';
import styles from './DeckTeamPanel.module.css';

export function DeckTeamPanel({ deckId }: { deckId: string }) {
	const [email, setEmail] = useState('');
	const [role, setRole] = useState<InvitableRole>('READER');
	const [sent, setSent] = useState(false);
	const [toRemove, setToRemove] = useState<DeckMember | null>(null);
	const members = useDeckMembers(deckId, true);
	const invitations = useDeckInvitations(deckId);
	const invite = useInviteToDeck();
	const cancelInvitation = useCancelInvitation(deckId);
	const removeMember = useRemoveDeckMember(deckId);

	const submit = (event: FormEvent) => {
		event.preventDefault();
		setSent(false);
		invite.mutate(
			{ deckId, email, role },
			{
				onSuccess: () => {
					setEmail('');
					setSent(true);
				},
			},
		);
	};

	const closeRemoveDialog = () => {
		if (!removeMember.isPending) {
			removeMember.reset();
			setToRemove(null);
		}
	};

	return (
		<section className={styles.section} aria-labelledby="team-title">
			<h2 id="team-title" className={styles.title}>
				<UsersIcon size={22} />
				Participantes
			</h2>
			<form className={styles.form} onSubmit={submit}>
				<div className={styles.email}>
					<TextField
						label="Convidar por e-mail"
						type="email"
						required
						maxLength={254}
						autoComplete="off"
						placeholder="pessoa@exemplo.com"
						value={email}
						onChange={(event) => setEmail(event.target.value)}
					/>
				</div>
				<div className={styles.role}>
					<Select label="Papel" value={role} onChange={(event) => setRole(event.target.value as InvitableRole)}>
						<option value="READER">Leitor</option>
						<option value="EDITOR">Editor</option>
					</Select>
				</div>
				<Button type="submit" loading={invite.isPending}>
					Convidar
				</Button>
			</form>
			{invite.isError && <Alert>{messageForInviteError(invite.error)}</Alert>}
			{sent && !invite.isError && <p role="status" className={styles.note}>{inviteSentMessage}</p>}

			<h3 className={styles.subtitle}>Convites pendentes</h3>
			{invitations.isError && <Alert>Não foi possível carregar os convites.</Alert>}
			{cancelInvitation.isError && <Alert>{messageForCancelInvitationError(cancelInvitation.error)}</Alert>}
			{invitations.data && invitations.data.length === 0 && <p className={styles.empty}>Nenhum convite pendente.</p>}
			{invitations.data && invitations.data.length > 0 && (
				<ul className={styles.list} aria-label="Convites pendentes">
					{invitations.data.map((invitation) => (
						<li key={invitation.id} className={styles.row}>
							<span className={styles.name}>{invitation.inviteeName}</span>
							<span className={styles.tag}>{roleLabels[invitation.role]}</span>
							<Button
								type="button"
								variant="ghost"
								aria-label={`Cancelar convite de ${invitation.inviteeName}`}
								disabled={cancelInvitation.isPending}
								onClick={() => cancelInvitation.mutate(invitation.id)}
							>
								Cancelar
							</Button>
						</li>
					))}
				</ul>
			)}

			<h3 className={styles.subtitle}>Quem participa</h3>
			{members.isError && <Alert>Não foi possível carregar os participantes.</Alert>}
			{members.data && members.data.length === 0 && <p className={styles.empty}>Só você, por enquanto.</p>}
			{members.data && members.data.length > 0 && (
				<ul className={styles.list} aria-label="Participantes">
					{members.data.map((member) => (
						<li key={member.userId} className={styles.row}>
							<span className={styles.name}>{member.name}</span>
							<span className={styles.tag}>{roleLabels[member.role]}</span>
							<Button
								type="button"
								variant="ghost"
								aria-label={`Remover ${member.name}`}
								onClick={() => setToRemove(member)}
							>
								Remover
							</Button>
						</li>
					))}
				</ul>
			)}
			<ConfirmDialog
				open={toRemove !== null}
				title="Remover participante?"
				description={toRemove ? `${toRemove.name} perde o acesso a este baralho. Você pode convidar a pessoa de novo depois.` : ''}
				confirmLabel="Remover"
				pending={removeMember.isPending}
				error={removeMember.isError ? messageForRemoveMemberError(removeMember.error) : undefined}
				onConfirm={() => toRemove && removeMember.mutate(toRemove.userId, { onSuccess: closeRemoveDialog })}
				onCancel={closeRemoveDialog}
			/>
		</section>
	);
}
