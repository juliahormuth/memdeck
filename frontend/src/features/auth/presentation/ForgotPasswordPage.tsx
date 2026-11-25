import { zodResolver } from '@hookform/resolvers/zod';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useNavigate } from 'react-router';
import { z } from 'zod';
import { validatedField } from '../../../shared/forms/validatedField';
import { Alert } from '../../../shared/ui/Alert';
import { Button } from '../../../shared/ui/Button';
import { PasswordField } from '../../../shared/ui/PasswordField';
import { TextField } from '../../../shared/ui/TextField';
import { validateEmail, validatePassword, validateResetCode } from '../domain/rules';
import { emailMessages, messageForAuthError, passwordMessages, resetCodeMessages } from './authMessages';
import { useConfirmPasswordReset, useRequestPasswordReset } from './authQueries';
import styles from './AuthForm.module.css';

const emailSchema = z.object({ email: validatedField(validateEmail, emailMessages) });

const resetSchema = z.object({
	code: validatedField(validateResetCode, resetCodeMessages),
	newPassword: validatedField(validatePassword, passwordMessages),
});

export function ForgotPasswordPage() {
	const [email, setEmail] = useState<string | null>(null);

	return (
		<section className={styles.card}>
			<h1 className={styles.title}>Recuperar conta</h1>
			{email === null ? (
				<EmailStep onSent={setEmail} />
			) : (
				<CodeStep email={email} onChangeEmail={() => setEmail(null)} />
			)}
			<p className={styles.footer}>
				<Link to="/login">Voltar para Entrar</Link>
			</p>
		</section>
	);
}

function EmailStep({ onSent }: { onSent: (email: string) => void }) {
	const request = useRequestPasswordReset();
	const {
		register,
		handleSubmit,
		formState: { errors },
	} = useForm<z.infer<typeof emailSchema>>({ resolver: zodResolver(emailSchema) });

	return (
		<>
			<p className={styles.subtitle}>Enviaremos um código para o seu e-mail.</p>
			<form
				className={styles.form}
				noValidate
				onSubmit={handleSubmit(({ email }) => request.mutate(email, { onSuccess: () => onSent(email.trim()) }))}
			>
				{request.isError && <Alert>{messageForAuthError(request.error)}</Alert>}
				<TextField
					label="E-mail"
					type="email"
					autoComplete="username"
					placeholder="voce@exemplo.com"
					error={errors.email?.message}
					{...register('email')}
				/>
				<Button type="submit" fullWidth loading={request.isPending}>
					Enviar código
				</Button>
			</form>
		</>
	);
}

function CodeStep({ email, onChangeEmail }: { email: string; onChangeEmail: () => void }) {
	const confirm = useConfirmPasswordReset();
	const resend = useRequestPasswordReset();
	const navigate = useNavigate();
	const {
		register,
		handleSubmit,
		formState: { errors },
	} = useForm<z.infer<typeof resetSchema>>({ resolver: zodResolver(resetSchema) });

	return (
		<>
			<p className={styles.subtitle}>
				Se houver uma conta para <strong>{email}</strong>, o código chega em instantes e vale por 15 minutos.
			</p>
			<form
				className={styles.form}
				noValidate
				onSubmit={handleSubmit((values) =>
					confirm.mutate(
						{ email, ...values },
						{ onSuccess: () => navigate('/login', { replace: true, state: { passwordChanged: true } }) },
					),
				)}
			>
				{confirm.isError && <Alert>{messageForAuthError(confirm.error)}</Alert>}
				{resend.isError && <Alert>{messageForAuthError(resend.error)}</Alert>}
				<TextField
					label="Código"
					inputMode="numeric"
					autoComplete="one-time-code"
					maxLength={6}
					placeholder="6 números"
					error={errors.code?.message}
					{...register('code')}
				/>
				<PasswordField
					label="Nova senha"
					autoComplete="new-password"
					placeholder="Pelo menos 8 caracteres"
					error={errors.newPassword?.message}
					{...register('newPassword')}
				/>
				<Button type="submit" fullWidth loading={confirm.isPending}>
					Alterar senha
				</Button>
			</form>
			{resend.isSuccess && (
				<p role="status" className={styles.footer}>
					Pedido enviado. Aguarde um minuto antes de pedir outro código.
				</p>
			)}
			<div className={styles.actions}>
				<button type="button" className={styles.link} disabled={resend.isPending} onClick={() => resend.mutate(email)}>
					Reenviar código
				</button>
				<button type="button" className={styles.link} onClick={onChangeEmail}>
					Usar outro e-mail
				</button>
			</div>
		</>
	);
}
