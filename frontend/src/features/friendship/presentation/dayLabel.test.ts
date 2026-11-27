import { describe, expect, it } from 'vitest';
import { dayLabel } from './dayLabel';

const now = new Date('2026-11-27T23:30:00Z');

describe('dayLabel', () => {
	it.each([
		['2026-11-27', 'Hoje'],
		['2026-11-26', 'Ontem'],
		['2026-11-25', '25 de novembro'],
		['2026-10-31', '31 de outubro'],
	])('shows %s as %s', (day, label) => {
		expect(dayLabel(day, now)).toBe(label);
	});
});
