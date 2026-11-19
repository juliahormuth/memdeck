export const AVATARS = ['CAT', 'DOG', 'RABBIT', 'BIRD', 'FISH', 'TURTLE', 'SQUIRREL', 'PANDA'] as const;

export type AvatarKey = (typeof AVATARS)[number];

export interface Profile {
	id: string;
	name: string;
	email: string;
	avatar: AvatarKey;
	reviewedDays: number;
	streakDays: number;
	friends: number;
}
