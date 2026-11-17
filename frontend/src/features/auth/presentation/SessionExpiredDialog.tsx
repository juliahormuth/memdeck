import { Button } from '../../../shared/ui/Button';
import { Dialog } from '../../../shared/ui/Dialog';
import { ClockIcon } from '../../../shared/ui/icons';

interface SessionExpiredDialogProps {
	open: boolean;
	onSignInAgain: () => void;
}

export function SessionExpiredDialog({ open, onSignInAgain }: SessionExpiredDialogProps) {
	return (
		<Dialog
			open={open}
			icon={<ClockIcon size={28} />}
			title="Sua sessão expirou"
			description="Por segurança, você foi desconectado depois de um tempo sem atividade. Entre novamente para continuar de onde parou."
			onCancel={onSignInAgain}
		>
			<Button type="button" fullWidth autoFocus onClick={onSignInAgain}>
				Entrar novamente
			</Button>
		</Dialog>
	);
}
