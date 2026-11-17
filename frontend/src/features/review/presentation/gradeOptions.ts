import type { ReviewGrade } from '../domain/ReviewGrade';

export interface GradeOption {
	grade: ReviewGrade;
	label: string;
	hint: string;
}

export const gradeOptions: GradeOption[] = [
	{ grade: 'AGAIN', label: 'Não lembro', hint: 'Não lembrei' },
	{ grade: 'HARD', label: 'Difícil', hint: 'Lembrei com esforço' },
	{ grade: 'GOOD', label: 'Bom', hint: 'Lembrei' },
	{ grade: 'EASY', label: 'Fácil', hint: 'Lembrei na hora' },
];
