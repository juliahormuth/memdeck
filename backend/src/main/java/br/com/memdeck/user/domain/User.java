package br.com.memdeck.user.domain;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

public class User {

	public static final int NAME_MAX_LENGTH = 100;
	public static final int EMAIL_MAX_LENGTH = 254;

	private static final Pattern USERNAME = Pattern.compile("^[a-z0-9._]{3,20}$");
	private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

	private final UUID id;
	private final String name;
	private final String username;
	private final String email;
	private final String passwordHash;
	private final Avatar avatar;
	private final Instant createdAt;

	private User(UUID id, String name, String username, String email, String passwordHash, Avatar avatar, Instant createdAt) {
		this.id = id;
		this.name = name;
		this.username = username;
		this.email = email;
		this.passwordHash = passwordHash;
		this.avatar = avatar;
		this.createdAt = createdAt;
	}

	public static User register(String name, String username, String email, String passwordHash) {
		String trimmedName = validName(name);
		String normalizedUsername = normalizeUsername(username);
		String normalizedEmail = normalizeEmail(email);
		if (passwordHash == null || passwordHash.isBlank()) {
			throw new InvalidUserException("Password hash is required");
		}
		return new User(UUID.randomUUID(), trimmedName, normalizedUsername, normalizedEmail, passwordHash, Avatar.CAT, Instant.now());
	}

	public static User restore(UUID id, String name, String username, String email, String passwordHash, Avatar avatar,
			Instant createdAt) {
		return new User(id, name, username, email, passwordHash, avatar, createdAt);
	}

	public User withProfile(String newName, Avatar newAvatar) {
		if (newAvatar == null) {
			throw new InvalidUserException("Avatar is required");
		}
		return new User(id, validName(newName), username, email, passwordHash, newAvatar, createdAt);
	}

	public User withPasswordHash(String newPasswordHash) {
		if (newPasswordHash == null || newPasswordHash.isBlank()) {
			throw new InvalidUserException("Password hash is required");
		}
		return new User(id, name, username, email, newPasswordHash, avatar, createdAt);
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

	public static String normalizeUsername(String username) {
		if (username == null || username.isBlank()) {
			throw new InvalidUserException("Username must not be blank");
		}
		String normalized = username.strip().toLowerCase(Locale.ROOT);
		if (normalized.startsWith("@")) {
			normalized = normalized.substring(1);
		}
		if (!USERNAME.matcher(normalized).matches()) {
			throw new InvalidUserException("Username must have 3 to 20 letters, numbers, dots or underscores");
		}
		return normalized;
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

	public String getUsername() {
		return username;
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
