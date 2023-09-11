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
@Table(name = "definitions")
public class Definition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long defId;

    @Column(name = "word_desc", nullable = false)
    private String wordDesc;

    @Column
    private String examples;

    @ManyToMany(mappedBy = "definitions")
    List<Vocabulary> vocabularies;
}
