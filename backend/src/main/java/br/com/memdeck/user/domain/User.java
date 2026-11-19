package br.com.memdeck.user.domain;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

public class User {

	public static final int NAME_MAX_LENGTH = 100;
	public static final int EMAIL_MAX_LENGTH = 254;

	private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

	private final UUID id;
	private final String name;
	private final String email;
	private final String passwordHash;
	private final Avatar avatar;
	private final Instant createdAt;

	private User(UUID id, String name, String email, String passwordHash, Avatar avatar, Instant createdAt) {
		this.id = id;
		this.name = name;
		this.email = email;
		this.passwordHash = passwordHash;
		this.avatar = avatar;
		this.createdAt = createdAt;
	}

	public static User register(String name, String email, String passwordHash) {
		String trimmedName = validName(name);
		String normalizedEmail = normalizeEmail(email);
		if (passwordHash == null || passwordHash.isBlank()) {
			throw new InvalidUserException("Password hash is required");
		}
		return new User(UUID.randomUUID(), trimmedName, normalizedEmail, passwordHash, Avatar.BOOK, Instant.now());
	}

	public static User restore(UUID id, String name, String email, String passwordHash, Avatar avatar,
			Instant createdAt) {
		return new User(id, name, email, passwordHash, avatar, createdAt);
	}

	public User withProfile(String newName, Avatar newAvatar) {
		if (newAvatar == null) {
			throw new InvalidUserException("Avatar is required");
		}
		return new User(id, validName(newName), email, passwordHash, newAvatar, createdAt);
	}

	private static String validName(String name) {
		if (name == null || name.isBlank()) {
			throw new InvalidUserException("Name must not be blank");
		}
		String trimmedName = name.strip();
		if (trimmedName.length() > NAME_MAX_LENGTH) {
			throw new InvalidUserException("Name must have at most " + NAME_MAX_LENGTH + " characters");
		}
		return trimmedName;
	}

	public static String normalizeEmail(String email) {
		if (email == null || email.isBlank()) {
			throw new InvalidUserException("Email must not be blank");
		}
		String normalized = email.strip().toLowerCase(Locale.ROOT);
		if (normalized.length() > EMAIL_MAX_LENGTH || !EMAIL.matcher(normalized).matches()) {
			throw new InvalidUserException("Email is not valid");
		}
		return normalized;
	}

	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public Avatar getAvatar() {
		return avatar;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
