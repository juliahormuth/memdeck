import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { useNavigate } from 'react-router';
import { z } from 'zod';
import { validatedField } from '../../../shared/forms/validatedField';
import { Alert } from '../../../shared/ui/Alert';
import { Button } from '../../../shared/ui/Button';
import { ButtonLink } from '../../../shared/ui/ButtonLink';
import { TextArea } from '../../../shared/ui/TextArea';
import { TextField } from '../../../shared/ui/TextField';
import { validateDeckDescription, validateDeckName } from '../domain/rules';
import { descriptionMessages, messageForDeckError, nameMessages } from './deckMessages';
import { useCreateDeck } from './deckQueries';
import styles from './NewDeckPage.module.css';

const schema = z.object({
	name: validatedField(validateDeckName, nameMessages),
	description: validatedField(validateDeckDescription, descriptionMessages),
	visibility: z.enum(['PUBLIC', 'PRIVATE']),
});

type NewDeckFormValues = z.infer<typeof schema>;

export function NewDeckPage() {
	const navigate = useNavigate();
	const createDeck = useCreateDeck();
	const {
		register,
		handleSubmit,
		formState: { errors },
	} = useForm<NewDeckFormValues>({
		resolver: zodResolver(schema),
		defaultValues: { name: '', description: '', visibility: 'PRIVATE' },
	});

	return (
		<section className={styles.page}>
			<h1 className={styles.title}>Novo baralho</h1>
			<p className={styles.subtitle}>Escolha o nome e quem pode acessar o baralho.</p>
			<form
				className={styles.form}
				noValidate
				onSubmit={handleSubmit((values) =>
					createDeck.mutate(values, { onSuccess: (deck) => navigate(`/decks/${deck.id}`) }),
				)}
			>
				{createDeck.isError && <Alert>{messageForDeckError(createDeck.error)}</Alert>}
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
					<ButtonLink to="/" variant="ghost">
						Cancelar
					</ButtonLink>
					<Button type="submit" loading={createDeck.isPending}>
						Criar baralho
					</Button>
				</div>
			</form>
		</section>
	);
}
