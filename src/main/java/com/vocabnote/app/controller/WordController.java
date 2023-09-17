package com.vocabnote.app.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabnote.app.dto.word.Meanings;
import com.vocabnote.app.dto.word.OxfordVocabDTO;
import com.vocabnote.app.dto.word.ResponseWordDetails;
import com.vocabnote.app.model.Definition;
import com.vocabnote.app.model.Vocabulary;
import com.vocabnote.app.model.WordJson;
import com.vocabnote.app.repository.DefinitionRepository;
import com.vocabnote.app.repository.VocabularyRepository;
import com.vocabnote.app.utils.OxfordDictCrawler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

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

//    @GetMapping
//    public void test(@RequestParam String keyword) {
//            vocabularyRepository.findByWordStartsWith(keyword).forEach(vocab -> {
//            System.out.println(vocab.getWord());
//            if(vocab.getPhonetic() != null) return;
//            if(vocab.getWord().contains("/")) return;
//            try {
//                ResponseWordDetails[] responseWordDetails = new RestClient()
//                        .setContentType(MediaType.APPLICATION_JSON_VALUE)
//                        .setUrl("https://api.dictionaryapi.dev/api/v2/entries/en/" + vocab.getWord())
//                        .setMethod("GET")
//                        .call(ResponseWordDetails[].class);
//
//                Arrays.stream(responseWordDetails)
//                        .forEach(responseWordDetail -> {
//                            if(responseWordDetail.getPhonetic() != null) {
//                                vocab.setPhonetic(responseWordDetail.getPhonetic());
//                            }
//                            responseWordDetail.getPhonetics().forEach(phonetic -> {
//                                if(phonetic.getText() != null && !phonetic.getText().equals("") && vocab.getPhonetic() == null) {
//                                    vocab.setPhonetic(phonetic.getText());
//                                }
//                                if(phonetic.getAudioSrc() != null && !phonetic.getAudioSrc().equals("")) {
//                                    vocab.setAudioUrl(phonetic.getAudioSrc());
//                                }
//                            });
//                            vocabularyRepository.save(vocab);
//                        });
//            } catch (HttpErrorStatusException e) {
//                System.out.println(e.getStatusCode() + " :" + e.getMessage());
//            }
//        });
//    }

    @GetMapping("/get-all")
    public void getAllWords(){
        vocabularyRepository.findByPos("pronoun").forEach(System.out::println);
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

    @GetMapping("/phonetic")
    public void addPhonetic(@RequestParam String firstChar){
        List<Vocabulary> listResult = vocabularyRepository.findByWordStartsWith(firstChar);
        List<OxfordVocabDTO> oxfordPhonetic = new ArrayList<>();
        for (Vocabulary vocab : listResult) {
            if(vocab.getPhonetics_uk() == null){
                System.out.println(vocab.getWord());
                oxfordPhonetic.addAll(OxfordDictCrawler.getDataOxfordVocab(vocab.getWord()));
                for (OxfordVocabDTO oxfordWord: oxfordPhonetic) {
                    if(oxfordWord.getPos() != null){
                        if( Objects.equals(vocab.getWord(), oxfordWord.getWord()) && oxfordWord.getPos().contains(vocab.getPos())){
                            vocab = Vocabulary.builder()
                                    .vocabId(vocab.getVocabId())
                                    .word(vocab.getWord())
                                    .pos(vocab.getPos())
                                    .phonetics_us(oxfordWord.getPhoneUs())
                                    .phonetics_uk(oxfordWord.getPhoneUk())
                                    .audio_us(oxfordWord.getPhonUsUrl())
                                    .audio_uk(oxfordWord.getPhonUkUrl())
                                    .definitions(vocab.getDefinitions())
                                    .build();
                            vocabularyRepository.save(vocab);
                        }
                    } else {
                        if( Objects.equals(vocab.getWord(), oxfordWord.getWord())){
                             vocab = Vocabulary.builder()
                                     .vocabId(vocab.getVocabId())
                                    .word(vocab.getWord())
                                    .pos(vocab.getPos())
                                    .phonetics_us(oxfordWord.getPhoneUs())
                                    .phonetics_uk(oxfordWord.getPhoneUk())
                                    .audio_us(oxfordWord.getPhonUsUrl())
                                    .audio_uk(oxfordWord.getPhonUkUrl())
                                     .definitions(vocab.getDefinitions())
                                    .build();
                            vocabularyRepository.save(vocab);
                        }
                    }
                }
                oxfordPhonetic.clear();
            }
        }

    }

    @GetMapping("/phonetic-again")
    public void addPhoneticSecond(){
        List<Vocabulary> listResult = vocabularyRepository.findVocabulariesWithNullPhoneticsUk();
        List<OxfordVocabDTO> oxfordPhonetic = new ArrayList<>();
        for (Vocabulary vocab : listResult) {
            if(vocab.getPhonetics_uk() == null){
                System.out.println(vocab.getWord());
                oxfordPhonetic.addAll(OxfordDictCrawler.getDataOxfordVocab(vocab.getWord()));
                for (OxfordVocabDTO oxfordWord: oxfordPhonetic) {
                    if(oxfordWord.getPos() != null){
                        if( Objects.equals(vocab.getWord(), oxfordWord.getWord()) && oxfordWord.getPos().contains(vocab.getPos())){
                            vocab = Vocabulary.builder()
                                    .vocabId(vocab.getVocabId())
                                    .word(vocab.getWord())
                                    .pos(vocab.getPos())
                                    .phonetics_us(oxfordWord.getPhoneUs())
                                    .phonetics_uk(oxfordWord.getPhoneUk())
                                    .audio_us(oxfordWord.getPhonUsUrl())
                                    .audio_uk(oxfordWord.getPhonUkUrl())
                                    .definitions(vocab.getDefinitions())
                                    .build();
                            vocabularyRepository.save(vocab);
                        }
                    } else {
                        if( Objects.equals(vocab.getWord(), oxfordWord.getWord())){
                            vocab = Vocabulary.builder()
                                    .vocabId(vocab.getVocabId())
                                    .word(vocab.getWord())
                                    .pos(vocab.getPos())
                                    .phonetics_us(oxfordWord.getPhoneUs())
                                    .phonetics_uk(oxfordWord.getPhoneUk())
                                    .audio_us(oxfordWord.getPhonUsUrl())
                                    .audio_uk(oxfordWord.getPhonUkUrl())
                                    .definitions(vocab.getDefinitions())
                                    .build();
                            vocabularyRepository.save(vocab);
                        }
                    }
                }
                oxfordPhonetic.clear();
            }
        }

    }

    @GetMapping("/phrase")
    public void addPhrase(@RequestParam String firstChar){
        List<Vocabulary> listResult = vocabularyRepository.findByWordStartsWith(firstChar);
        List<OxfordVocabDTO> oxfordPhonetic = new ArrayList<>();
        for (Vocabulary vocab : listResult) {
            if(!vocab.getWord().contains(" ")){
                continue;
            }
            System.out.println(vocab.getWord());
            oxfordPhonetic.addAll(OxfordDictCrawler.getDataOxfordVocab(vocab.getWord()));
            for (OxfordVocabDTO oxfordWord: oxfordPhonetic) {
                if( Objects.equals(vocab.getWord(), oxfordWord.getWord()) && oxfordWord.getPos() != null){
                    if(Objects.equals(vocab.getPos(), "verb") && oxfordWord.getPos().equals("phrasal verb")){
                        vocab.setPos(oxfordWord.getPos());
                    }
                    vocab = Vocabulary.builder()
                            .vocabId(vocab.getVocabId())
                            .word(vocab.getWord())
                            .pos(vocab.getPos())
                            .phonetics_us(oxfordWord.getPhoneUs())
                            .phonetics_uk(oxfordWord.getPhoneUk())
                            .audio_us(oxfordWord.getPhonUsUrl())
                            .audio_uk(oxfordWord.getPhonUkUrl())
                            .definitions(vocab.getDefinitions())
                            .build();

                    vocabularyRepository.save(vocab);
                }
                }
            }
            oxfordPhonetic.clear();
        }

    @GetMapping("/add-def")
    public void addDefByPos(@RequestParam String pos){
        RestTemplate rt = new RestTemplate();
        List<Vocabulary> vocabularyList = vocabularyRepository.findByPos(pos);
        vocabularyList.forEach(vocabulary -> {
            System.out.println(vocabulary.getWord());
            String url = "https://api.dictionaryapi.dev/api/v2/entries/en/" + vocabulary.getWord();
            try {
                List<ResponseWordDetails> responseWordDetailsList = Arrays.stream(Objects.requireNonNull(rt.getForObject(url, ResponseWordDetails[].class))).toList();
                responseWordDetailsList.forEach(responseWordDetails -> {
                    List<Meanings> meanings = responseWordDetails.getMeanings();
                    meanings.forEach(meaning -> {
                        if(meaning.getPartOfSpeech().equals(pos)){
                            List<Definition> definitionList = new ArrayList<>();
                            meaning.getDefinitions().forEach(def -> {
                                Definition definition = Definition.builder()
                                        .wordDesc(def.getDefinition())
                                        .examples(def.getExamples())
                                        .vocabularies(List.of(vocabulary))
                                        .build();
                                Definition definitionSaved = definitionRepository.save(definition);
                                vocabulary.getDefinitions().add(definitionSaved);
                                vocabularyRepository.save(vocabulary);
                            });
                        }

                    });
                });
            } catch (HttpClientErrorException.NotFound e){
                System.out.println("Không tìm thấy định nghĩa cho từ: " + vocabulary.getWord());
            }
        });
    }

    @GetMapping("/phonetic-pos")
    public void addPhoneitcByPos(@RequestParam String pos){
        List<Vocabulary> listResult = vocabularyRepository.findByPos(pos);
        List<OxfordVocabDTO> oxfordPhonetic = new ArrayList<>();
        for (Vocabulary vocab : listResult) {
            System.out.println(vocab.getWord());
            oxfordPhonetic.addAll(OxfordDictCrawler.getDataOxfordVocab(vocab.getWord()));
            for (OxfordVocabDTO oxfordWord: oxfordPhonetic) {
                if( Objects.equals(vocab.getWord(), oxfordWord.getWord()) && oxfordWord.getPos() != null){
                    vocab = Vocabulary.builder()
                            .vocabId(vocab.getVocabId())
                            .word(vocab.getWord())
                            .pos(vocab.getPos())
                            .phonetics_us(oxfordWord.getPhoneUs())
                            .phonetics_uk(oxfordWord.getPhoneUk())
                            .audio_us(oxfordWord.getPhonUsUrl())
                            .audio_uk(oxfordWord.getPhonUkUrl())
                            .definitions(vocab.getDefinitions())
                            .build();
                    vocabularyRepository.save(vocab);
                }
            }
        }
        oxfordPhonetic.clear();
    }

    }

