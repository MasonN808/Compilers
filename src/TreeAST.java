import java.util.ArrayList;

/**
 * <h1>Tree Class</h1>
 * Used to construct our Concrete Syntax Tree (CST) while using Node Class
 * <p>
 * <b>Note:</b> Still in progress
 *
 * @author  Mason Nakamura
 * @since   03-04-2022
 */
public class TreeAST {
    public static Node root = null;
    public static Node current = null;

    public TreeAST() {
        this.root = null; // make sure root and current are made null for a new instance of Tree
        this.current = null;
    }

    public static void addNode(String kind, String label, Token token) {
        Node n = new Node(); //initialize new Node
        n.name = label;
        n.value = token.s;
        if (root == null) { //case for the first node in tree
            root = n;
            n.parent = null;
        } else {
            //DEBUGGING
//            System.out.println("n: " + n.name);
//            System.out.println("n.parent: " + current.name);
            n.parent = current; //Assign n's parent to current node

            n.parent.children.add(n); //add n to parent's children array, letting parent know who its children is
        }
        if (!kind.equals("leaf")) {
            current = n;
        }
        n.kind = kind;
    }

    public static void addNodeAsStringList(String kind, String label, ArrayList<String> list) {
        Node n = new Node(); //initialize new Node
        n.name = label;
        String listString = String.join(", ", list);
        n.value = listString;
        if (root == null) { //case for the first node in tree
            root = n;
            n.parent = null;
        } else {
            //DEBUGGING
//            System.out.println("n: " + n.name);
//            System.out.println("n.parent: " + current.name);
            n.parent = current; //Assign n's parent to current node

            n.parent.children.add(n); //add n to parent's children array, letting parent know who its children is
        }
        if (!kind.equals("leaf")) {
            current = n;
        }
        n.kind = kind;
    }

    public static void moveUp() {
        current = current.parent; //Go up the tree
    }

    // Adapted from https://stackoverflow.com/questions/19338009/traversing-a-non-binary-tree-in-java
    public static String traverse(Node node, int depth, String traversalResult) { // post order traversal
        for (int i = 0; i < depth; i++) {
            traversalResult = traversalResult.concat("-");
        }
        if (node.kind.equals("leaf")) {
            System.out.println(traversalResult.concat("[" + node.name + ", " + node.value + "]"));
        } else {
            System.out.println(traversalResult.concat("<" + node.name + ">"));
        }
        for (Node each : node.children) {
            traverse(each, depth + 1, traversalResult);
        }
        return traversalResult;
    }
}
