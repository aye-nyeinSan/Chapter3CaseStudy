package com.example.ch3casestudy.controller;

import com.example.ch3casestudy.helpers.WordMapList;
import com.example.ch3casestudy.model.FileFreq;
import com.example.ch3casestudy.model.PDFdocument;
import org.pdfbox.pdfviewer.MapEntry;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collector;
import java.util.stream.Collectors;


public class WordMapMergeTask implements Callable<LinkedHashMap<String, ArrayList<FileFreq>>> {

    private Map<String,FileFreq>[] wordMap;
   public  WordMapMergeTask(Map<String, FileFreq>[] wordMap) {
       this.wordMap = wordMap;
   }

    @Override
    public LinkedHashMap<String, ArrayList<FileFreq>> call() throws Exception {
        LinkedHashMap<String, ArrayList<FileFreq>> uniqueSets;
        List<Map<String, FileFreq>> wordMapList =new ArrayList<>(Arrays.asList(wordMap));
        uniqueSets= wordMapList.stream()
                .flatMap(m-> m.entrySet().stream())
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collector.of(()->new WordMapList( new ArrayList<>()),
                                (list,item)-> {
                                    list.add(item.getValue());
                                   // System.out.println("list: "+list);
                                },
                                (current_list,new_items)->{
                                    current_list.addAll(new_items);

                                    return current_list;})
                ))
                .entrySet()
                .stream()
//                .sorted(Map.Entry.comparingByKey())//sorted alphabetically by Key
//                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
//                        (v1,v2)-> v1, LinkedHashMap::new)
//                        // Collect the sorted entries into a new LinkedHashMap. //v1 is key, v2 is creating new HashMap
//
//                );
                .collect(Collectors.toMap(item->
                            item.getKey() + "(" +
                                    item.getValue().stream()
                                            .map(FileFreq::getFreq)
                                            .sorted(Comparator.reverseOrder()) // Sort frequencies in descending order
                                            .map(Object::toString)
                                            .collect(Collectors.joining(",")) + ")"

                , Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(Map.Entry::getKey,
                        Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));




        return uniqueSets;//  LinkedHashMap<String, ArrayList<FileFreq>>
    }



}


