package ru.yandex.practicum;

import java.io.PrintWriter;
import java.util.*;

public class WordleGame {

    private final WordleDictionary dictionary;
    private final PrintWriter log;
    private final WordleEvaluator evaluator = new WordleEvaluator();

    private String answer;
    private int steps;
    private final int maxSteps = 6;

    private final List<String> guesses = new ArrayList<>();
    private final List<String> patterns = new ArrayList<>();
    private final Set<String> usedWords = new HashSet<>();

    private boolean isWon;
    private boolean isFinished;

    public WordleGame(WordleDictionary dictionary, PrintWriter log) {
        this.dictionary = dictionary;
        this.log = log;

        this.answer = dictionary.getRandomWord();
        this.steps = maxSteps;
        this.isWon = false;
        this.isFinished = false;

    }

    public TurnResult makeTurn(String input) throws GameException {
        String guess = WordleDictionary.normalize(input);

        if (!isValidGuess(guess)) {
            throw new InvalidWordFormatException("нужно слово из 5 русских букв");
        }

        if (!dictionary.contains(guess)) {
            throw new WordNotFoundInDictionaryException("Слова нет в словаре");
        }

        steps--;
        usedWords.add(guess);
        String pattern = evaluator.evaluate(guess, answer);

        guesses.add(guess);
        patterns.add(pattern);

        if (guess.equals(answer)) {
            isWon = true;
            isFinished = true;
        }

        if (steps == 0 && !isWon) {
            isFinished = true;
        }

        return new TurnResult(guess, pattern);
    }

    public String suggest() {
        List<String> candidates = new ArrayList<>();

        for (String word : dictionary.allWords()) {
            if (usedWords.contains(word)) {
                continue;
            }

            boolean ok = true;
            for (int i = 0; i < guesses.size(); i++) {
                String g = guesses.get(i);
                String p = patterns.get(i);

                if (!evaluator.evaluate(g, word).equals(p)) {
                    ok = false;
                    break;
                }
            }

            if (ok) {
                candidates.add(word);
            }
        }

        candidates.removeAll(usedWords);

        if (candidates.isEmpty()) {
            log.println("Подходящих слов для подсказки не найдено");
            return null;
        }

        Random rnd = new Random();
        String hint = candidates.get(rnd.nextInt(candidates.size()));
        usedWords.add(hint);
        log.println("Подсказка: " + hint + " (всего вариантов: " + candidates.size() + ")");
        return hint;
    }

    private boolean isValidGuess(String s) {
        if (s.length() != 5) {
            return false;
        }

        for (char c : s.toCharArray()) {
            if (c < 'а' || c > 'я') {
                return false;
            }
        }

        return true;
    }

    public boolean isWon() {
        return isWon;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public int stepsLeft() {
        return steps;
    }

    public String getAnswer() {
        return answer;
    }
}