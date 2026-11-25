import { useState } from 'react';
import type { ComponentProps } from 'react';
import { EyeIcon, EyeOffIcon } from './icons';
import { TextField } from './TextField';
import styles from './TextField.module.css';

type PasswordFieldProps = Omit<ComponentProps<typeof TextField>, 'type' | 'trailing'>;

export function PasswordField(props: PasswordFieldProps) {
	const [visible, setVisible] = useState(false);
	const action = visible ? 'Ocultar senha' : 'Exibir senha';

	return (
		<TextField
			{...props}
			type={visible ? 'text' : 'password'}
			autoCapitalize="none"
			spellCheck={false}
			trailing={
				<button
					type="button"
					className={styles.reveal}
					aria-label={action}
					title={action}
					onClick={() => setVisible((current) => !current)}
				>
					{visible ? <EyeOffIcon size={20} /> : <EyeIcon size={20} />}
				</button>
			}
		/>
	);
}
