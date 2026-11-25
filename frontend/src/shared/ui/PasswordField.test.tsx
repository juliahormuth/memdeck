import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';
import { PasswordField } from './PasswordField';

describe('PasswordField', () => {
	it('hides the password until the user asks to see it, and hides it again', async () => {
		render(<PasswordField label="Senha" />);
		const input = screen.getByLabelText('Senha');
		await userEvent.type(input, 'correct-horse');
		expect(input).toHaveAttribute('type', 'password');

		await userEvent.click(screen.getByRole('button', { name: 'Exibir senha' }));
		expect(input).toHaveAttribute('type', 'text');
		expect(input).toHaveValue('correct-horse');

		await userEvent.click(screen.getByRole('button', { name: 'Ocultar senha' }));
		expect(input).toHaveAttribute('type', 'password');
	});

	it('does not submit the form when the password is revealed', async () => {
		let submissions = 0;
		render(
			<form onSubmit={() => (submissions += 1)}>
				<PasswordField label="Senha" />
			</form>,
		);

		await userEvent.click(screen.getByRole('button', { name: 'Exibir senha' }));

		expect(submissions).toBe(0);
	});
});
