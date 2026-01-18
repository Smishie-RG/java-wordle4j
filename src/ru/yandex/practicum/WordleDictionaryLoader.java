package ru.yandex.practicum;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class WordleDictionaryLoader {

    private final PrintWriter log;

    public WordleDictionaryLoader(PrintWriter log) {
        this.log = log;
    }

    public WordleDictionary load(String filename)
            throws DictionaryLoadException, EmptyDictionaryException {

        Set<String> unique = new LinkedHashSet<>();

        try (BufferedReader br = Files.newBufferedReader(
                Path.of(filename), StandardCharsets.UTF_8)) {

            String line;
            while ((line = br.readLine()) != null) {
                String word = normalize(line);

                if (word.length() != 5) continue;
                if (!isRussian(word)) continue;

                unique.add(word);
            }

        } catch (IOException e) {
            log.println("Ошибка загрузки словаря: " + e.getMessage());
            throw new DictionaryLoadException("Не удалось загрузить словарь", e);
        }

        if (unique.isEmpty()) {
            throw new EmptyDictionaryException("Словарь пуст или не содержит подходящих слов");
        }

        log.println("Словарь загружен. Количество слов: " + unique.size());
        return new WordleDictionary(new ArrayList<>(unique));
    }

    private String normalize(String s) {
        return s.trim().toLowerCase().replace('ё', 'е');
    }

    private boolean isRussian(String s) {
        for (char c : s.toCharArray()) {
            if (c < 'а' || c > 'я') return false;
        }
        return true;
    }
}