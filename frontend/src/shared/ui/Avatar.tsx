import type { ReactElement } from 'react';
import { BookIcon, ClockIcon, GlobeIcon, LayersIcon, PencilIcon, PlayIcon, StarIcon, UsersIcon } from './icons';
import styles from './Avatar.module.css';

type IconComponent = (props: { size?: number }) => ReactElement;

const icons: Record<string, IconComponent> = {
	BOOK: BookIcon,
	LAYERS: LayersIcon,
	STAR: StarIcon,
	GLOBE: GlobeIcon,
	CLOCK: ClockIcon,
	PENCIL: PencilIcon,
	PLAY: PlayIcon,
	USERS: UsersIcon,
};

interface AvatarProps {
	avatar: string;
	size?: number;
}

export function Avatar({ avatar, size = 40 }: AvatarProps) {
	const Icon = icons[avatar] ?? BookIcon;
	return (
		<span className={styles.avatar} style={{ width: size, height: size }} aria-hidden="true">
			<Icon size={Math.round(size * 0.5)} />
		</span>
	);
}
