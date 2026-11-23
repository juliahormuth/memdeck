import { useState } from 'react';
import type { FormEvent } from 'react';
import { Alert } from '../../../shared/ui/Alert';
import { Button } from '../../../shared/ui/Button';
import { ConfirmDialog } from '../../../shared/ui/ConfirmDialog';
import { Dialog } from '../../../shared/ui/Dialog';
import { Select } from '../../../shared/ui/Select';
import { TextField } from '../../../shared/ui/TextField';
import { PlusIcon, UsersIcon } from '../../../shared/ui/icons';
import type { InvitableRole } from '../../invitation/domain/Invitation';
import { inviteSentMessage, messageForCancelInvitationError, messageForInviteError } from '../../invitation/presentation/invitationMessages';
import { useCancelInvitation, useDeckInvitations, useInviteToDeck } from '../../invitation/presentation/invitationQueries';
import type { DeckMember } from '../domain/Deck';
import { messageForRemoveMemberError, roleLabels } from './deckMessages';
import { useDeckMembers, useRemoveDeckMember } from './deckQueries';
import styles from './DeckTeamPanel.module.css';

export function DeckTeamPanel({ deckId }: { deckId: string }) {
	const [adding, setAdding] = useState(false);
	const [email, setEmail] = useState('');
	const [role, setRole] = useState<InvitableRole>('READER');
	const [sent, setSent] = useState(false);
	const [toRemove, setToRemove] = useState<DeckMember | null>(null);
	const members = useDeckMembers(deckId, true);
	const invitations = useDeckInvitations(deckId);
	const invite = useInviteToDeck();
	const cancelInvitation = useCancelInvitation(deckId);
	const removeMember = useRemoveDeckMember(deckId);

	const openAddDialog = () => {
		invite.reset();
		setSent(false);
		setAdding(true);
	};

	const closeAddDialog = () => {
		if (!invite.isPending) {
			invite.reset();
			setAdding(false);
		}
	};

	const submit = (event: FormEvent) => {
		event.preventDefault();
		invite.mutate(
			{ deckId, email, role },
			{
				onSuccess: () => {
					setEmail('');
					setRole('READER');
					setAdding(false);
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
			<div className={styles.header}>
				<h2 id="team-title" className={styles.title}>
					<UsersIcon size={22} />
					Participantes{members.data && members.data.length > 0 ? ` (${members.data.length})` : ''}
				</h2>
				<Button type="button" variant="secondary" onClick={openAddDialog}>
					<PlusIcon size={18} />
					Adicionar participante
				</Button>
			</div>
			{sent && (
				<p role="status" className={styles.note}>
					{inviteSentMessage}
				</p>
			)}

			{cancelInvitation.isError && <Alert>{messageForCancelInvitationError(cancelInvitation.error)}</Alert>}
			{invitations.isError && <Alert>Não foi possível carregar os convites.</Alert>}
			{invitations.data && invitations.data.length > 0 && (
				<>
					<h3 className={styles.subtitle}>Convites pendentes</h3>
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
				</>
			)}

			{members.isError && <Alert>Não foi possível carregar os participantes.</Alert>}
			{members.data && members.data.length === 0 && <p className={styles.empty}>Apenas você</p>}
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

			<Dialog
				open={adding}
				variant="form"
				icon={<UsersIcon size={28} />}
				title="Adicionar participante"
				description="Convide uma pessoa pelo e-mail e escolha o que ela pode fazer no baralho."
				onCancel={closeAddDialog}
			>
				<form className={styles.dialogForm} onSubmit={submit}>
					{invite.isError && <Alert>{messageForInviteError(invite.error)}</Alert>}
					<TextField
						label="E-mail da pessoa"
						type="email"
						required
						maxLength={254}
						autoComplete="off"
						placeholder="pessoa@exemplo.com"
						value={email}
						onChange={(event) => setEmail(event.target.value)}
					/>
					<Select label="Papel" value={role} onChange={(event) => setRole(event.target.value as InvitableRole)}>
						<option value="READER">Leitor</option>
						<option value="EDITOR">Editor</option>
					</Select>
					<Button type="submit" fullWidth loading={invite.isPending}>
						Convidar
					</Button>
					<Button type="button" variant="ghost" fullWidth disabled={invite.isPending} onClick={closeAddDialog}>
						Cancelar
					</Button>
				</form>
			</Dialog>

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
