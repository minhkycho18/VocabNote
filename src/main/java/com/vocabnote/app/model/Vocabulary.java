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
    private String phonetic;

    @Column(name = "audio_url")
    private String audioUrl;

    @ManyToMany
    @JoinTable(
            name = "vocab_def",
            joinColumns = @JoinColumn(name = "vocab_id"),
            inverseJoinColumns = @JoinColumn(name = "def_id"))
    List<Definition> definitions;
}
