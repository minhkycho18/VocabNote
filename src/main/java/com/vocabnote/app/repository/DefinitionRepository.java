package com.vocabnote.app.repository;

import com.vocabnote.app.model.Definition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DefinitionRepository extends JpaRepository<Definition, Long> {
}
