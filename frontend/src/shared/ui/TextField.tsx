import { useId } from 'react';
import type { InputHTMLAttributes, Ref } from 'react';
import styles from './TextField.module.css';

interface TextFieldProps extends InputHTMLAttributes<HTMLInputElement> {
	label: string;
	error?: string;
	ref?: Ref<HTMLInputElement>;
}

export function TextField({ label, error, id, className, ...rest }: TextFieldProps) {
	const generatedId = useId();
	const inputId = id ?? generatedId;
	const errorId = `${inputId}-error`;
	return (
		<div className={styles.field}>
			<label htmlFor={inputId} className={styles.label}>
				{label}
			</label>
			<input
				{...rest}
				id={inputId}
				className={[styles.input, error ? styles.invalid : '', className ?? ''].filter(Boolean).join(' ')}
				aria-invalid={error ? true : undefined}
				aria-describedby={error ? errorId : undefined}
			/>
			{error && (
				<span id={errorId} className={styles.error}>
					{error}
				</span>
			)}
		</div>
	);
}
