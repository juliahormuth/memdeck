import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { validatedField } from '../../../shared/forms/validatedField';
import { Alert } from '../../../shared/ui/Alert';
import { Button } from '../../../shared/ui/Button';
import { ButtonLink } from '../../../shared/ui/ButtonLink';
import { TextArea } from '../../../shared/ui/TextArea';
import { TextField } from '../../../shared/ui/TextField';
import { validateDeckDescription, validateDeckName } from '../domain/rules';
import { descriptionMessages, nameMessages } from './deckMessages';
import styles from './DeckFormPage.module.css';

const schema = z.object({
	name: validatedField(validateDeckName, nameMessages),
	description: validatedField(validateDeckDescription, descriptionMessages),
	visibility: z.enum(['PUBLIC', 'PRIVATE']),
});

export type DeckFormValues = z.infer<typeof schema>;

interface DeckFormProps {
	initialValues: DeckFormValues;
	submitLabel: string;
	cancelTo: string;
	pending: boolean;
	error?: string;
	onSubmit: (values: DeckFormValues) => void;
}

export function DeckForm({ initialValues, submitLabel, cancelTo, pending, error, onSubmit }: DeckFormProps) {
	const {
		register,
		handleSubmit,
		formState: { errors },
	} = useForm<DeckFormValues>({ resolver: zodResolver(schema), defaultValues: initialValues });

	return (
		<form className={styles.form} noValidate onSubmit={handleSubmit(onSubmit)}>
			{error && <Alert>{error}</Alert>}
			<TextField label="Nome" placeholder="Ex.: Anatomia humana" error={errors.name?.message} {...register('name')} />
			<TextArea
				label="Descrição (opcional)"
				placeholder="Sobre o que é este baralho?"
				error={errors.description?.message}
				{...register('description')}
			/>
			<fieldset className={styles.visibility}>
				<legend className={styles.legend}>Visibilidade</legend>
				<label className={styles.option}>
					<input className={styles.radio} type="radio" value="PRIVATE" {...register('visibility')} />
					<span>
						<span className={styles.optionTitle}>Privado</span>
						<span className={styles.optionText}>Só você acessa este baralho.</span>
					</span>
				</label>
				<label className={styles.option}>
					<input className={styles.radio} type="radio" value="PUBLIC" {...register('visibility')} />
					<span>
						<span className={styles.optionTitle}>Público</span>
						<span className={styles.optionText}>Outros usuários da plataforma podem ver este baralho.</span>
					</span>
				</label>
			</fieldset>
			<div className={styles.actions}>
				<ButtonLink to={cancelTo} variant="ghost">
					Cancelar
				</ButtonLink>
				<Button type="submit" loading={pending}>
					{submitLabel}
				</Button>
			</div>
		</form>
	);
}
