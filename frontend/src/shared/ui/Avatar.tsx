import { Bird, Cat, Dog, Fish, Panda, Rabbit, Squirrel, Turtle } from 'lucide-react';
import type { LucideIcon } from 'lucide-react';
import styles from './Avatar.module.css';

const animals: Record<string, { icon: LucideIcon; color: string }> = {
	CAT: { icon: Cat, color: 'var(--avatar-cat)' },
	DOG: { icon: Dog, color: 'var(--avatar-dog)' },
	RABBIT: { icon: Rabbit, color: 'var(--avatar-rabbit)' },
	BIRD: { icon: Bird, color: 'var(--avatar-bird)' },
	FISH: { icon: Fish, color: 'var(--avatar-fish)' },
	TURTLE: { icon: Turtle, color: 'var(--avatar-turtle)' },
	SQUIRREL: { icon: Squirrel, color: 'var(--avatar-squirrel)' },
	PANDA: { icon: Panda, color: 'var(--avatar-panda)' },
};

interface AvatarProps {
	avatar: string;
	size?: number;
}

export function Avatar({ avatar, size = 40 }: AvatarProps) {
	const { icon: Icon, color } = animals[avatar] ?? animals.CAT!;
	return (
		<span className={styles.avatar} style={{ width: size, height: size, background: color }} aria-hidden="true">
			<Icon size={Math.round(size * 0.56)} strokeWidth={2} />
		</span>
	);
}
