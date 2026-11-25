package br.com.memdeck.user.infrastructure;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface PasswordResetJpaRepository extends JpaRepository<PasswordResetJpaModel, UUID> {

	// the user row is locked because there may be no reset row yet
	@Query(value = "select id from users where id = :userId for update", nativeQuery = true)
	UUID lockUser(@Param("userId") UUID userId);
}
