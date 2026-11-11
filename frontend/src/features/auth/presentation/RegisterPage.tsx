import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { Link } from 'react-router';
import { z } from 'zod';
import { Alert } from '../../../shared/ui/Alert';
import { Button } from '../../../shared/ui/Button';
import { TextField } from '../../../shared/ui/TextField';
import { validateEmail, validateName, validatePassword } from '../domain/rules';
import type { FieldErrorCode } from '../domain/rules';
import { emailMessages, messageForAuthError, nameMessages, passwordMessages } from './authMessages';
import { useRegister } from './authQueries';
import styles from './AuthForm.module.css';

function field(validate: (value: string) => FieldErrorCode | null, messages: Partial<Record<FieldErrorCode, string>>) {
	return z.string().superRefine((value, context) => {
		const code = validate(value);
		if (code) {
			context.addIssue({ code: 'custom', message: messages[code] ?? 'Valor inválido.' });
		}
	});
}

const schema = z.object({
	name: field(validateName, nameMessages),
	email: field(validateEmail, emailMessages),
	password: field(validatePassword, passwordMessages),
});

type RegisterFormValues = z.infer<typeof schema>;

export function RegisterPage() {
	const registerAccount = useRegister();
	const {
		register,
		handleSubmit,
		formState: { errors },
	} = useForm<RegisterFormValues>({ resolver: zodResolver(schema) });

	return (
		<section className={styles.card}>
			<h1 className={styles.title}>Criar conta</h1>
			<p className={styles.subtitle}>Comece a revisar no momento certo.</p>
			<form
				className={styles.form}
				noValidate
				onSubmit={handleSubmit((values) => registerAccount.mutate(values))}
			>
				{registerAccount.isError && <Alert>{messageForAuthError(registerAccount.error)}</Alert>}
				<TextField label="Nome" autoComplete="name" placeholder="Seu nome" error={errors.name?.message} {...register('name')} />
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
					autoComplete="new-password"
					placeholder="Pelo menos 8 caracteres"
					error={errors.password?.message}
					{...register('password')}
				/>
				<Button type="submit" fullWidth loading={registerAccount.isPending}>
					Criar conta
				</Button>
			</form>
			<p className={styles.footer}>
				Já tem conta? <Link to="/login">Entrar</Link>
			</p>
		</section>
	);
}
