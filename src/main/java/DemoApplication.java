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
            if (loudsHas) {
                List<String> wordsWithPrefix = louds.getWordsWithPrefix(prefix);
                System.out.printf("  Слова LOADS с префиксом '%s': %s%n",
                        prefix, wordsWithPrefix);
            }
        }


        // Запуск бенчмарков
        System.out.println("\nЗАПУСК БЕНЧМАРКОВ:");
        //runBenchmarks();
        System.out.println("\nВремя поиска слов:");
        searchTimeTest();
        System.out.println("\nВремя поиска по префиксу:");
        prefixTimeTest();
        System.out.println("\nВремя создания:");
        buildTimeAndMemoryTest();
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

    private static void searchTimeTest()
    {
        // Генерация тестовых данных
        List<String> benchmarkWords = generateWords(100000, 3, 10);

        int[] iterCnt = {5000, 7500, 10000, 25000, 50000, 75000, 100000};
        for (int i:iterCnt){

            List<String> searchWords = new ArrayList<>();
            //int testsize = i / 10;
            //int testsize = 500;
            int testsize = 1000;

            // Берем слова для поиска (50% из вставленных, 50% случайных)
            Random random = new Random(42);
            for (int j = 0; j < testsize/2; j++) {
                searchWords.add(benchmarkWords.get(random.nextInt(benchmarkWords.size())));
            }
            for (int j = 0; j < testsize/2; j++) {
                searchWords.add(generateRandomWord(random, 3, 10));
            }

            // Создаем структуры
            TrieLouds benchmarkLouds = TrieLouds.buildFromWordList(benchmarkWords.subList(0,i));

            Trie benchmarkTrie = new Trie();
            for (String word : benchmarkWords.subList(0,i)) {
                benchmarkTrie.insert(word);
            }

            // Поиск в LOUDS Trie
            long startTime = System.currentTimeMillis();
            int loudsFound = 0;
            for (String word : searchWords) {
                if (benchmarkLouds.search(word)) {
                    loudsFound++;
                }
            }
            long loudsSearchTime = System.currentTimeMillis() - startTime;

            // Поиск в обычном Trie
            startTime = System.currentTimeMillis();
            int trieFound = 0;
            for (String word : searchWords) {
                if (benchmarkTrie.search(word)) {
                    trieFound++;
                }
            }
            long trieSearchTime = System.currentTimeMillis() - startTime;
            System.out.printf("| Среднее время поиска (%d из %d слов)| %d ms | %d ms |%n", testsize, i, loudsSearchTime , trieSearchTime );

        }

    }

    private static void prefixTimeTest()
    {
        // Генерация тестовых данных
        List<String> benchmarkWords = generateWords(100000, 3, 10);
        List<String> prefixes = new ArrayList<>();

        int testsize = 500;

        // Берем префиксы для поиска
        Random random = new Random(42);
        for (int j = 0; j < testsize; j++) {
            //prefixes.add(generateRandomWord(random, 2, 5));
            // Выбираем случайное слово
            String randomWord = benchmarkWords.get(random.nextInt(benchmarkWords.size()));
            // Генерируем случайную длину префикса (от 1 до длины слова)
            int minPrefixLength = 2;
            int maxPrefixLength = randomWord.length() - 1 ; // максимум 3 символа
            int prefixLength = minPrefixLength + random.nextInt(maxPrefixLength - minPrefixLength + 1);

            // Добавляем префикс
            prefixes.add(randomWord.substring(0, prefixLength));
        }

        int[] iterCnt = {5000, 7500, 10000, 25000, 50000, 75000, 100000};
        for (int i:iterCnt){

            // Создаем структуры
            TrieLouds benchmarkLouds = TrieLouds.buildFromWordList(benchmarkWords.subList(0,i));

            Trie benchmarkTrie = new Trie();
            for (String word : benchmarkWords.subList(0,i)) {
                benchmarkTrie.insert(word);
            }

            int cnt = 0;

            // Поиск в LOUDS Trie
            long startTime = System.currentTimeMillis();
            for (String prefix : prefixes) {
                boolean loudsHas = benchmarkLouds.startsWith(prefix);
                if (loudsHas) {
                    cnt++;
                    List<String> wordsWithPrefix = benchmarkLouds.getWordsWithPrefix(prefix);
                }
            }
            long loudsSearchTime = (System.currentTimeMillis() - startTime) ;// cnt;

            cnt = 0;

            // Поиск в обычном Trie
            startTime = System.currentTimeMillis();
            for (String prefix : prefixes) {
                boolean trieHas = benchmarkTrie.startsWith(prefix);
                if (trieHas) {
                    cnt++;
                    List<String> wordsWithPrefix = benchmarkTrie.getWordsWithPrefix(prefix);
                }
            }
            long trieSearchTime = (System.currentTimeMillis() - startTime) ;// cnt ;
            System.out.printf("| Среднее время поиска (%d префиксов, %d слов)| %d ms | %d ms |%n", cnt, i, loudsSearchTime , trieSearchTime );

        }

    }

    private static void buildTimeAndMemoryTest() {
        // Для сохранения ссылок, чтобы GC не удалил объекты
        List<Object> keepAlive = new ArrayList<>();
        List<String> benchmarkWords = generateWords(100_000, 3, 10);
        int[] iterCnt = {5000, 7500, 10000, 25000, 50000, 75000, 100000};
        System.out.println("| Количество слов | LOUDS: время (мс) | LOUDS: память (Кб) | Trie: время (мс) | Trie: память (Кб) |");
        System.out.println("|:---------------:|:-----------------:|:------------------:|:----------------:|:-----------------:|");
    
        for (int i : iterCnt) {
            // LOUDS
            System.gc();
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            long memBefore = getUsedMemory();
            long startTime = System.currentTimeMillis();
            TrieLouds benchmarkLouds = TrieLouds.buildFromWordList(benchmarkWords.subList(0, i));
            long buildTime = System.currentTimeMillis() - startTime;
            long memAfter = getUsedMemory();
            long loudsMemory = (memAfter - memBefore) / 1024;
            keepAlive.add(benchmarkLouds);

            // Trie
            System.gc();
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            long memBeforeTrie = getUsedMemory();
            long startTimeTrie = System.currentTimeMillis();
            Trie benchmarkTrie = new Trie();
            for (String word : benchmarkWords.subList(0, i)) {
                benchmarkTrie.insert(word);
            }
            long buildTimeTrie = System.currentTimeMillis() - startTimeTrie;
            long memAfterTrie = getUsedMemory();
            long trieMemory = (memAfterTrie - memBeforeTrie) / 1024;
            keepAlive.add(benchmarkTrie);
    
            System.out.printf("| %d | %d | %d | %d | %d |\n", i, buildTime, loudsMemory, buildTimeTrie, trieMemory);
        }
    }
    
    private static long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        return runtime.totalMemory() - runtime.freeMemory();
    }
}
