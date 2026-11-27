package br.com.memdeck.user.infrastructure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface UserJpaRepository extends JpaRepository<UserJpaModel, UUID> {

	boolean existsByEmail(String email);

	boolean existsByUsername(String username);

	Optional<UserJpaModel> findByUsername(String username);

	Optional<UserJpaModel> findByEmail(String email);
}
