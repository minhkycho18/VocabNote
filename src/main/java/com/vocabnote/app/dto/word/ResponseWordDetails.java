package com.vocabnote.app.dto.word;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseWordDetails {
    @JsonProperty("word")
    private String word;
    @JsonProperty("phonetic")
    private String phonetic;
    @JsonProperty("phonetics")
    private List<Phonetics> phonetics;
    @JsonProperty("meanings")
    private List<Meanings> meanings;
}
