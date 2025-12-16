import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.util.*;

/**
 * Тесты для класса BasicLouds
 */
public class BasicLoudsTest {

    private BasicLouds louds;
    //private BasicLouds.TreeNode root;

    @Before
    public void setUp() {
        // Пример дерева:
        //        0
        //      / | \
        //     1  2  3
        //       / \
        //      4   5

        BasicLouds.TreeNode n1 = new BasicLouds.TreeNode(0, null);
        BasicLouds.TreeNode n2 = new BasicLouds.TreeNode(1, null);
        BasicLouds.TreeNode n3 = new BasicLouds.TreeNode(2, null);
        BasicLouds.TreeNode n4 = new BasicLouds.TreeNode(3, null);
        BasicLouds.TreeNode n5 = new BasicLouds.TreeNode(4, null);
        BasicLouds.TreeNode n6 = new BasicLouds.TreeNode(5, null);

        n1.addChild(n2);
        n1.addChild(n3);
        n1.addChild(n4);
        n3.addChild(n5);
        n3.addChild(n6);

        //root = n1;
        louds = new BasicLouds(n1);
    }

    @Test
    public void testLoudsConstruction() {
        // Проверяем основные свойства LOUDS структуры
        assertEquals("Узлов должно быть 6", 6, louds.getNodeCount());
        assertEquals("Длина битовой строки должна быть 13 (2*6+1)", 13, louds.getBitLength());

        // Проверяем строку LOUDS (ожидаемая: 10 1110 10 110 10 10)
        assertNotNull("LOUDS строка не должна быть null", louds.getLoudsString());
        assertFalse("LOUDS строка не должна быть пустой", louds.getLoudsString().isEmpty());
    }

    @Test
    public void testRankSelectOperations() {
        // Тестируем rank/select операции
        assertEquals("rank1(10) должен быть 6", 6, louds.rank1(10));
        assertEquals("select1(4) должен быть 4", 4, louds.select1(4));
        assertEquals("rank0(10) должен быть 5", 5, louds.rank0(10));
        assertEquals("select0(5) должен быть 10", 10, louds.select0(5));
    }

    @Test
    public void testNodeNavigation() {
        // Тестируем навигацию по узлам
        // Узел 0 (корень)
        assertEquals("Родитель корня должен быть -1", -1, louds.parent(0));
        assertEquals("Первый ребёнок узла 0 должен быть 1", 1, louds.firstChild(0));
        assertEquals("Последний ребёнок узла 1 должен быть 3", 3, louds.lastChild(0));
        assertFalse("Узел 1 не должен быть листом", louds.isLeaf(0));
        assertEquals("Степень узла 0 должна быть 3", 3, louds.degree(0));

        // Узел 1 (лист)
        assertEquals("Родитель узла 1 должен быть 0", 0, louds.parent(1));
        assertTrue("Узел 1 должен быть листом", louds.isLeaf(1));
        assertEquals("Степень узла 1 должна быть 0", 0, louds.degree(1));

        // Узел 2
        assertEquals("Родитель узла 2 должен быть 0", 0, louds.parent(2));
        assertEquals("Первый ребёнок узла 2 должен быть 4", 4, louds.firstChild(2));
        assertEquals("Последний ребёнок узла 2 должен быть 5", 5, louds.lastChild(2));
        assertFalse("Узел 2 не должен быть листом", louds.isLeaf(2));
        assertEquals("Степень узла 2 должна быть 2", 2, louds.degree(2));

        // Узел 3 (лист)
        assertEquals("Родитель узла 3 должен быть 0", 0, louds.parent(3));
        assertTrue("Узел 3 должен быть листом", louds.isLeaf(3));
        assertEquals("Степень узла 3 должна быть 0", 0, louds.degree(3));

        // Узел 4 (лист)
        assertEquals("Родитель узла 4 должен быть 2", 2, louds.parent(4));
        assertTrue("Узел 4 должен быть листом", louds.isLeaf(4));
        assertEquals("Степень узла 4 должна быть 0", 0, louds.degree(4));

        // Узел 5 (лист)
        assertEquals("Родитель узла 5 должен быть 2", 2, louds.parent(5));
        assertTrue("Узел 5 должен быть листом", louds.isLeaf(5));
        assertEquals("Степень узла 5 должна быть 0", 0, louds.degree(5));
    }

    @Test
    public void testSiblings() {
        // Тестируем sibling навигацию
        assertEquals("Следующий sibling узла 1 должен быть 2", 2, louds.nextSibling(1));
        assertEquals("Следующий sibling узла 2 должен быть 3", 3, louds.nextSibling(2));
        assertEquals("Следующий sibling узла 3 должен быть -1", -1, louds.nextSibling(3));

        assertEquals("Предыдущий sibling узла 1 должен быть -1", -1, louds.prevSibling(1));
        assertEquals("Предыдущий sibling узла 2 должен быть 1", 1, louds.prevSibling(2));
        assertEquals("Предыдущий sibling узла 3 должен быть 2", 2, louds.prevSibling(3));

        assertEquals("Следующий sibling узла 4 должен быть 5", 5, louds.nextSibling(4));
        assertEquals("Следующий sibling узла 5 должен быть -1", -1, louds.nextSibling(5));
        assertEquals("Предыдущий sibling узла 4 должен быть -1", -1, louds.prevSibling(4));
        assertEquals("Предыдущий sibling узла 5 должен быть 4", 4, louds.prevSibling(5));
    }

    @Test
    public void testChildOperations() {
        // Тестируем операции с детьми
        assertEquals("child(0, 1) должен вернуть 1", 1, louds.child(0, 1));
        assertEquals("child(0, 2) должен вернуть 2", 2, louds.child(0, 2));
        assertEquals("child(0, 3) должен вернуть 3", 3, louds.child(0, 3));

        assertEquals("child(1, 1) должен вернуть -1 (выход за пределы)", -1, louds.child(1, 1));
        assertEquals("child(3, 2) должен вернуть -1 (выход за пределы)", -1, louds.child(3, 2));
        assertEquals("child(2, 3) должен вернуть -1 (выход за пределы)", -1, louds.child(3, 3));

        assertEquals("child(2, 1) должен вернуть 4", 4, louds.child(2, 1));
        assertEquals("child(2, 2) должен вернуть 5", 5, louds.child(2, 2));
    }

    @Test
    public void testChildRank() {
        // Тестируем childRank
        assertEquals("childRank(1) должен быть 1", 1, louds.childRank(1));
        assertEquals("childRank(2) должен быть 2", 2, louds.childRank(2));
        assertEquals("childRank(3) должен быть 3", 3, louds.childRank(3));
        assertEquals("childRank(4) должен быть 1", 1, louds.childRank(4));
        assertEquals("childRank(5) должен быть 2", 2, louds.childRank(5));
    }

    @Test
    public void testEdgeCases() {
        // Тестируем граничные случаи
        assertEquals("rank1(0) должен быть 1", 1, louds.rank1(0));
        assertEquals("rank1(100) должен быть равен количеству единиц", louds.getNodeCount(), louds.rank1(100));
        assertEquals("select1(0) должен быть -1", -1, louds.select1(0));
        assertEquals("select1(100) должен быть -1", -1, louds.select1(100));
        assertEquals("select0(0) должен быть -1", -1, louds.select0(0));
        assertEquals("prevSibling(0) должен быть -1", -1, louds.prevSibling(0));
        assertEquals("child(1, 0) должен быть -1", -1, louds.child(1, 0));
        assertEquals("childRank(0) должен быть -1", -1, louds.childRank(0));
    }
}
