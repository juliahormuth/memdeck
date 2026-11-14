import { ButtonLink } from '../../../shared/ui/ButtonLink';
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
				<p className={styles.emptyText}>Crie um baralho para começar a estudar.</p>
				<ButtonLink to="/decks/new">Criar baralho</ButtonLink>
			</section>
		</div>
	);
}
