package com.vocabnote.app.dto.word;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class OxfordVocabDTO {
    private String word;
    private String pos;
    private String phonUs;
    private String phoneUk;
    private String phonUsUrl;
    private String phonUkUrl;

    @Override
    public String toString() {
        return "\nOxfordVocabDTO{" +
                "\nword='" + word + '\'' +
                "\n, pos='" + pos + '\'' +
                "\n, phonUs='" + phonUs + '\'' +
                "\n, phoneUk='" + phoneUk + '\'' +
                "\n, phonUsUrl='" + phonUsUrl + '\'' +
                "\n, phonUkUrl='" + phonUkUrl + '\'' +
                '}';
    }
}
