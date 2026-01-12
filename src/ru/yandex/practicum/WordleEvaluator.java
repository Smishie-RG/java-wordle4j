package ru.yandex.practicum;

public class WordleEvaluator {

    public String evaluate(String guess, String answer) {
        char[] result = new char[5];
        int[] freq = new int[32];

        for (int i = 0; i < 5; i++) {
            freq[answer.charAt(i) - 'а']++;
        }

        for (int i = 0; i < 5; i++) {
            if (guess.charAt(i) == answer.charAt(i)) {
                result[i] = '+';
                freq[guess.charAt(i) - 'а']--;
            }
        }

        for (int i = 0; i < 5; i++) {
            if (result[i] == '+') continue;

            int idx = guess.charAt(i) - 'а';
            if (freq[idx] > 0) {
                result[i] = '^';
                freq[idx]--;
            } else {
                result[i] = '-';
            }
        }

        return new String(result);
    }
}