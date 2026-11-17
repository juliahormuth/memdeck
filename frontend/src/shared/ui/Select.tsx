import { useId } from 'react';
import type { Ref, SelectHTMLAttributes } from 'react';
import styles from './TextField.module.css';

interface SelectProps extends SelectHTMLAttributes<HTMLSelectElement> {
	label: string;
	ref?: Ref<HTMLSelectElement>;
}

export function Select({ label, id, className, children, ...rest }: SelectProps) {
	const generatedId = useId();
	const selectId = id ?? generatedId;
	return (
		<div className={styles.field}>
			<label htmlFor={selectId} className={styles.label}>
				{label}
			</label>
			<select {...rest} id={selectId} className={[styles.input, className ?? ''].filter(Boolean).join(' ')}>
				{children}
			</select>
		</div>
	);
}
