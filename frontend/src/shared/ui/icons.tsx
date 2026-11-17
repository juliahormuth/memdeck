import type { ReactNode } from 'react';

interface IconProps {
	size?: number;
}

function Icon({ size = 20, children }: IconProps & { children: ReactNode }) {
	return (
		<svg
			width={size}
			height={size}
			viewBox="0 0 24 24"
			fill="none"
			stroke="currentColor"
			strokeWidth="2"
			strokeLinecap="round"
			strokeLinejoin="round"
			aria-hidden="true"
			focusable="false"
		>
			{children}
		</svg>
	);
}

export function HomeIcon(props: IconProps) {
	return (
		<Icon {...props}>
			<path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
			<polyline points="9 22 9 12 15 12 15 22" />
		</Icon>
	);
}

export function LogoutIcon(props: IconProps) {
	return (
		<Icon {...props}>
			<path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
			<polyline points="16 17 21 12 16 7" />
			<line x1="21" x2="9" y1="12" y2="12" />
		</Icon>
	);
}

export function BookIcon(props: IconProps) {
	return (
		<Icon {...props}>
			<path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z" />
			<path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z" />
		</Icon>
	);
}

export function PlusIcon(props: IconProps) {
	return (
		<Icon {...props}>
			<path d="M5 12h14" />
			<path d="M12 5v14" />
		</Icon>
	);
}

export function LayersIcon(props: IconProps) {
	return (
		<Icon {...props}>
			<path d="m12.83 2.18a2 2 0 0 0-1.66 0L2.6 6.08a1 1 0 0 0 0 1.83l8.58 3.91a2 2 0 0 0 1.66 0l8.58-3.9a1 1 0 0 0 0-1.83z" />
			<path d="m22 17.65-9.17 4.16a2 2 0 0 1-1.66 0L2 17.65" />
			<path d="m22 12.65-9.17 4.16a2 2 0 0 1-1.66 0L2 12.65" />
		</Icon>
	);
}

export function GlobeIcon(props: IconProps) {
	return (
		<Icon {...props}>
			<circle cx="12" cy="12" r="10" />
			<path d="M12 2a14.5 14.5 0 0 0 0 20 14.5 14.5 0 0 0 0-20" />
			<path d="M2 12h20" />
		</Icon>
	);
}

export function LockIcon(props: IconProps) {
	return (
		<Icon {...props}>
			<rect width="18" height="11" x="3" y="11" rx="2" ry="2" />
			<path d="M7 11V7a5 5 0 0 1 10 0v4" />
		</Icon>
	);
}

export function ClockIcon(props: IconProps) {
	return (
		<Icon {...props}>
			<circle cx="12" cy="12" r="10" />
			<polyline points="12 6 12 12 16 14" />
		</Icon>
	);
}

export function ArrowLeftIcon(props: IconProps) {
	return (
		<Icon {...props}>
			<path d="m12 19-7-7 7-7" />
			<path d="M19 12H5" />
		</Icon>
	);
}

export function StarIcon(props: IconProps) {
	return (
		<Icon {...props}>
			<path d="M12 2.5l2.9 6 6.6.9-4.8 4.6 1.2 6.5L12 17.4l-5.9 3.1 1.2-6.5L2.5 9.4l6.6-.9z" />
		</Icon>
	);
}

export function PencilIcon(props: IconProps) {
	return (
		<Icon {...props}>
			<path d="M12 20h9" />
			<path d="M16.5 3.5a2.1 2.1 0 0 1 3 3L7 19l-4 1 1-4z" />
		</Icon>
	);
}

export function TrashIcon(props: IconProps) {
	return (
		<Icon {...props}>
			<path d="M3 6h18" />
			<path d="M8 6V4h8v2" />
			<path d="M19 6l-1 14H6L5 6" />
			<path d="M10 11v6" />
			<path d="M14 11v6" />
		</Icon>
	);
}
