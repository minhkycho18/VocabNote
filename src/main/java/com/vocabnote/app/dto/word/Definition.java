package com.vocabnote.app.dto.word;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Definition {
    @JsonProperty("definition")
    private String definition;
    @JsonProperty("example")
    private String examples;
}
