package ru.yandex.practicum;

public class TurnResult {
    private final String word;
    private final String pattern;

    public TurnResult(String word, String pattern) {
        this.word = word;
        this.pattern = pattern;
    }

    public String word() {
        return word;
    }

    public String pattern() {
        return pattern;
    }
}