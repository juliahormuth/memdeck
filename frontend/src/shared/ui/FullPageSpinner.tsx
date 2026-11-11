import styles from './Spinner.module.css';

export function FullPageSpinner() {
	return (
		<div className={styles.center} role="status">
			<div className={styles.spinner} />
			<span className={styles.hidden}>Carregando...</span>
		</div>
	);
}
