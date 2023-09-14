package com.vocabnote.app.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabnote.app.dto.word.Meanings;
import com.vocabnote.app.dto.word.ResponseWordDetails;
import com.vocabnote.app.exception.HttpErrorStatusException;
import com.vocabnote.app.model.Definition;
import com.vocabnote.app.model.Vocabulary;
import com.vocabnote.app.model.WordJson;
import com.vocabnote.app.repository.DefinitionRepository;
import com.vocabnote.app.repository.VocabularyRepository;
import com.vocabnote.app.utils.RestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@RestController
@RequestMapping("/word")
public class WordController {

    @Autowired
    private VocabularyRepository vocabularyRepository;

    @Autowired
    private DefinitionRepository definitionRepository;

        @GetMapping
    public void test(@RequestParam String keyword) {
            vocabularyRepository.findByWordStartsWith(keyword).forEach(vocab -> {
            System.out.println(vocab.getWord());
            if(vocab.getPhonetic() != null) return;
            if(vocab.getWord().contains("/")) return;
            try {
                ResponseWordDetails[] responseWordDetails = new RestClient()
                        .setContentType(MediaType.APPLICATION_JSON_VALUE)
                        .setUrl("https://api.dictionaryapi.dev/api/v2/entries/en/" + vocab.getWord())
                        .setMethod("GET")
                        .call(ResponseWordDetails[].class);

                Arrays.stream(responseWordDetails)
                        .forEach(responseWordDetail -> {
                            if(responseWordDetail.getPhonetic() != null) {
                                vocab.setPhonetic(responseWordDetail.getPhonetic());
                            }
                            responseWordDetail.getPhonetics().forEach(phonetic -> {
                                if(phonetic.getText() != null && !phonetic.getText().equals("") && vocab.getPhonetic() == null) {
                                    vocab.setPhonetic(phonetic.getText());
                                }
                                if(phonetic.getAudioSrc() != null && !phonetic.getAudioSrc().equals("")) {
                                    vocab.setAudioUrl(phonetic.getAudioSrc());
                                }
                            });
                            vocabularyRepository.save(vocab);
                        });
            } catch (HttpErrorStatusException e) {
                System.out.println(e.getStatusCode() + " :" + e.getMessage());
            }
        });
    }
    @GetMapping("/words")
    public void addAllWord() throws IOException {
        List<String> listPath;
        try (Stream<Path> stream = Files.list(Paths.get("D:/Download/AI/wordset-dictionary-master/data"))) {
            listPath = stream
                    .filter(file -> !Files.isDirectory(file))
                    .map(Path::toAbsolutePath)
                    .map(Path::toString)
                    .toList();
        }
        AtomicInteger countWordPos = new AtomicInteger();
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
                    countWordPos.getAndIncrement();
                    try {
                        WordJson wordJson = mapper.treeToValue(field.getValue(), WordJson.class);
                        System.out.println(wordJson.getWord());
                        List<String> poss;
                        if (wordJson.getMeanings() != null) {
                            poss = wordJson.getMeanings().stream()
                                    .map(WordJson.Meaning::getSpeech_part)
                                    .distinct()
                                    .toList();
                            poss.forEach(pos -> {
                                Vocabulary vocabulary = Vocabulary.builder()
                                        .word(wordJson.getWord())
                                        .pos(pos)
                                        .build();
                                vocabularyRepository.save(vocabulary);
                            });
                        }

                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                });

            }
        }
        System.out.println(countWordPos.get());
    }

    @GetMapping("/defFinal")
    public void addDefandSysnoysmFinal(@RequestParam("fileName") String fileName) throws IOException {
        List<String> jsonArr = Files.readAllLines(
                Paths.get("D:/Download/AI/wordset-dictionary-master/data" + "/" + fileName + ".json"),
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
                            Optional<Definition> definitionOptional = definitionRepository.findByWordDesc(meaning.getDef());
                            Vocabulary vocab = vocabularyRepository.findByWordAndPos(
                                    wordJson.getWord(), meaning.getSpeech_part()).get();
                            if (definitionOptional.isPresent()) {
                                Definition definition = definitionOptional.get();
                                if(!vocab.getDefinitions().isEmpty()) {
                                    boolean checkExistDef = vocab.getDefinitions().stream()
                                            .anyMatch(def -> definition.getWordDesc().equals(def.getWordDesc()));
                                    if(checkExistDef) return;
                                }
                                vocab.getDefinitions().add(definition);
                                vocabularyRepository.save(vocab);
                                return;
                            }
                            Definition definition = Definition.builder()
                                    .wordDesc(meaning.getDef())
                                    .examples(meaning.getExample())
                                    .vocabularies(List.of(vocab))
                                    .build();
                            Definition definitionSaved = definitionRepository.save(definition);
                            vocab.getDefinitions().add(definitionSaved);
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

