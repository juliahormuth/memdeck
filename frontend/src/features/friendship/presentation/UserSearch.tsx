import { useState } from 'react';
import type { FormEvent, ReactNode } from 'react';
import { Alert } from '../../../shared/ui/Alert';
import { Avatar } from '../../../shared/ui/Avatar';
import { Button } from '../../../shared/ui/Button';
import { TextField } from '../../../shared/ui/TextField';
import type { FoundUser } from '../domain/Friendship';
import { messageForSearchError } from './friendshipMessages';
import { useSearchUser } from './friendshipQueries';
import styles from './UserSearch.module.css';

interface UserSearchProps {
	label: string;
	children: (user: FoundUser) => ReactNode;
}

export function UserSearch({ label, children }: UserSearchProps) {
	const [username, setUsername] = useState('');
	const search = useSearchUser();

	const submit = (event: FormEvent) => {
		event.preventDefault();
		search.mutate(username);
	};

	return (
		<div className={styles.search}>
			<form className={styles.form} onSubmit={submit}>
				<div className={styles.field}>
					<TextField
						label={label}
						required
						maxLength={21}
						autoComplete="off"
						autoCapitalize="none"
						spellCheck={false}
						placeholder="Nome de usuário exato"
						value={username}
						onChange={(event) => {
							setUsername(event.target.value);
							search.reset();
						}}
					/>
				</div>
				<Button type="submit" variant="secondary" loading={search.isPending}>
					Pesquisar
				</Button>
			</form>
			{search.isError && <Alert>{messageForSearchError(search.error)}</Alert>}
			{search.data === null && (
				<p role="status" className={styles.note}>
					Nenhum usuário encontrado.
				</p>
			)}
			{search.data && (
				<div className={styles.result} role="group" aria-label="Usuário encontrado">
					<Avatar avatar={search.data.avatar} />
					<span className={styles.who}>
						<span className={styles.name}>{search.data.name}</span>
						<span className={styles.username}>@{search.data.username}</span>
					</span>
					{children(search.data)}
				</div>
			)}
		</div>
	);
}
