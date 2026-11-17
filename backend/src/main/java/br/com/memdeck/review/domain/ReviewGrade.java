package br.com.memdeck.review.domain;

public enum ReviewGrade {

	AGAIN(1),
	HARD(3),
	GOOD(4),
	EASY(5);

	private final int quality;

	ReviewGrade(int quality) {
		this.quality = quality;
	}

	public int quality() {
		return quality;
	}

	boolean isRecalled() {
		return quality >= 3;
	}
}
