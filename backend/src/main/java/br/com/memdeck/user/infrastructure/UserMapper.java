package br.com.memdeck.user.infrastructure;

import br.com.memdeck.user.domain.User;

final class UserMapper {

	private UserMapper() {
	}

	static UserJpaModel toModel(User user) {
		return new UserJpaModel(
				user.getId(),
				user.getName(),
				user.getEmail(),
				user.getPasswordHash(),
				user.getCreatedAt());
	}

	static User toDomain(UserJpaModel model) {
		return User.restore(
				model.getId(),
				model.getName(),
				model.getEmail(),
				model.getPasswordHash(),
				model.getCreatedAt());
	}
}
