package ru.yandex.practicum;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class Wordle {

    public static void main(String[] args) {
        try (PrintWriter log = new PrintWriter(
                Files.newBufferedWriter(Path.of("wordle.log"), StandardCharsets.UTF_8))) {

            WordleDictionaryLoader loader = new WordleDictionaryLoader(log);
            WordleDictionary dictionary = loader.load("words_ru.txt");
            WordleGame game = new WordleGame(dictionary, log);

            Scanner scanner = new Scanner(System.in);

            while (!game.isFinished()) {
                System.out.println("Введите слово:");
                String input = scanner.nextLine();

                try {
                    TurnResult result = game.makeTurn(input);
                    System.out.println(result.word());
                    System.out.println(result.pattern());
                    System.out.println("Осталось попыток: " + game.stepsLeft());

                } catch (GameException e) {
                    System.out.println("Ошибка: " + e.getMessage());
                }
            }

            if (game.isWon()) {
                System.out.println("Победа!");
            } else {
                System.out.println("Попытки закончились.");
                System.out.println("Загаданное слово: " + game.getAnswer());
            }

        } catch (ProgramException e) {
            System.out.println("Ошибка программы. Подробности в логе.");
        } catch (Exception e) {
            System.out.println("Критическая ошибка.");
        }
    }
}