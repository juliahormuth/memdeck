import { Link, NavLink, Outlet } from 'react-router';
import { useCurrentUser, useLogout } from '../../features/auth/presentation/authQueries';
import { useFriendRequests } from '../../features/friendship/presentation/friendshipQueries';
import { useMyInvitations } from '../../features/invitation/presentation/invitationQueries';
import { useProfile } from '../../features/profile/presentation/profileQueries';
import { Avatar } from '../../shared/ui/Avatar';
import { GlobeIcon, HomeIcon, LogoutIcon, MailIcon, PlusIcon, UsersIcon } from '../../shared/ui/icons';
import styles from './MainLayout.module.css';

const menu = [
	{ to: '/', label: 'Início', icon: <HomeIcon />, end: true },
	{ to: '/explore', label: 'Explorar', icon: <GlobeIcon />, end: false },
	{ to: '/invitations', label: 'Convites', icon: <MailIcon />, end: false },
	{ to: '/friends', label: 'Amigos', icon: <UsersIcon />, end: false },
];

export function MainLayout() {
	const { data: user } = useCurrentUser();
	const logout = useLogout();
	const { data: invitations } = useMyInvitations();
	const pendingInvitations = invitations?.length ?? 0;
	const { data: profile } = useProfile();
	const { data: friendRequests } = useFriendRequests();
	const pendingFriendRequests = friendRequests?.filter((request) => request.direction === 'INCOMING').length ?? 0;
	const badgeFor = (path: string) => (path === '/invitations' ? pendingInvitations : path === '/friends' ? pendingFriendRequests : 0);

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
									{badgeFor(item.to) > 0 && (
										<span className={styles.badge} aria-label={`${badgeFor(item.to)} pendentes`}>
											{badgeFor(item.to)}
										</span>
									)}
								</NavLink>
							</li>
						))}
					</ul>
					<Link to="/decks/new" className={styles.newDeck} aria-label="Novo baralho">
						<PlusIcon size={22} />
						<span className={styles.newDeckLabel}>Novo baralho</span>
					</Link>
				</nav>
				<div className={styles.user}>
					<Link to="/profile" className={styles.profile} aria-label="Meu perfil">
						<Avatar avatar={profile?.avatar ?? 'BOOK'} />
						<span className={styles.email} title={user?.email}>
							{user?.email}
						</span>
					</Link>
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
