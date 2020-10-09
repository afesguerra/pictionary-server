package com.afesguerra.pictionary.server.model;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

@Log4j2
public class WordGenerator {
    private static final String WORDS_TEXT_FILE_PATH = "/lista.txt";

    private final List<String> wordList;

    public WordGenerator() {
        try {
            final URI uri = WordGenerator.class.getResource(WORDS_TEXT_FILE_PATH).toURI();
            final Path filePath = Paths.get(uri);
            wordList = Files.lines(filePath)
                    .filter(Objects::nonNull)
                    .filter(s -> !s.isEmpty())
                    .map(String::trim)
                    .collect(Collectors.toList());
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException("Error retrieving list of words", e);
        }
    }

    public String getWord() {
        return wordList.get(new Random().nextInt(wordList.size()));
    }
}
