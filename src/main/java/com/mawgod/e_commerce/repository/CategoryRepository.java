package com.mawgod.e_commerce.repository;

import com.mawgod.e_commerce.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findBySlug(String slug);

    List<Category> findByParentIsNull();

    boolean existsBySlug(String slug);
}
