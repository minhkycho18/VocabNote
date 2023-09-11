package com.vocabnote.app.dto.word;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Phonetics {
    @JsonProperty("text")
    private String text;
    @JsonProperty("audio")
    private String audioSrc;
}
