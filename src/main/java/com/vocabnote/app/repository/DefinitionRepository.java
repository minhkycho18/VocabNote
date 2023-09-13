package com.vocabnote.app.repository;

import com.vocabnote.app.model.Definition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DefinitionRepository extends JpaRepository<Definition, Long> {
    Optional<Definition> findByWordDesc(String definition);
}
