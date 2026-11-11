import styles from './HomePage.module.css';

export function HomePage() {
	return (
		<div className={styles.page}>
			<header>
				<h1 className={styles.title}>Início</h1>
				<p className={styles.subtitle}>Seus baralhos de estudo.</p>
			</header>
			<section className={styles.empty} aria-labelledby="decks-empty-title">
				<img className={styles.emptyLogo} src="/logo.png" alt="" />
				<h2 id="decks-empty-title" className={styles.emptyTitle}>
					Seus baralhos aparecerão aqui
				</h2>
				<p className={styles.emptyText}>Em breve você poderá criar baralhos e revisar seus flashcards.</p>
			</section>
		</div>
	);
}
