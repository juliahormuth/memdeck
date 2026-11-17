import { Alert } from './Alert';
import { Button } from './Button';
import { Dialog } from './Dialog';
import { TrashIcon } from './icons';

interface ConfirmDialogProps {
	open: boolean;
	title: string;
	description: string;
	confirmLabel: string;
	pending?: boolean;
	error?: string;
	onConfirm: () => void;
	onCancel: () => void;
}

export function ConfirmDialog({ open, title, description, confirmLabel, pending = false, error, onConfirm, onCancel }: ConfirmDialogProps) {
	return (
		<Dialog open={open} tone="danger" icon={<TrashIcon size={28} />} title={title} description={description} onCancel={onCancel}>
			{error && <Alert>{error}</Alert>}
			<Button type="button" variant="ghost" autoFocus fullWidth disabled={pending} onClick={onCancel}>
				Cancelar
			</Button>
			<Button type="button" variant="danger" fullWidth loading={pending} onClick={onConfirm}>
				{confirmLabel}
			</Button>
		</Dialog>
	);
}
