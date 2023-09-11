package com.vocabnote.app.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabnote.app.model.Definition;
import com.vocabnote.app.model.Vocabulary;
import com.vocabnote.app.model.WordJson;
import com.vocabnote.app.repository.DefinitionRepository;
import com.vocabnote.app.repository.VocabularyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

@RestController
@RequestMapping("/word")
public class WordController {

    @Autowired
    private VocabularyRepository vocabularyRepository;

    @Autowired
    private DefinitionRepository definitionRepository;

    //    @GetMapping
//    public void test(@RequestParam String keyword) {
//        wordRepository.findAll().forEach(word -> {
//            System.out.println(word.getHeadword());
//            if(word.getPhonetic() != null) return;
//            if(word.getHeadword().contains("/")) return;
//            try {
//                ResponseWordDetails[] responseWordDetails = new RestClient()
//                        .setContentType(MediaType.APPLICATION_JSON_VALUE)
//                        .setUrl("https://api.dictionaryapi.dev/api/v2/entries/en/" + word.getHeadword())
//                        .setMethod("GET")
//                        .call(ResponseWordDetails[].class);
//
//                Arrays.stream(responseWordDetails)
//                        .forEach(responseWordDetail -> {
//                            if(responseWordDetail.getPhonetic() != null) {
//                                word.setPhonetic(responseWordDetail.getPhonetic());
//                            }
//                            responseWordDetail.getPhonetics().forEach(phonetic -> {
//                                if(phonetic.getText() != null && !phonetic.getText().equals("") && word.getPhonetic() == null) {
//                                    word.setPhonetic(phonetic.getText());
//                                }
//                                if(phonetic.getAudioSrc() != null && !phonetic.getAudioSrc().equals("")) {
//                                    word.setAudioUrl(phonetic.getAudioSrc());
//                                }
//                            });
//                            wordRepository.save(word);
//                            Optional<Meanings> optionalMeanings = responseWordDetail.getMeanings().stream()
//                                    .filter(meanings -> meanings.getPartOfSpeech().equals(word.getPos()))
//                                    .findFirst();
//                            if (optionalMeanings.isPresent()) {
//                                Meanings meanings = optionalMeanings.get();
//                                meanings.getDefinitions().stream()
//                                        .forEach(definition -> {
//                                            Word wordSave = wordRepository.findById(new WordId(word.getHeadword(),word.getPos())).get();
//                                            Definition definitionSave = Definition.builder()
//                                                    .wordDesc(definition.getDefinition())
//                                                    .examples(definition.getExamples())
//                                                    .word(wordSave)
//                                                    .build();
//                                            definitionRepository.save(definitionSave);
//                                        });
//                            }
//                        });
//            } catch (HttpErrorStatusException e) {
//                System.out.println(e.getStatusCode() + " :" + e.getMessage());
//            }
//        });
//    }
    @GetMapping("/readjson")
    public void test1() throws IOException {
        List<String> listPath ;
        try (Stream<Path> stream = Files.list(Paths.get("D:/Download/AI/wordset-dictionary-master/data"))) {
            listPath = stream
                    .filter(file -> !Files.isDirectory(file))
                    .map(Path::toAbsolutePath)
                    .map(Path::toString)
                    .toList();
        }
        for (String urlDict : listPath) {
            List<String> jsonArr = Files.readAllLines(
                    Paths.get(urlDict),
                    StandardCharsets.UTF_8);

            String json = String.join("", jsonArr);
            ObjectMapper mapper = new ObjectMapper();

            JsonNode jsonNode = mapper.readTree(json);
            if (jsonNode.isObject()) {
                Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
                fields.forEachRemaining(field -> {
                    try {
                        WordJson wordJson = mapper.treeToValue(field.getValue(), WordJson.class);
                        System.out.println(wordJson.getWord());
                        if (wordJson.getMeanings() != null) {
                            wordJson.getMeanings().forEach(meaning -> {
                                Vocabulary vocab;
                                Optional<Vocabulary> vocabularyOptional = vocabularyRepository.findByWordAndPos(
                                        wordJson.getWord(), meaning.getSpeech_part());
                                if (vocabularyOptional.isEmpty()) {
                                    Vocabulary newVocab = Vocabulary.builder()
                                            .word(wordJson.getWord())
                                            .pos(meaning.getSpeech_part())
                                            .build();
                                    vocab = vocabularyRepository.save(newVocab);
                                } else {
                                    vocab = vocabularyOptional.get();
                                }
                                Definition definition = Definition.builder()
                                        .wordDesc(meaning.getDef())
                                        .examples(meaning.getExample())
                                        .vocabularies(List.of(vocab))
                                        .build();
                                definition = definitionRepository.save(definition);
                                List<Definition> definitions = new ArrayList<>();
                                definitions.add(definition);
                                vocab.setDefinitions(definitions);
                                vocabularyRepository.save(vocab);
                            });
                        }
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }

    }
}
