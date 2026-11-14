import { z } from 'zod';

export function validatedField(validate: (value: string) => string | null, messages: Record<string, string>) {
	return z.string().superRefine((value, context) => {
		const code = validate(value);
		if (code) {
			context.addIssue({ code: 'custom', message: messages[code] ?? 'Valor inválido.' });
		}
	});
}
