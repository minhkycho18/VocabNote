package com.vocabnote.app.repository;

import com.vocabnote.app.model.Vocabulary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VocabularyRepository extends JpaRepository<Vocabulary, Long> {
    Optional<Vocabulary> findByWordAndPos(String word, String pos);
    List<Vocabulary> findByWordInAndPosIs(List<String> words, String pos);

    @Query( value = "SELECT * FROM vocabularies v WHERE v.phonetics_uk IS NULL", nativeQuery = true)
    List<Vocabulary> findVocabulariesWithNullPhoneticsUk();

    List<Vocabulary> findByPos(String pos);
    List<Vocabulary> findByWordStartsWith(String character);
}
