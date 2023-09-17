package com.vocabnote.app.utils;

import com.vocabnote.app.dto.word.OxfordVocabDTO;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OxfordDictCrawler {
    private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/106.0.0.0 Safari/537.36";


    private OxfordDictCrawler() {
    }

    public static List<OxfordVocabDTO> getDataOxfordVocab(String word){
        List<OxfordVocabDTO> allResults = new ArrayList<>();
        String input = word.toLowerCase().trim().replace(" ", "-")
                .replace("’", "-").replace("'","-");
        try {
            String primaryUrl = "https://www.oxfordlearnersdictionaries.com/definition/english/" + input;
            System.out.println(primaryUrl);
            Document primaryDoc = getHtmlDocument(primaryUrl);

            //404
            if(primaryDoc == null){
                return allResults;
            }
            List<Document> docList = new ArrayList<>();
            docList.add(primaryDoc);

            Elements nearList = primaryDoc.select("div#relatedentries");

            if (!nearList.isEmpty()) {
                for (Element item : nearList.select("li")) {
                    String newWord = item.select("span").first().ownText().trim();
                    if (newWord.equals(word)) {
                        String link = item.select("a").first().attr("href");
                        Document otherDoc = getHtmlDocument(link);
                        docList.add(otherDoc);
                    }
                }
            }

            for (Document soup : docList) {
                OxfordVocabDTO oxfordVocabDTO = downloadSingle(soup);
                if (oxfordVocabDTO.getWord().equals(word)) {
                    allResults.add(oxfordVocabDTO);
                }
            }
//            System.out.println(allResults);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return allResults;
    }

    private static Document getHtmlDocument(String url) throws IOException {
        try {
            return Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .get();
        } catch (HttpStatusException e) {
            int statusCode = e.getStatusCode();
            return null;
        }
    }

    private static OxfordVocabDTO downloadSingle(Document soup) {
        String headword = soup.select("h1.headword").first().text();
        Element position = soup.select("span.pos").first();
        String pos = (position != null) ? position.text() : null;

        Element pronounceDivUs = soup.select("div.phons_n_am").first();
        String phoneticsUs = null;
        String linkUs = null;
        if (pronounceDivUs != null) {
            Element phoneticsElementUs = pronounceDivUs.select("span").first();
            if (phoneticsElementUs != null) {
                phoneticsUs = phoneticsElementUs.text();
            }
            Element soundElementUs = pronounceDivUs.select("div.sound.pron-us").first();
            if (soundElementUs != null) {
                linkUs = soundElementUs.attr("data-src-mp3");
            }
        }

        Element pronounceDivUk = soup.select("div.phons_br").first();
        String phoneticsUk = null;
        String linkUk = null;
        if (pronounceDivUk != null) {
            Element phoneticsElementUk = pronounceDivUk.select("span").first();
            if (phoneticsElementUk != null) {
                phoneticsUk = phoneticsElementUk.text();
            }
            Element soundElementUk = pronounceDivUk.select("div.sound.pron-uk").first();
            if (soundElementUk != null) {
                linkUk = soundElementUk.attr("data-src-mp3");
            }
        }

        return OxfordVocabDTO.builder()
                .word(headword)
                .pos(pos)
                .phoneUs(phoneticsUs)
                .phoneUk(phoneticsUk)
                .phonUsUrl(linkUs)
                .phonUkUrl(linkUk)
                .build();
    }
}
