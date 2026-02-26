package com.sofkianos.consumer.repository;

import com.sofkianos.consumer.entity.Kudo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface KudoRepository extends JpaRepository<Kudo, Long> {
	@Query(value = "SELECT * FROM kudos k WHERE (:category IS NULL OR k.category = :category) AND (:searchText IS NULL OR LOWER(k.message) LIKE LOWER(CONCAT('%', :searchText, '%'))) ORDER BY k.created_at DESC", 
	       countQuery = "SELECT COUNT(*) FROM kudos k WHERE (:category IS NULL OR k.category = :category) AND (:searchText IS NULL OR LOWER(k.message) LIKE LOWER(CONCAT('%', :searchText, '%')))",
	       nativeQuery = true)
	Page<Kudo> findKudos(@Param("category") String category, @Param("searchText") String searchText, Pageable pageable);
}