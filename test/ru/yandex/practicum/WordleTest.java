package ru.yandex.practicum;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class WordleTest {

    private static PrintWriter log;
    private WordleDictionary dictionary;

    @BeforeAll
    static void initLog() {
        log = new PrintWriter(System.out, true);
    }

    @BeforeEach
    void setUp() {
        List<String> testWords = Arrays.asList("слово", "книга", "герой", "замок", "город");
        dictionary = new WordleDictionary(testWords);
    }

    @Test
    void dictionaryContainsWords() {
        assertTrue(dictionary.contains("слово"));
        assertTrue(dictionary.contains("книга"));
        assertTrue(dictionary.contains("герой"));
        assertFalse(dictionary.contains("домик"));
    }

    @Test
    void dictionarySizeCorrect() {
        assertEquals(5, dictionary.size());
    }

    @Test
    void dictionaryRandomWord() {
        Random rnd = new Random();
        String word = dictionary.getRandomWord(rnd);
        assertNotNull(word);
        assertTrue(dictionary.contains(word));
    }

    @Test
    void allWordsList() {
        List<String> words = dictionary.allWords();
        assertEquals(dictionary.size(), words.size());
        assertTrue(words.contains("город"));
    }

    @Test
    void loadDictionarySuccess() throws Exception {
        File tempFile = File.createTempFile("dict", ".txt");
        tempFile.deleteOnExit();

        try (FileWriter writer = new FileWriter(tempFile, StandardCharsets.UTF_8)) {
            writer.write("слово\n");
            writer.write("книга\n");
            writer.write("герой\n");
            writer.write("дом\n");
            writer.write("большой\n");
        }

        WordleDictionaryLoader loader = new WordleDictionaryLoader(log);
        WordleDictionary dict = loader.load(tempFile.getAbsolutePath());

        assertNotNull(dict);
        assertEquals(3, dict.size());
        assertTrue(dict.contains("слово"));
        assertTrue(dict.contains("книга"));
        assertFalse(dict.contains("дом"));
        assertFalse(dict.contains("большой"));
    }

    @Test
    void loadDictionaryFileNotFound() {
        WordleDictionaryLoader loader = new WordleDictionaryLoader(log);
        assertThrows(DictionaryLoadException.class, () -> loader.load("missing_file.txt"));
    }

    @Test
    void loadEmptyDictionaryThrows() throws Exception {
        File tempFile = File.createTempFile("empty_dict", ".txt");
        tempFile.deleteOnExit();
        WordleDictionaryLoader loader = new WordleDictionaryLoader(log);
        assertThrows(EmptyDictionaryException.class, () -> loader.load(tempFile.getAbsolutePath()));
    }

    @Test
    void loadDictionaryNormalization() throws Exception {
        File tempFile = File.createTempFile("norm_dict", ".txt");
        tempFile.deleteOnExit();

        try (FileWriter writer = new FileWriter(tempFile, StandardCharsets.UTF_8)) {
            writer.write("СЛОВО\n");   // нормализуется в "слово" 5 букв
            writer.write("книга\n");   // нормализуется в "книга" 5 букв
            writer.write("ёлкад\n");   // нормализуется в "елкад" 5 букв
            writer.write("Ёжикм\n");   // нормализуется в "ежикм" 5 букв
        }

        WordleDictionaryLoader loader = new WordleDictionaryLoader(log);
        WordleDictionary dict = loader.load(tempFile.getAbsolutePath());

        assertTrue(dict.contains("слово"));
        assertTrue(dict.contains("книга"));
        assertTrue(dict.contains("елкад"));   // вместо "ёлкад"
        assertTrue(dict.contains("ежикм"));   // вместо "Ёжикм"
    }

    @Test
    void evaluateAllCorrect() {
        WordleEvaluator evaluator = new WordleEvaluator();
        String pattern = evaluator.evaluate("слово", "слово");
        assertEquals("+++++", pattern);
    }

    @Test
    void evaluateAllWrong() {
        WordleEvaluator evaluator = new WordleEvaluator();
        String pattern = evaluator.evaluate("книга", "герой");
        assertEquals(5, pattern.length());
        for (char c : pattern.toCharArray()) {
            assertTrue(c == '-' || c == '^' || c == '+');
        }
    }

    @Test
    void evaluateMixed() {
        WordleEvaluator evaluator = new WordleEvaluator();
        String pattern = evaluator.evaluate("гонец", "герой");
        assertEquals(5, pattern.length());
        assertEquals('+', pattern.charAt(0));
    }

    @Test
    void gameInitialState() {
        WordleGame game = new WordleGame(dictionary, log);
        assertFalse(game.isFinished());
        assertFalse(game.isWon());
        assertEquals(6, game.stepsLeft());
    }

    @Test
    void makeValidTurn() throws GameException {
        WordleGame game = new WordleGame(dictionary, log);
        String word = dictionary.allWords().get(0);
        TurnResult result = game.makeTurn(word);
        assertEquals(word, result.word());
        assertEquals(5, result.pattern().length());
        assertEquals(5, game.stepsLeft());
    }

    @Test
    void makeInvalidLengthTurn() {
        WordleGame game = new WordleGame(dictionary, log);
        assertThrows(InvalidWordFormatException.class, () -> game.makeTurn("дом"));
    }

    @Test
    void makeWordNotInDictionaryTurn() {
        WordleGame game = new WordleGame(dictionary, log);
        assertThrows(WordNotFoundInDictionaryException.class, () -> game.makeTurn("абвгд"));
    }

    @Test
    void winGame() throws GameException {
        WordleGame game = new WordleGame(dictionary, log);
        String answer = game.getAnswer();
        TurnResult result = game.makeTurn(answer);
        assertTrue(game.isWon());
        assertTrue(game.isFinished());
        assertEquals("+++++", result.pattern());
    }

    @Test
    void maxStepsGame() throws GameException {
        WordleGame game = new WordleGame(dictionary, log);
        String wrongWord = "книга".equals(game.getAnswer()) ? "город" : "книга";
        for (int i = 0; i < 6; i++) {
            if (!game.isFinished()) game.makeTurn(wrongWord);
        }
        assertEquals(0, game.stepsLeft());
        assertTrue(game.isFinished());
        assertFalse(game.isWon());
    }

    @Test
    void hintProvidesValidWord() throws GameException {
        WordleGame game = new WordleGame(dictionary, log);
        String hint = game.suggest();
        assertNotNull(hint);
        assertTrue(dictionary.contains(hint));
    }

    @Test
    void hintExcludesUsedWords() throws GameException {
        WordleGame game = new WordleGame(dictionary, log);
        String firstHint = game.suggest();
        game.makeTurn(firstHint);
        String secondHint = game.suggest();
        assertNotEquals(firstHint, secondHint);
    }
}