package br.com.memdeck.friendship.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.memdeck.friendship.domain.FriendshipStatus;

interface FriendshipJpaRepository extends JpaRepository<FriendshipJpaModel, UUID> {

	@Query("""
			select f from FriendshipJpaModel f
			where (f.requesterId = :first and f.addresseeId = :second)
			   or (f.requesterId = :second and f.addresseeId = :first)
			""")
	Optional<FriendshipJpaModel> findBetween(@Param("first") UUID first, @Param("second") UUID second);

	@Query("""
			select f from FriendshipJpaModel f
			where (f.requesterId = :userId or f.addresseeId = :userId) and f.status = :status
			""")
	List<FriendshipJpaModel> findInvolving(@Param("userId") UUID userId, @Param("status") FriendshipStatus status);
}
