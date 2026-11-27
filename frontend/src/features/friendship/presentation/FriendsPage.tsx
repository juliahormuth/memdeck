import { useState } from 'react';
import { Alert } from '../../../shared/ui/Alert';
import { Avatar } from '../../../shared/ui/Avatar';
import { Button } from '../../../shared/ui/Button';
import { ConfirmDialog } from '../../../shared/ui/ConfirmDialog';
import type { FoundUser, Friend } from '../domain/Friendship';
import {
	useAcceptFriendRequest,
	useCancelFriendRequest,
	useDeclineFriendRequest,
	useFriendRequests,
	useFriends,
	useRemoveFriend,
	useSendFriendRequest,
} from './friendshipQueries';
import {
	messageForAnswerError,
	messageForRemoveFriendError,
	messageForSendError,
	relationLabels,
	requestSentMessage,
} from './friendshipMessages';
import { UserSearch } from './UserSearch';
import styles from './FriendsPage.module.css';

export function FriendsPage() {
	const [sentTo, setSentTo] = useState<string | null>(null);
	const [searchKey, setSearchKey] = useState(0);
	const [toRemove, setToRemove] = useState<Friend | null>(null);
	const friends = useFriends();
	const requests = useFriendRequests();
	const send = useSendFriendRequest();
	const accept = useAcceptFriendRequest();
	const decline = useDeclineFriendRequest();
	const cancel = useCancelFriendRequest();
	const remove = useRemoveFriend();
	const answering = accept.isPending || decline.isPending || cancel.isPending;
	const answerError = accept.isError ? accept.error : decline.isError ? decline.error : cancel.isError ? cancel.error : null;
	const incoming = requests.data?.filter((request) => request.direction === 'INCOMING') ?? [];
	const outgoing = requests.data?.filter((request) => request.direction === 'OUTGOING') ?? [];

	const sendTo = (user: FoundUser) => {
		setSentTo(null);
		send.mutate(user.username, {
			onSuccess: () => {
				setSentTo(user.name);
				setSearchKey((key) => key + 1);
			},
		});
	};

	const closeRemoveDialog = () => {
		if (!remove.isPending) {
			remove.reset();
			setToRemove(null);
		}
	};

	return (
		<div className={styles.page}>
			<header>
				<h1 className={styles.title}>Amigos</h1>
				<p className={styles.subtitle}>Encontre pessoas pelo nome de usuário</p>
			</header>

			<UserSearch key={searchKey} label="Adicionar amigo">
				{(user) =>
					user.relation === 'NONE' ? (
						<Button type="button" loading={send.isPending} onClick={() => sendTo(user)}>
							Enviar pedido
						</Button>
					) : (
						<span className={styles.note}>{relationLabels[user.relation]}</span>
					)
				}
			</UserSearch>
			{send.isError && <Alert>{messageForSendError(send.error)}</Alert>}
			{sentTo && !send.isError && (
				<p role="status" className={styles.note}>
					{requestSentMessage(sentTo)}
				</p>
			)}
			{answerError && <Alert>{messageForAnswerError(answerError)}</Alert>}
			{(friends.isError || requests.isError) && <Alert>Não foi possível carregar seus amigos.</Alert>}

			<section aria-labelledby="incoming-title" className={styles.section}>
				<h2 id="incoming-title" className={styles.sectionTitle}>
					Pedidos recebidos
				</h2>
				{requests.data && incoming.length === 0 && <p className={styles.empty}>Nenhum pedido recebido.</p>}
				{incoming.length > 0 && (
					<ul className={styles.list} aria-label="Pedidos recebidos">
						{incoming.map((request) => (
							<li key={request.id} className={styles.row}>
								<Avatar avatar={request.avatar} />
								<span className={styles.name}>{request.name}</span>
								<Button type="button" disabled={answering} aria-label={`Aceitar pedido de ${request.name}`} onClick={() => accept.mutate(request.id)}>
									Aceitar
								</Button>
								<Button type="button" variant="ghost" disabled={answering} aria-label={`Recusar pedido de ${request.name}`} onClick={() => decline.mutate(request.id)}>
									Recusar
								</Button>
							</li>
						))}
					</ul>
				)}
			</section>

			<section aria-labelledby="outgoing-title" className={styles.section}>
				<h2 id="outgoing-title" className={styles.sectionTitle}>
					Pedidos enviados
				</h2>
				{requests.data && outgoing.length === 0 && <p className={styles.empty}>Nenhum pedido pendente.</p>}
				{outgoing.length > 0 && (
					<ul className={styles.list} aria-label="Pedidos enviados">
						{outgoing.map((request) => (
							<li key={request.id} className={styles.row}>
								<Avatar avatar={request.avatar} />
								<span className={styles.name}>{request.name}</span>
								<Button type="button" variant="ghost" disabled={answering} aria-label={`Cancelar pedido para ${request.name}`} onClick={() => cancel.mutate(request.id)}>
									Cancelar
								</Button>
							</li>
						))}
					</ul>
				)}
			</section>

			<section aria-labelledby="friends-title" className={styles.section}>
				<h2 id="friends-title" className={styles.sectionTitle}>
					Seus amigos{friends.data && friends.data.length > 0 ? ` (${friends.data.length})` : ''}
				</h2>
				{friends.data && friends.data.length === 0 && <p className={styles.empty}>Você ainda não tem amigos adicionados.</p>}
				{friends.data && friends.data.length > 0 && (
					<ul className={styles.list} aria-label="Seus amigos">
						{friends.data.map((friend) => (
							<li key={friend.userId} className={styles.row}>
								<Avatar avatar={friend.avatar} />
								<span className={styles.name}>{friend.name}</span>
								<Button type="button" variant="ghost" aria-label={`Remover ${friend.name} dos amigos`} onClick={() => setToRemove(friend)}>
									Remover
								</Button>
							</li>
						))}
					</ul>
				)}
			</section>

			<ConfirmDialog
				open={toRemove !== null}
				title="Remover amigo?"
				description={toRemove ? `${toRemove.name} deixa de ser seu amigo. Vocês podem se adicionar de novo depois.` : ''}
				confirmLabel="Remover"
				pending={remove.isPending}
				error={remove.isError ? messageForRemoveFriendError(remove.error) : undefined}
				onConfirm={() => toRemove && remove.mutate(toRemove.userId, { onSuccess: closeRemoveDialog })}
				onCancel={closeRemoveDialog}
			/>
		</div>
	);
}
