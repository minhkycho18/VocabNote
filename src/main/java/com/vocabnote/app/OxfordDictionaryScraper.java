package com.vocabnote.app;

import com.vocabnote.app.dto.word.OxfordVocabDTO;
import com.vocabnote.app.model.Vocabulary;
import com.vocabnote.app.repository.VocabularyRepository;
import com.vocabnote.app.utils.OxfordDictCrawler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.*;

public class OxfordDictionaryScraper {
    public static void main(String[] args) {

        List<OxfordVocabDTO> datas = new ArrayList<>();
        datas = OxfordDictCrawler.getDataOxfordVocab("yacht");
        datas.forEach(System.out::println);
    }


}

