import { GlobeIcon, LockIcon } from '../../../shared/ui/icons';
import type { Visibility } from '../domain/Deck';
import { visibilityLabels } from './deckMessages';
import styles from './VisibilityBadge.module.css';

export function VisibilityBadge({ visibility }: { visibility: Visibility }) {
	return (
		<span className={[styles.badge, visibility === 'PRIVATE' ? styles.private : ''].filter(Boolean).join(' ')}>
			{visibility === 'PUBLIC' ? <GlobeIcon size={14} /> : <LockIcon size={14} />}
			{visibilityLabels[visibility]}
		</span>
	);
}
