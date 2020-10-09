package com.afesguerra.pictionary.server.modelo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

public class WordGenerator {
    private static final String WORDS_TEXT_FILE_PATH = "lista.txt";

    private List<String> wordList;

    public WordGenerator() {
        try {
            wordList = Files.lines(Paths.get(WORDS_TEXT_FILE_PATH))
                    .filter(Objects::nonNull)
                    .filter(s -> !s.isEmpty())
                    .map(String::trim)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getWord() {
        return wordList.get(new Random().nextInt(wordList.size()));
    }
}
