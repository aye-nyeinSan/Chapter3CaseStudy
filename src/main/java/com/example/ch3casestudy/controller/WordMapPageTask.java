package com.example.ch3casestudy.controller;

import com.example.ch3casestudy.model.FileFreq;
import com.example.ch3casestudy.model.PDFdocument;
import org.pdfbox.util.PDFTextStripper;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

public class WordMapPageTask implements Callable<Map<String,FileFreq>> {

    private PDFdocument doc;
    public WordMapPageTask(PDFdocument doc) throws IOException {
        this.doc = doc;
    }


    @Override
    public Map<String, FileFreq> call() throws Exception {
        Map<String, FileFreq> wordCount;
        PDFTextStripper reader = new PDFTextStripper();//Only Text in PDF
        Pattern pattern = Pattern.compile(" ");
        String s = reader.getText(doc.getDocument());
        doc.getDocument().close();

        wordCount = pattern.splitAsStream(s)
                .map(word -> word.replaceAll("[^a-zA-Z]", "").toLowerCase().trim())
                .filter(word -> word.length() > 3)
                .map(word -> new AbstractMap.SimpleEntry<>(word, 1))
                .collect(toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue, Integer::sum))
                .entrySet()
                .stream()
                .filter(e -> e.getValue() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, e -> new FileFreq(doc.getName(), doc.getFilePath(), e.getValue())));
         System.out.println(wordCount);
      //  System.out.println(doc.getFilePath());
        return wordCount;
    }

    public PDFdocument getDoc() {
        return doc;
    }

    public void setDoc(PDFdocument doc) {
        this.doc = doc;
    }
}
