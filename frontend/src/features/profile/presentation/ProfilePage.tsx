import { zodResolver } from '@hookform/resolvers/zod';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link } from 'react-router';
import { z } from 'zod';
import { validatedField } from '../../../shared/forms/validatedField';
import { Alert } from '../../../shared/ui/Alert';
import { Avatar } from '../../../shared/ui/Avatar';
import { Button } from '../../../shared/ui/Button';
import { TextField } from '../../../shared/ui/TextField';
import { AVATARS } from '../domain/Profile';
import type { AvatarKey, Profile } from '../domain/Profile';
import { validateProfileName } from '../domain/rules';
import { avatarLabels, messageForProfileError, nameMessages } from './profileMessages';
import { useProfile, useUpdateProfile } from './profileQueries';
import styles from './ProfilePage.module.css';

const schema = z.object({ name: validatedField(validateProfileName, nameMessages) });

export function ProfilePage() {
	const { data: profile, isPending, isError, refetch } = useProfile();

	if (isPending) {
		return <p role="status">Carregando perfil...</p>;
	}
	if (isError || !profile) {
		return (
			<div className={styles.error}>
				<Alert>Não foi possível carregar o perfil.</Alert>
				<Button type="button" variant="ghost" onClick={() => void refetch()}>
					Tentar novamente
				</Button>
			</div>
		);
	}
	return <ProfileView profile={profile} />;
}

function ProfileView({ profile }: { profile: Profile }) {
	const updateProfile = useUpdateProfile();
	const [avatar, setAvatar] = useState<AvatarKey>(profile.avatar);
	const [saved, setSaved] = useState(false);
	const {
		register,
		handleSubmit,
		formState: { errors },
	} = useForm<{ name: string }>({ resolver: zodResolver(schema), defaultValues: { name: profile.name } });

	const submit = ({ name }: { name: string }) => {
		setSaved(false);
		updateProfile.mutate({ name, avatar }, { onSuccess: () => setSaved(true) });
	};

	return (
		<div className={styles.page}>
			<header className={styles.header}>
				<Avatar avatar={profile.avatar} size={72} />
				<div className={styles.who}>
					<h1 className={styles.title}>{profile.name}</h1>
					<p className={styles.email}>@{profile.username}</p>
					<p className={styles.email}>{profile.email}</p>
				</div>
			</header>

			<ul className={styles.stats} aria-label="Sua atividade">
				<li className={styles.stat}>
					<span className={styles.value}>{profile.reviewedDays}</span>
					<span className={styles.label}>{profile.reviewedDays === 1 ? 'dia revisado' : 'dias revisados'}</span>
				</li>
				<li className={styles.stat}>
					<span className={styles.value}>{profile.streakDays}</span>
					<span className={styles.label}>{profile.streakDays === 1 ? 'dia seguido' : 'dias seguidos'}</span>
				</li>
				<li className={styles.stat}>
					<Link to="/friends" className={styles.statLink}>
						<span className={styles.value}>{profile.friends}</span>
						<span className={styles.label}>{profile.friends === 1 ? 'amigo' : 'amigos'}</span>
					</Link>
				</li>
			</ul>

			<form className={styles.form} noValidate onSubmit={handleSubmit(submit)}>
				<h2 className={styles.subtitle}>Editar perfil</h2>
				{updateProfile.isError && <Alert>{messageForProfileError(updateProfile.error)}</Alert>}
				{saved && !updateProfile.isError && (
					<p role="status" className={styles.saved}>
						Perfil salvo.
					</p>
				)}
				<TextField label="Nome" autoComplete="name" error={errors.name?.message} {...register('name')} />
				<fieldset className={styles.avatars}>
					<legend className={styles.legend}>Ícone do perfil</legend>
					<div className={styles.grid}>
						{AVATARS.map((option) => (
							<label key={option} className={styles.option}>
								<input
									type="radio"
									name="avatar"
									value={option}
									checked={avatar === option}
									onChange={() => setAvatar(option)}
									className={styles.radio}
								/>
								<span className={styles.choice}>
									<Avatar avatar={option} size={48} />
									<span className={styles.optionLabel}>{avatarLabels[option]}</span>
								</span>
							</label>
						))}
					</div>
				</fieldset>
				<Button type="submit" loading={updateProfile.isPending}>
					Salvar alterações
				</Button>
			</form>
		</div>
	);
}
