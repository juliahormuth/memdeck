import { describe, expect, it } from 'vitest';
import { z } from 'zod';
import './zodConfig';

describe('zodConfig', () => {
	it('turns zod JIT off so the page never needs unsafe-eval', () => {
		expect(z.config().jitless).toBe(true);
	});

	it('still validates with the JIT off', () => {
		const schema = z.object({ name: z.string().min(1) });

		expect(schema.safeParse({ name: 'Julia' }).success).toBe(true);
		expect(schema.safeParse({ name: '' }).success).toBe(false);
	});
});
