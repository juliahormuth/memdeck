import { Link } from 'react-router';
import type { LinkProps } from 'react-router';
import styles from './Button.module.css';

interface ButtonLinkProps extends LinkProps {
	variant?: 'primary' | 'ghost';
}

export function ButtonLink({ variant = 'primary', className, ...rest }: ButtonLinkProps) {
	return <Link {...rest} className={[styles.button, styles[variant], styles.link, className ?? ''].filter(Boolean).join(' ')} />;
}
