import java.util.*;

public class TrieLouds extends BasicLouds {

    protected final List<Boolean> isWordEnd = new ArrayList<>();

    /**
     * Построение префиксного дерева LOUDS
     */
    public TrieLouds(TreeNode root) {
        super();
        buildFromTree(root);
        buildRankSelectTables();
    }

    @Override
    protected void buildFromTree(TreeNode root) {
        StringBuilder bits = new StringBuilder();
        bits.append("10");  // Искусственный корень

        Queue<TreeNode> queue = new LinkedList<>();
        queue.add(root);
        nodeCount = 1;  // Учли корень

        // BFS обход
        while (!queue.isEmpty()) {
            TreeNode node = queue.poll();

            isWordEnd.add(node.nodeData.endsWith("*")); // является ли узел концом слова
            loudsData.add(node.nodeData.substring(0,1)); // Записываем символ узла

            int degree = node.children.size(); // Определяем количество потомков

            // Добавляем единицы по количеству потомков и один ноль
            for (int i = 0; i < degree; i++) {
                bits.append('1');
            }
            bits.append('0');

            // Добавляем потомков в очередь
            for (TreeNode child : node.children) {
                queue.add(child);
                nodeCount++;
            }
        }

        totalBits = 2 * nodeCount + 1;

        // Преобразуем строку в BitSet
        loudsBits = new BitSet(bits.length());
        for (int i = 0; i < bits.length(); i++) {
            if (bits.charAt(i) == '1') {
                loudsBits.set(i);
            }
        }
    }

    /**
     * ========== МЕТОДЫ ДЛЯ ПРЕФИКСНОГО ДЕРЕВА ==========
     */

    /**
     * Получить символ узла
     */
    private char getNodeChar(int nodeNumber) {
        String data = loudsData.get(nodeNumber);
        // Возвращаем первый символ (символ узла)
        return data.charAt(0);
    }

    /**
     * Проверить, является ли узел концом слова
     */
    public boolean isWordEnd(int nodeNumber) {
        return isWordEnd.get(nodeNumber);
    }

    /**
     * Найти дочерний узел по символу
     */
    public int findChildByChar(int nodeNumber, char ch) {
        int firstChild = firstChild(nodeNumber);
        if (firstChild == -1) {
            return -1; // Нет детей
        }

        int degree = degree(nodeNumber);
        /*for (int i = 0; i < degree; i++) {
            int childNode = firstChild + i;
            if (getNodeChar(childNode) == ch) {
                return childNode;
            }
        }*/
        // Бинарный поиск в отсортированном массиве детей
        int left = 0;
        int right = degree - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;
            int childNode = firstChild + mid;
            char childChar = getNodeChar(childNode);

            if (childChar == ch) {
                return childNode; // Нашли
            } else if (childChar < ch) {
                left = mid + 1; // Ищем справа
            } else {
                right = mid - 1; // Ищем слева
            }
        }

        return -1;
    }

    /**
     * Поиск слова в префиксном дереве
     */
    public boolean search(String word) {
        int currentNode = 0; // Начинаем с корня

        for (char ch : word.toCharArray()) {
            int childNode = findChildByChar(currentNode, ch);

            if (childNode == -1) {
                return false; // Символ не найден
            }

            currentNode = childNode;
        }

        // Проверяем, что последний узел помечен как конец слова
        return isWordEnd(currentNode);
    }

    /**
     * Проверить, есть ли слова с данным префиксом
     */
    public boolean startsWith(String prefix) {
        int currentNode = 0; // Начинаем с корня

        for (char ch : prefix.toCharArray()) {
            int childNode = findChildByChar(currentNode, ch);

            if (childNode == -1) {
                return false; // Префикс не найден
            }

            currentNode = childNode;
        }

        return true; // Весь префикс найден
    }

    /**
     * Получить все слова в дереве
     */
    public List<String> getAllWords() {
        List<String> words = new ArrayList<>();
        StringBuilder currentWord = new StringBuilder();
        getAllWordsDFS(0, currentWord, words);
        return words;
    }

    /**
     * Рекурсивный обход для получения всех слов
     */
    private void getAllWordsDFS(int nodeNumber, StringBuilder currentWord, List<String> words) {
        // Добавляем символ текущего узла (кроме корня)
        if (nodeNumber != 0) {
            currentWord.append(getNodeChar(nodeNumber));
        }

        // Если узел помечен как конец слова, добавляем слово в список
        if (isWordEnd(nodeNumber)) {
            words.add(currentWord.toString());
        }

        // Рекурсивно обходим всех детей
        int firstChild = firstChild(nodeNumber);
        if (firstChild != -1) {
            int degree = degree(nodeNumber);
            for (int i = 0; i < degree; i++) {
                int childNode = firstChild + i;
                getAllWordsDFS(childNode, new StringBuilder(currentWord), words);
            }
        }
    }

    public List<String> getWordsWithPrefix(String prefix) {
        List<String> result = new ArrayList<>();

        // 1. Находим узел, соответствующий префиксу
        int prefixNode = findNodeByPrefix(prefix);
        if (prefixNode == -1) {
            return result; // префикс не найден
        }

        // 2. Собираем все слова из поддерева
        if (isWordEnd(prefixNode)) {
            result.add(prefix); // сам префикс является словом
        }

        // 3. Рекурсивно обходим поддерево
        StringBuilder currentWord = new StringBuilder(prefix);
        getAllWordsDFS(prefixNode, currentWord, result);

        return result;
    }

    private int findNodeByPrefix(String prefix) {
        int currentNode = 0;

        for (char ch : prefix.toCharArray()) {
            int childNode = findChildByChar(currentNode, ch);
            if (childNode == -1) {
                return -1; // префикс не найден
            }
            currentNode = childNode;
        }

        return currentNode;
    }

    public static TrieLouds buildFromWordList(List<String> words) {
        // Создаем корневой узел для префиксного дерева
        TreeNode root = new TreeNode(0, "\0"); // Корень имеет пустой символ

        // Вставляем все слова в префиксное дерево
        for (String word : words) {
            if (word == null || word.isEmpty()) continue;

            TreeNode current = root;
            for (int i = 0; i < word.length(); i++) {
                char ch = word.charAt(i);
                // Ищем дочерний узел с таким символом
                BasicLouds.TreeNode foundChild = null;
                for (BasicLouds.TreeNode child : current.children) {
                    if (child.nodeData.charAt(0) == ch) {
                        foundChild = child;
                        break;
                    }
                }

                if (foundChild == null) {
                    // Создаем новый узел
                    BasicLouds.TreeNode newNode = new BasicLouds.TreeNode(-1, String.valueOf(ch));
                    current.addChild(newNode);
                    current = newNode;
                } else {
                    current = foundChild;
                }

                // Если это последний символ слова, отмечаем узел как конец слова
                if (i == word.length() - 1) {
                    // Если уже есть маркер конца слова (например, "a*"), оставляем его
                    if (!current.nodeData.endsWith("*")) {
                        current.nodeData += "*"; // Добавляем звездочку как маркер конца слова
                    }
                }
            }
        }

        // Присваиваем id узлам в порядке BFS
        Queue<TreeNode> queue = new LinkedList<>();
        queue.add(root);
        int nextId = 0;

        while (!queue.isEmpty()) {
            TreeNode node = queue.poll();
            node.id = nextId++;

            // Сортируем детей по символу для детерминированного порядка
            node.children.sort(Comparator.comparingInt(c -> c.nodeData.charAt(0)));

            queue.addAll(node.children);
        }

        return new TrieLouds(root);
    }

}
