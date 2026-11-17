package br.com.memdeck.deck.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.memdeck.deck.domain.Visibility;

interface DeckJpaRepository extends JpaRepository<DeckJpaModel, UUID> {

	List<DeckJpaModel> findByOwnerIdOrderByCreatedAtDesc(UUID ownerId);

	@Query("""
			select d from DeckJpaModel d
			where d.visibility = :visibility
			  and d.ownerId <> :excludedOwnerId
			  and (lower(d.name) like :pattern escape '\\'
			       or lower(coalesce(d.description, '')) like :pattern escape '\\')
			order by d.createdAt desc
			""")
	List<DeckJpaModel> searchByVisibility(@Param("visibility") Visibility visibility,
			@Param("excludedOwnerId") UUID excludedOwnerId, @Param("pattern") String pattern, Pageable pageable);
}
