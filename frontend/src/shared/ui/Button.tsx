import type { ButtonHTMLAttributes } from 'react';
import styles from './Button.module.css';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
	variant?: 'primary' | 'ghost';
	fullWidth?: boolean;
	loading?: boolean;
}

export function Button({ variant = 'primary', fullWidth = false, loading = false, className, disabled, ...rest }: ButtonProps) {
	const classes = [styles.button, styles[variant], fullWidth ? styles.fullWidth : '', className ?? ''].filter(Boolean).join(' ');
	return <button {...rest} className={classes} disabled={disabled || loading} aria-busy={loading || undefined} />;
}
