package com.vocabnote.app.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "vocabularies", uniqueConstraints = @UniqueConstraint(columnNames = {"word","pos"}))
public class Vocabulary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long vocabId;

    @Column(length = 100)
    private String word;

    @Column(length = 100)
    private String pos;

    @Column
    private String phonetics_us;

    @Column
    private String phonetics_uk;

    @Column
    private String audio_us;

    @Column
    private String audio_uk;

    @ManyToMany
    @JoinTable(
            name = "vocab_def",
            joinColumns = @JoinColumn(name = "vocab_id", referencedColumnName = "vocabId"),
            inverseJoinColumns = @JoinColumn(name = "def_id", referencedColumnName = "defId"))
    List<Definition> definitions;

    @Override
    public String toString() {
        return "\nVocabulary{" +
                "vocabId=" + vocabId +
                ", word='" + word + '\'' +
                ", pos='" + pos + '\'' +
                ", phonetic_us='" + phonetics_us + '\'' +
                ", phonetic_uk='" + phonetics_uk + '\'' +
                ", audio_us='" + audio_us + '\'' +
                ", audio_uk='" + audio_uk + '\'' +
                '}';
    }
}
