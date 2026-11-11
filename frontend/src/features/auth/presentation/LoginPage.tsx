import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { Link } from 'react-router';
import { z } from 'zod';
import { Alert } from '../../../shared/ui/Alert';
import { Button } from '../../../shared/ui/Button';
import { TextField } from '../../../shared/ui/TextField';
import { messageForAuthError } from './authMessages';
import { useLogin } from './authQueries';
import styles from './AuthForm.module.css';

const schema = z.object({
	email: z.string().trim().min(1, 'Informe o e-mail.'),
	password: z.string().min(1, 'Informe a senha.'),
});

type LoginFormValues = z.infer<typeof schema>;

export function LoginPage() {
	const login = useLogin();
	const {
		register,
		handleSubmit,
		formState: { errors },
	} = useForm<LoginFormValues>({ resolver: zodResolver(schema) });

	return (
		<section className={styles.card}>
			<h1 className={styles.title}>Entrar</h1>
			<p className={styles.subtitle}>Que bom ver você de novo.</p>
			<form className={styles.form} noValidate onSubmit={handleSubmit((values) => login.mutate(values))}>
				{login.isError && <Alert>{messageForAuthError(login.error)}</Alert>}
				<TextField
					label="E-mail"
					type="email"
					autoComplete="username"
					placeholder="voce@exemplo.com"
					error={errors.email?.message}
					{...register('email')}
				/>
				<TextField
					label="Senha"
					type="password"
					autoComplete="current-password"
					placeholder="Sua senha"
					error={errors.password?.message}
					{...register('password')}
				/>
				<Button type="submit" fullWidth loading={login.isPending}>
					Entrar
				</Button>
			</form>
			<p className={styles.footer}>
				Ainda não tem conta? <Link to="/register">Cadastre-se</Link>
			</p>
		</section>
	);
}
