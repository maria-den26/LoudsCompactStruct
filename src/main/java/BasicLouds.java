import java.util.*;

/**
 * Базовая реализация LOUDS (Level-Order Unary Degree Sequence) для упорядоченных деревьев
 */
public class BasicLouds {

    protected BitSet loudsBits;                                     // Битовый вектор LOUDS
    protected int nodeCount;                                        // Количество узлов в дереве
    protected int totalBits;                                        // В LOUDS битовая строка всегда имеет длину 2n + 1
    protected final List<String> loudsData = new ArrayList<>();     // Данные всех узлов дерева в порядке нумерации узлов
    protected int[] rank1Table;                                     // Таблица для ускорения rank1
    protected int[] select1Table;                                   // Таблица для ускорения select1
    protected int[] select0Table;                                   // Таблица для ускорения select0

    /**
     * Класс для представления узла дерева (для построения)
     */
    public static class TreeNode {
        public int id;

        public String nodeData;

        public List<TreeNode> children = new ArrayList<>();

        public TreeNode(int id, String data) {
            this.id = id;
            this.nodeData = data;
        }

        public void addChild(TreeNode child) {
            children.add(child);
        }
    }

    /**
     * Построение LOUDS из дерева
     */
    public BasicLouds() {}

    public BasicLouds(TreeNode root) {
        buildFromTree(root);
        buildRankSelectTables();
    }

    /**
     * Построение битовой строки LOUDS или LBS (LOUDS Bit String) обходом в ширину (BFS)
     */
    protected void buildFromTree(TreeNode root) {
        StringBuilder bits = new StringBuilder();
        bits.append("10");  // Искусственный корень

        Queue<TreeNode> queue = new LinkedList<>();
        queue.add(root);
        nodeCount = 1;  // Учли корень

        // BFS обход
        while (!queue.isEmpty()) {
            TreeNode node = queue.poll();

            loudsData.add(node.nodeData); // Записываем данные узла

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
     * Построение таблиц для ускорения rank и select
     */
    protected void buildRankSelectTables() {
        rank1Table = new int[totalBits];
        select1Table = new int[totalBits - nodeCount - 1];
        select0Table = new int[nodeCount + 1];

        int cnt1 = 0;
        int cnt0 = 0;
        for (int i = 0; i < totalBits; i++) {
            if (loudsBits.get(i))
            {
                select1Table[cnt1] = i;
                cnt1++;
            }
            else
            {
                select0Table[cnt0] = i;
                cnt0++;
            }
            rank1Table[i] = cnt1;
        }

    }

    /**
     * Операция rank1(i) - количество единиц до позиции i (включительно)
     */
    public int rank1(int i) {
        if (i < 0) return 0;
        if (i > totalBits) return totalBits - nodeCount - 1;
        return rank1Table[i];
    }

    /**
     * Операция rank0(i) - количество нулей до позиции i (включительно)
     */
    public int rank0(int i) {
        if (i < 0) return 0;
        if (i > totalBits) return nodeCount + 1;
        return i - rank1Table[i] + 1;
    }

    /**
     * Операция select1(k) - позиция k-ой единицы (1-based)
     */
    public int select1(int k) {
        if (k <= 0 || k > totalBits - nodeCount - 1) return -1;
        return select1Table[k-1];
    }

    /**
     * Операция select0(k) - позиция k-ого нуля (1-based)
     */
    public int select0(int k) {
        if (k <= 0 || k > nodeCount + 1) return -1;
        return select0Table[k-1];
    }

    /**
     * ========== МЕТОДЫ НАВИГАЦИИ ПО ДЕРЕВУ ==========
     */

    /**
     * Получить родителя узла (по номеру узла)
     */
    public int parent(int nodeNumber) {
        return rank0(select1(nodeNumber+1)-1)-1;
    }

    /**
     * Получить первого ребёнка узла
     */
    public int firstChild(int nodeNumber) {
        int nodePos = select0(nodeNumber+1);
        // Если следующий бит узла - 0, то детей нет
        if (!loudsBits.get(nodePos + 1)) {
            return -1;  // Нет детей
        }
        return nodePos - nodeNumber;
    }

    /**
     * Получить последнего ребёнка узла
     */
    public int lastChild(int nodeNumber) {
        int nodePos = select0(nodeNumber+1);
        if (!loudsBits.get(nodePos + 1)) {
            return -1;  // Нет детей
        }
        return rank1(select0(nodeNumber+2)-2);
    }

    /**
     * Получить следующего sibling (брата/сестру)
     */
    public int nextSibling(int nodeNumber) {
        int parentNode = parent(nodeNumber);
        int rank = childRank(nodeNumber);
        return child(parentNode, rank + 1);
    }

    /**
     * Получить предыдущего sibling
     */
    public int prevSibling(int nodeNumber) {
        if (nodeNumber <= 0) return -1;
        int parentNode = parent(nodeNumber);
        int rank = childRank(nodeNumber);
        return child(parentNode, rank - 1);
    }

    /**
     * Проверить, является ли узел листом
     */
    public boolean isLeaf(int nodeNumber) {
        int nodePos = select0(nodeNumber+1);
        // Если следующий бит узла - 0, то детей нет
        return !loudsBits.get(nodePos + 1);
    }

    /**
     * Получить степень узла (количество детей)
     */
    public int degree(int nodeNumber) {
        if (firstChild(nodeNumber) == -1) return 0;
        return lastChild(nodeNumber) - firstChild(nodeNumber) + 1;
    }

    /**
     * Получить i-го ребёнка (индексация с 1)
     */
    public int child(int nodeNumber, int childIndex) {
        if (childIndex < 1 || childIndex > degree(nodeNumber)) return - 1;
        return firstChild(nodeNumber) + childIndex - 1;
    }

    /**
     * Получить номер ребёнка среди детей родителя (1-based)
     */
    public int childRank(int nodeNumber) {
        int parentNode = parent(nodeNumber);
        if (parentNode == -1)
            return -1;
        int first = firstChild(parentNode);
        if (first == nodeNumber)
            return 1;
        return nodeNumber - first + 1;  // На сколько позиций правее
    }

    /**
     * Визуализация битовой строки LOUDS
     */
    public String getLoudsString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < totalBits; i++) {
            boolean bit = loudsBits.get(i);
            sb.append(bit ? '1' : '0');
            if (!bit) {
                sb.append(' ');
            }
        }

        return sb.toString().trim();
    }

    /**
     * Получить количество узлов
     */
    public int getNodeCount() {
        return nodeCount;
    }

    /**
     * Получить длину битовой строки
     */
    public int getBitLength() {
        return totalBits;
    }

    /**
     * Получить данные узла
     */
    public String getNodeData(int nodeNumber)
    {
        return loudsData.get(nodeNumber);
    }

}