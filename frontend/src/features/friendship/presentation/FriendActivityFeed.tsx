import { Alert } from '../../../shared/ui/Alert';
import { Avatar } from '../../../shared/ui/Avatar';
import { dayLabel } from './dayLabel';
import { useFriendActivity } from './friendshipQueries';
import styles from './FriendActivityFeed.module.css';

export function FriendActivityFeed() {
	const { data: activity, isError } = useFriendActivity();

	return (
		<section className={styles.section} aria-labelledby="friend-activity-title">
			<h2 id="friend-activity-title" className={styles.title}>
				Atividade dos amigos
			</h2>
			{isError && <Alert>Não foi possível carregar a atividade dos amigos.</Alert>}
			{activity && activity.length === 0 && <p className={styles.empty}>Nenhuma atividade recente.</p>}
			{activity && activity.length > 0 && (
				<ul className={styles.list} aria-label="Atividade dos amigos">
					{activity.map((entry) => (
						<li key={`${entry.userId}-${entry.day}`} className={styles.row}>
							<Avatar avatar={entry.avatar} size={36} />
							<span className={styles.text}>
								<strong>{entry.name}</strong> revisou {entry.reviewedFlashcards}{' '}
								{entry.reviewedFlashcards === 1 ? 'flashcard' : 'flashcards'}
							</span>
							<time className={styles.day} dateTime={entry.day}>
								{dayLabel(entry.day)}
							</time>
						</li>
					))}
				</ul>
			)}
		</section>
	);
}
