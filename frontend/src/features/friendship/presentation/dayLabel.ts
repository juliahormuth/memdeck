const DAY_IN_MS = 24 * 60 * 60 * 1000;

// the server counts review days in UTC, so "today" has to be the UTC day as well
export function dayLabel(day: string, now = new Date()): string {
	const date = new Date(`${day}T00:00:00Z`);
	const today = Date.UTC(now.getUTCFullYear(), now.getUTCMonth(), now.getUTCDate());
	const age = Math.round((today - date.getTime()) / DAY_IN_MS);
	if (age <= 0) return 'Hoje';
	if (age === 1) return 'Ontem';
	return date.toLocaleDateString('pt-BR', { day: 'numeric', month: 'long', timeZone: 'UTC' });
}
