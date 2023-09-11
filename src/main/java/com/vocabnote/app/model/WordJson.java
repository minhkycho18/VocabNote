package com.vocabnote.app.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;
import java.util.Map;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WordJson {
    private String word;
    private String wordset_id;
    private List<Meaning> meanings;
    private List<Label> labels;
    private List<String> editors;
    private List<String> contributors;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Meaning {
        private String id;
        private String def;
        private String speech_part;
        private List<String> synonyms;
        private String example;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Label {
        private String name;
        private boolean is_dialect;
    }

}
