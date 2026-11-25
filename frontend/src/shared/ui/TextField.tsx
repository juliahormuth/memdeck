import { useId } from 'react';
import type { InputHTMLAttributes, ReactNode, Ref } from 'react';
import styles from './TextField.module.css';

interface TextFieldProps extends InputHTMLAttributes<HTMLInputElement> {
	label: string;
	error?: string;
	trailing?: ReactNode;
	ref?: Ref<HTMLInputElement>;
}

export function TextField({ label, error, trailing, id, className, ...rest }: TextFieldProps) {
	const generatedId = useId();
	const inputId = id ?? generatedId;
	const errorId = `${inputId}-error`;
	return (
		<div className={styles.field}>
			<label htmlFor={inputId} className={styles.label}>
				{label}
			</label>
			<div className={styles.control}>
				<input
					{...rest}
					id={inputId}
					className={[styles.input, error ? styles.invalid : '', className ?? ''].filter(Boolean).join(' ')}
					aria-invalid={error ? true : undefined}
					aria-describedby={error ? errorId : undefined}
				/>
				{trailing}
			</div>
			{error && (
				<span id={errorId} className={styles.error}>
					{error}
				</span>
			)}
		</div>
	);
}
