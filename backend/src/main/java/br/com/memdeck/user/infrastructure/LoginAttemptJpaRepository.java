package br.com.memdeck.user.infrastructure;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface LoginAttemptJpaRepository extends JpaRepository<LoginAttemptJpaModel, UUID> {

	int countByEmailAndOriginAndAttemptedAtAfter(String email, String origin, Instant since);

	Optional<LoginAttemptJpaModel> findFirstByEmailAndOriginAndAttemptedAtAfterOrderByAttemptedAtAsc(String email,
			String origin, Instant since);

	@Modifying
	@Query("delete from LoginAttemptJpaModel a where a.email = :email and a.origin = :origin")
	void deleteByKey(@Param("email") String email, @Param("origin") String origin);

	@Modifying
	@Query("delete from LoginAttemptJpaModel a where a.attemptedAt < :limit")
	int deleteOlderThan(@Param("limit") Instant limit);
}
