import { useEffect, useId, useRef } from 'react';
import type { ReactNode } from 'react';
import styles from './Dialog.module.css';

interface DialogProps {
	open: boolean;
	title: string;
	description: string;
	icon?: ReactNode;
	tone?: 'default' | 'danger';
	variant?: 'alert' | 'form';
	onCancel: () => void;
	children: ReactNode;
}

export function Dialog({ open, title, description, icon, tone = 'default', variant = 'alert', onCancel, children }: DialogProps) {
	const ref = useRef<HTMLDialogElement>(null);
	const titleId = useId();
	const descriptionId = useId();

	useEffect(() => {
		const dialog = ref.current;
		if (!dialog) {
			return;
		}
		if (open && !dialog.open) {
			dialog.showModal();
		} else if (!open && dialog.open) {
			dialog.close();
		}
	}, [open]);

	return (
		<dialog
			ref={ref}
			className={[styles.dialog, variant === 'form' ? styles.form : ''].filter(Boolean).join(' ')}
			role={variant === 'form' ? 'dialog' : 'alertdialog'}
			aria-labelledby={titleId}
			aria-describedby={descriptionId}
			onCancel={(event) => {
				event.preventDefault();
				onCancel();
			}}
		>
			{icon && <div className={[styles.icon, tone === 'danger' ? styles.iconDanger : ''].filter(Boolean).join(' ')}>{icon}</div>}
			<h2 id={titleId} className={styles.title}>
				{title}
			</h2>
			<p id={descriptionId} className={styles.description}>
				{description}
			</p>
			<div className={styles.actions}>{children}</div>
		</dialog>
	);
}
