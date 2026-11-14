import { useId } from 'react';
import type { Ref, TextareaHTMLAttributes } from 'react';
import styles from './TextField.module.css';

interface TextAreaProps extends TextareaHTMLAttributes<HTMLTextAreaElement> {
	label: string;
	error?: string;
	ref?: Ref<HTMLTextAreaElement>;
}

export function TextArea({ label, error, id, className, ...rest }: TextAreaProps) {
	const generatedId = useId();
	const inputId = id ?? generatedId;
	const errorId = `${inputId}-error`;
	return (
		<div className={styles.field}>
			<label htmlFor={inputId} className={styles.label}>
				{label}
			</label>
			<textarea
				{...rest}
				id={inputId}
				className={[styles.input, styles.textarea, error ? styles.invalid : '', className ?? ''].filter(Boolean).join(' ')}
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
