import java.util.*;

public class Trie {
    TrieNode root = new TrieNode();;
    private int nodeCount = 1;

    /**
     * Класс для представления узла дерева
     */
    public static class TrieNode {
        Map<Character, TrieNode> children;
        boolean terminates;

        public TrieNode() {
            this.children = new HashMap<>();
        }
    }

    public void insert(String word) {
        TrieNode current = root;
        for (char c : word.toCharArray()) {
            if (!current.children.containsKey(c)) {
                current.children.put(c, new TrieNode());
                nodeCount++;
            }
            current = current.children.get(c);
        }
        current.terminates = true;
    }

    public boolean search(String word) {
        TrieNode current = root;
        for (char c : word.toCharArray()) {
            if (!current.children.containsKey(c)) {
                return false;
            }
            current = current.children.get(c);
        }
        return current.terminates;
    }

    public boolean startsWith(String prefix) {
        TrieNode current = root;
        for (char c : prefix.toCharArray()) {
            if (!current.children.containsKey(c)) {
                return false;
            }
            current = current.children.get(c);
        }
        return true;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    /**
     * Получить все слова в дереве
     */
    public List<String> getAllWords() {
        List<String> words = new ArrayList<>();
        collectWords(root, new StringBuilder(), words);
        return words;
    }

    /**
     * Рекурсивный обход для получения всех слов
     */
    private void collectWords(TrieNode node, StringBuilder prefix, List<String> words) {
        // Если текущий узел является концом слова, добавляем его в список
        if (node.terminates) {
            words.add(prefix.toString());
        }

        // Рекурсивно обходим всех потомков
        for (Map.Entry<Character, TrieNode> entry : node.children.entrySet()) {
            char c = entry.getKey();
            TrieNode child = entry.getValue();

            // Добавляем текущий символ к префиксу
            prefix.append(c);
            collectWords(child, prefix, words);
            // Удаляем последний символ для возврата к предыдущему состоянию
            prefix.deleteCharAt(prefix.length() - 1);
        }
    }

    /**
     * Метод для получения слов с заданным префиксом
     */
    public List<String> getWordsWithPrefix(String prefix) {
        List<String> words = new ArrayList<>();

        // Находим узел, соответствующий префиксу
        TrieNode current = root;
        for (char c : prefix.toCharArray()) {
            if (!current.children.containsKey(c)) {
                return words; // пустой список, если префикс не найден
            }
            current = current.children.get(c);
        }

        // Собираем все слова, начинающиеся с этого префикса
        collectWords(current, new StringBuilder(prefix), words);
        return words;
    }

}
