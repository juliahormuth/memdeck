package br.com.memdeck.review.domain;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

public final class ReviewActivity {

	private final Set<LocalDate> days;
	private final LocalDate today;

	private ReviewActivity(Set<LocalDate> days, LocalDate today) {
		this.days = days;
		this.today = today;
	}

	public static ReviewActivity of(Collection<LocalDate> reviewedDays, LocalDate today) {
		return new ReviewActivity(new TreeSet<>(reviewedDays), today);
	}

	public int reviewedDays() {
		return days.size();
	}

	// A streak that ended yesterday is still alive: today's review has not been missed yet.
	public int currentStreak() {
		LocalDate day = days.contains(today) ? today : today.minusDays(1);
		int streak = 0;
		while (days.contains(day)) {
			streak++;
			day = day.minusDays(1);
		}
		return streak;
	}
}
