import { NavLink, Outlet } from 'react-router';
import { useCurrentUser, useLogout } from '../../features/auth/presentation/authQueries';
import { BookIcon, HomeIcon, LogoutIcon } from '../../shared/ui/icons';
import styles from './MainLayout.module.css';

const menu = [{ to: '/', label: 'Início', icon: <HomeIcon />, end: true }];

export function MainLayout() {
	const { data: user } = useCurrentUser();
	const logout = useLogout();

	return (
		<div className={styles.shell}>
			<div className={styles.sidebar}>
				<div className={styles.brand}>
					<img className={styles.logo} src="/logo.png" alt="" />
					<span>Memdeck</span>
				</div>
				<nav className={styles.nav} aria-label="Menu principal">
					<ul className={styles.navList}>
						{menu.map((item) => (
							<li key={item.to}>
								<NavLink to={item.to} end={item.end} className={styles.navLink}>
									{item.icon}
									{item.label}
								</NavLink>
							</li>
						))}
					</ul>
				</nav>
				<div className={styles.user}>
					<div className={styles.profile}>
						<span className={styles.avatar}>
							<BookIcon />
						</span>
						<span className={styles.email} title={user?.email}>
							{user?.email}
						</span>
					</div>
					<button type="button" className={styles.logout} aria-label="Sair" disabled={logout.isPending} onClick={() => logout.mutate()}>
						<LogoutIcon />
						<span className={styles.logoutLabel}>Sair</span>
					</button>
				</div>
			</div>
			<main className={styles.main}>
				<Outlet />
			</main>
		</div>
	);
}
