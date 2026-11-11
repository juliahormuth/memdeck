import { Outlet } from 'react-router';
import styles from './PublicLayout.module.css';

function ForgettingCurve() {
	return (
		<svg className={styles.curve} viewBox="0 0 320 130" aria-hidden="true" focusable="false">
			<g fill="none" stroke="#aecde6" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round">
				<path d="M8 18 C 38 18, 62 70, 100 96" />
				<path d="M100 96 L100 30" strokeDasharray="2 7" />
				<path d="M100 30 C 130 30, 158 66, 196 84" />
				<path d="M196 84 L196 38" strokeDasharray="2 7" />
				<path d="M196 38 C 226 38, 262 58, 312 68" />
			</g>
			<g fill="#6aafe8">
				<circle cx="100" cy="30" r="5" />
				<circle cx="196" cy="38" r="5" />
			</g>
		</svg>
	);
}

export function PublicLayout() {
	return (
		<div className={styles.page}>
			<aside className={styles.brand}>
				<img className={styles.logo} src="/logo.png" alt="" />
				<span className={styles.name}>memdeck</span>
				<p className={styles.tagline}>Revise antes de esquecer.</p>
				<p className={styles.description}>
					Flashcards com repetição espaçada: você revisa cada cartão no momento certo e guarda mais do que estuda.
				</p>
				<ForgettingCurve />
			</aside>
			<main className={styles.content}>
				<Outlet />
			</main>
		</div>
	);
}
