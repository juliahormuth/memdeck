import { useState } from 'react';
import { Alert } from '../../../shared/ui/Alert';
import { Button } from '../../../shared/ui/Button';
import { ConfirmDialog } from '../../../shared/ui/ConfirmDialog';
import { Dialog } from '../../../shared/ui/Dialog';
import { Select } from '../../../shared/ui/Select';
import { PlusIcon, UsersIcon } from '../../../shared/ui/icons';
import type { FoundUser } from '../../friendship/domain/Friendship';
import { UserSearch } from '../../friendship/presentation/UserSearch';
import type { InvitableRole } from '../../invitation/domain/Invitation';
import { inviteSentMessage, messageForCancelInvitationError, messageForInviteError } from '../../invitation/presentation/invitationMessages';
import { useCancelInvitation, useDeckInvitations, useInviteToDeck } from '../../invitation/presentation/invitationQueries';
import type { DeckMember } from '../domain/Deck';
import { messageForRemoveMemberError, roleLabels } from './deckMessages';
import { useDeckMembers, useRemoveDeckMember } from './deckQueries';
import styles from './DeckTeamPanel.module.css';

export function DeckTeamPanel({ deckId }: { deckId: string }) {
	const [adding, setAdding] = useState(false);
	const [role, setRole] = useState<InvitableRole>('READER');
	const [sentTo, setSentTo] = useState<string | null>(null);
	const [toRemove, setToRemove] = useState<DeckMember | null>(null);
	const members = useDeckMembers(deckId, true);
	const invitations = useDeckInvitations(deckId);
	const invite = useInviteToDeck();
	const cancelInvitation = useCancelInvitation(deckId);
	const removeMember = useRemoveDeckMember(deckId);

	const openAddDialog = () => {
		invite.reset();
		setSentTo(null);
		setRole('READER');
		setAdding(true);
	};

	const closeAddDialog = () => {
		if (!invite.isPending) {
			invite.reset();
			setAdding(false);
		}
	};

	const inviteUser = (user: FoundUser) => {
		invite.mutate(
			{ deckId, username: user.username, role },
			{
				onSuccess: () => {
					setAdding(false);
					setSentTo(user.name);
				},
			},
		);
	};

	const invitationControls = (user: FoundUser) => {
		if (user.relation === 'SELF') {
			return <span className={styles.note}>Este é você</span>;
		}
		if (members.data?.some((member) => member.userId === user.userId && member.role === role)) {
			return <span className={styles.note}>Já participa como {roleLabels[role]}</span>;
		}
		return (
			<Button type="button" loading={invite.isPending} onClick={() => inviteUser(user)}>
				Convidar
			</Button>
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
			{sentTo && (
				<p role="status" className={styles.note}>
					{inviteSentMessage(sentTo)}
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
				description="Encontre a pessoa pelo nome de usuário e escolha o que ela pode fazer no baralho."
				onCancel={closeAddDialog}
			>
				{adding && (
					<div className={styles.dialogForm}>
						{invite.isError && <Alert>{messageForInviteError(invite.error)}</Alert>}
						<Select label="Papel" value={role} onChange={(event) => setRole(event.target.value as InvitableRole)}>
							<option value="READER">Leitor</option>
							<option value="EDITOR">Editor</option>
						</Select>
						<UserSearch label="Nome de usuário">{invitationControls}</UserSearch>
						<Button type="button" variant="ghost" fullWidth disabled={invite.isPending} onClick={closeAddDialog}>
							Cancelar
						</Button>
					</div>
				)}
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
