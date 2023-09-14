package com.vocabnote.app;

import com.vocabnote.app.dto.word.OxfordVocabDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.*;

public class OxfordDictionaryScraper {

    private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/106.0.0.0 Safari/537.36";

    public static void main(String[] args) {
        try {
//            System.out.print("Enter an English word: ");
//            String word = System.console().readLine().toLowerCase().trim().replace(" ", "-");
            String word = "increase";
            String primaryUrl = "https://www.oxfordlearnersdictionaries.com/definition/english/" + word;

            Document primaryDoc = getHtmlDocument(primaryUrl);
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

            List<OxfordVocabDTO> allResults = new ArrayList<>();

            for (Document soup : docList) {
                OxfordVocabDTO oxfordVocabDTO = downloadSingle(soup);
                if (oxfordVocabDTO.getWord().equals(word)) {
                    allResults.add(oxfordVocabDTO);
                }
            }
            System.out.println(allResults);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Document getHtmlDocument(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .get();
    }

    private static OxfordVocabDTO downloadSingle(Document soup) {
        String headword = soup.select("h1.headword").first().text();
        Element position = soup.select("span.pos").first();
        String pos = (position != null) ? position.text() : null;

        Element pronounceDivUs = soup.select("div.phons_n_am").first();
        String phoneticsUs = pronounceDivUs.select("span").first().text();
        String linkUs = pronounceDivUs.select("div.sound.pron-us").first().attr("data-src-mp3");

        Element pronounceDivUk = soup.select("div.phons_br").first();
        String phoneticsUk = pronounceDivUk.select("span").first().text();
        String linkUk = pronounceDivUk.select("div.sound.pron-uk").first().attr("data-src-mp3");

        return OxfordVocabDTO.builder()
                .word(headword)
                .pos(pos)
                .phonUs(phoneticsUs)
                .phoneUk(phoneticsUk)
                .phonUsUrl(linkUs)
                .phonUkUrl(linkUk)
                .build();
    }
}
