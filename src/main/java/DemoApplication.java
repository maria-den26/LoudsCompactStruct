import java.util.*;

public class DemoApplication {

    public static void main(String[] args) {
        System.out.println("=== ДЕМОНСТРАЦИЯ LOUDS TRIE и ОБЫЧНОГО TRIE ===\n");

        // Проверка корректности деревьев
        List<String> words = Arrays.asList(
                "apple", "app", "application", "banana", "band",
                "cat", "category", "dog", "domain", "door"
        );

        TrieLouds louds = generateTestLouds(words);
        System.out.println("\nLOUDS Trie. Количество узлов: " + louds.getNodeCount());

        List<String> loudsWords = louds.getAllWords();
        System.out.println("=== Слова LOUDS ===");
        for (String word : loudsWords)
            System.out.println(word);


        Trie trie = generateTestTrie(words);
        System.out.println("\nTrie. Количество узлов: " + trie.getNodeCount());
        List<String> trieWords = trie.getAllWords();
        System.out.println("=== Слова Trie ===");
        for (String word : trieWords)
            System.out.println(word);

        // Демонстрация поиска
        System.out.println("\nДЕМОНСТРАЦИЯ ПОИСКА:");
        List<String> testWords = Arrays.asList("app", "apple", "appl", "ban", "dog", "database", "datagram");

        for (String word : testWords) {
            boolean loudsResult = louds.search(word);
            boolean trieResult = trie.search(word);
            System.out.printf("Слово '%s': LOUDS=%b, Trie=%b, совпадение=%b%n",
                    word, loudsResult, trieResult, loudsResult == trieResult);
        }

        // Демонстрация поиска по префиксу
        System.out.println("\nПОИСК ПО ПРЕФИКСУ:");
        List<String> prefixes = Arrays.asList("app", "ban", "dat", "cat", "do");

        for (String prefix : prefixes) {
            boolean loudsHas = louds.startsWith(prefix);
            boolean trieHas = trie.startsWith(prefix);
            System.out.printf("Префикс '%s': LOUDS=%b, Trie=%b%n",
                    prefix, loudsHas, trieHas);

            // Покажем слова с этим префиксом
            if (trieHas) {
                List<String> wordsWithPrefix = trie.getWordsWithPrefix(prefix);
                System.out.printf("  Слова Trie с префиксом '%s': %s%n",
                        prefix, wordsWithPrefix);
            }
            /*if (loudsHas) {
                List<String> wordsWithPrefix = louds.getWordsWithPrefix(prefix);
                System.out.printf("  Слова LOADS с префиксом '%s': %s%n",
                        prefix, wordsWithPrefix);
            }*/

        }

        // Анализ памяти
        System.out.println("\nАНАЛИЗ ПАМЯТИ (ориентировочный):");
        analyzeMemory(louds, trie);

        // Запуск бенчмарков
        System.out.println("\nЗАПУСК БЕНЧМАРКОВ:");
        runBenchmarks();
    }

    public static TrieLouds generateTestLouds(List<String> words)
    {
        // Создаем корневой узел
        BasicLouds.TreeNode root = new BasicLouds.TreeNode(0, "\0"); // \0 - пустой символ для корня
        TrieLouds louds = new TrieLouds(root);

        if (words == null || words.isEmpty()) {
            return louds;
        }

        return TrieLouds.buildFromWordList(words);
    }

    public static Trie generateTestTrie(List<String> words)
    {
        Trie trie = new Trie();

        if (words == null || words.isEmpty()) {
            return trie;
        }

        // Добавляем все слова в Trie
        for (String word : words) {
            if (word != null && !word.isEmpty()) {
                trie.insert(word);
            }
        }

        return trie;
    }

    /**
     * Генератор случайных слов для тестирования
     */
    private static List<String> generateWords(int count, int minLength, int maxLength) {
        List<String> words = new ArrayList<>();
        Random random = new Random(42);

        for (int i = 0; i < count; i++) {
            String word = generateRandomWord(random, minLength, maxLength);
            words.add(word);
        }

        return words;
    }

    private static String generateRandomWord(Random random, int minLength, int maxLength) {
        String letters = "abcdefghijklmnopqrstuvwxyz";
        int length = minLength + random.nextInt(maxLength - minLength + 1);
        StringBuilder word = new StringBuilder();
        for (int j = 0; j < length; j++) {
            word.append(letters.charAt(random.nextInt(letters.length())));
        }
        return word.toString();
    }

    /**
     * Ориентировочный анализ памяти
     */
    private static void analyzeMemory(TrieLouds loudsTrie, Trie classicTrie) {
        // Ориентировочный расчет для LOUDS Trie
        int loudsNodes = loudsTrie.getNodeCount();
        int loudsBits = loudsTrie.getBitLength();
        int loudsBytesForBits = loudsBits / 8 + 1;
        int loudsBytesForData = loudsNodes * 2; // char + boolean (символ узла и флаг конца слова)

        // Таблицы rank/select
        int loudsTableBytes = 0;
        if (loudsTrie.rank1Table != null) {
            loudsTableBytes += loudsTrie.rank1Table.length * 4;
        }
        if (loudsTrie.select1Table != null) {
            loudsTableBytes += loudsTrie.select1Table.length * 4;
        }
        if (loudsTrie.select0Table != null) {
            loudsTableBytes += loudsTrie.select0Table.length * 4;
        }

        int totalLoudsBytes = loudsBytesForBits + loudsBytesForData + loudsTableBytes;

        // Ориентировочный расчет для обычного Trie
        int trieNodes = classicTrie.getNodeCount();
        // Каждый узел: HashMap (~48 байт) + boolean (1 байт) + ссылки(4 байта на ссылку)
        int trieBytesPerNode = 50; // консервативная оценка
        int totalTrieBytes = trieNodes * trieBytesPerNode;

        System.out.printf("LOUDS Trie:%n");
        System.out.printf("  Узлов: %d%n", loudsNodes);
        System.out.printf("  Битов: %d (~%d байт)%n", loudsBits, loudsBytesForBits);
        System.out.printf("  Таблицы rank/select: ~%d байт%n", loudsTableBytes);
        System.out.printf("  ОБЩАЯ ОЦЕНКА: ~%d байт (~%.2f KB)%n",
                totalLoudsBytes, totalLoudsBytes / 1024.0);

        System.out.printf("Обычный Trie:%n");
        System.out.printf("  Узлов: %d%n", trieNodes);
        System.out.printf("  ОБЩАЯ ОЦЕНКА: ~%d байт (~%.2f KB)%n",
                totalTrieBytes, totalTrieBytes / 1024.0);

        System.out.printf("ЭКОНОМИЯ LOUDS: %.1f%%%n",
                (1 - (double)totalLoudsBytes / totalTrieBytes) * 100);
    }

    /**
     * Запуск бенчмарков производительности
     */
    private static void runBenchmarks() {
        System.out.println("Генерация тестовых данных...");

        // Генерация тестовых данных
        List<String> benchmarkWords = generateWords(10000, 3, 10);
        List<String> searchWords = new ArrayList<>();

        // Берем 1000 слов для поиска (50% из вставленных, 50% случайных)
        Random random = new Random(42);
        for (int i = 0; i < 500; i++) {
            searchWords.add(benchmarkWords.get(random.nextInt(benchmarkWords.size())));
        }
        for (int i = 0; i < 500; i++) {
            searchWords.add(generateRandomWord(random, 3, 10));
        }

        // Создаем структуры
        System.out.println("Построение LOUDS Trie...");
        long startTime = System.currentTimeMillis();
        TrieLouds benchmarkLouds = TrieLouds.buildFromWordList(benchmarkWords);
        long loudsBuildTime = System.currentTimeMillis() - startTime;

        System.out.println("Построение обычного Trie...");
        startTime = System.currentTimeMillis();
        Trie benchmarkTrie = new Trie();
        for (String word : benchmarkWords) {
            benchmarkTrie.insert(word);
        }
        long trieBuildTime = System.currentTimeMillis() - startTime;

        // Бенчмарк поиска
        System.out.println("\nБЕНЧМАРК ПОИСКА (1000 слов):");

        // Поиск в LOUDS Trie
        startTime = System.currentTimeMillis();
        int loudsFound = 0;
        for (String word : searchWords) {
            if (benchmarkLouds.search(word)) {
                loudsFound++;
            }
        }
        long loudsSearchTime = System.currentTimeMillis() - startTime;

        // Поыск в обычном Trie
        startTime = System.currentTimeMillis();
        int trieFound = 0;
        for (String word : searchWords) {
            if (benchmarkTrie.search(word)) {
                trieFound++;
            }
        }
        long trieSearchTime = System.currentTimeMillis() - startTime;

        // Вывод результатов
        System.out.printf("Время построения:%n");
        System.out.printf("  LOUDS Trie: %d ms%n", loudsBuildTime);
        System.out.printf("  Обычный Trie: %d ms%n", trieBuildTime);

        System.out.printf("Время поиска:%n");
        System.out.printf("  LOUDS Trie: %d ms (найдено: %d)%n", loudsSearchTime, loudsFound);
        System.out.printf("  Обычный Trie: %d ms (найдено: %d)%n", trieSearchTime, trieFound);
        System.out.printf("  Отношение скоростей (Trie/LOUDS): %.2fx%n",
                (double)loudsSearchTime / trieSearchTime);

        // Анализ памяти для бенчмарка
        System.out.println("\nАНАЛИЗ ПАМЯТИ ДЛЯ БЕНЧМАРКА:");
        analyzeMemory(benchmarkLouds, benchmarkTrie);

        // Дополнительный тест: поиск по префиксу
        System.out.println("\nБЕНЧМАРК ПОИСКА ПО ПРЕФИКСУ (500 префиксов):");
        List<String> prefixes = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            String word = benchmarkWords.get(random.nextInt(benchmarkWords.size()));
            int prefixLength = Math.min(3, word.length());
            prefixes.add(word.substring(0, prefixLength));
        }

        startTime = System.currentTimeMillis();
        int loudsPrefixFound = 0;
        for (String prefix : prefixes) {
            if (benchmarkLouds.startsWith(prefix)) {
                loudsPrefixFound++;
            }
        }
        long loudsPrefixTime = System.currentTimeMillis() - startTime;

        startTime = System.currentTimeMillis();
        int triePrefixFound = 0;
        for (String prefix : prefixes) {
            if (benchmarkTrie.startsWith(prefix)) {
                triePrefixFound++;
            }
        }
        long triePrefixTime = System.currentTimeMillis() - startTime;

        System.out.printf("LOUDS Trie: %d ms (найдено: %d)%n", loudsPrefixTime, loudsPrefixFound);
        System.out.printf("Обычный Trie: %d ms (найдено: %d)%n", triePrefixTime, triePrefixFound);
    }

}
