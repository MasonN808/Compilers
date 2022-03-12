
/**
 * <h1>Tree Class</h1>
 * Used to construct our Concrete Syntax Tree (CST) while using Node Class
 * <p>
 * <b>Note:</b> Still in progress
 *
 * @author  Mason Nakamura
 * @since   03-04-2022
 */
public class Tree {
    public static Node root = null;
    public static Node current = null;

    public static void addNode(String kind, String label){
        Node n = new Node(); //initialize new Node
        n.name = label;
        if (root == null){ //case for the first node in tree
            root = n;
        }
        else{
            n.parent = current; //Assign n's parent to current node
            n.parent.children.add(n); //add n to parent's children array, letting parent know who its children is
            //TODO: make sure this is right
        }
        if (!kind.equals("leaf")){
            current = n;
        }
    }

    public static void moveUp() {
        current = current.parent; //Go up the tree
    }


    // Recursive function to handle the expansion of the nodes.
    public static void expand(Node node, int depth, String traversalResult){
        // Space out based on the current depth so
        // this looks at least a little tree-like.
        for (int i = 0; i < depth; i++)
        {
            traversalResult = traversalResult.concat("-");
        }

        // If there are no children (i.e., leaf nodes)...
        if (node.children.isEmpty()) {
            System.out.println("CHILDREN IS EMPTY");
            // ... note the leaf node.
            traversalResult = traversalResult.concat("[" + node.name + "] \n");
        }

        else
        {
            // There are children, so note these interior/branch nodes and ...
            traversalResult = traversalResult.concat("<" + node.name + "> \n");
            // .. recursively expand them.
            for (var i = 0; i < node.children.size(); i++)
            {
                expand(node.children.get(i), depth + 1, traversalResult);
            }
        }
    }

    public static String cstToString() {
        // Initialize the result string.
        var traversalResult = "";
        // Make the initial call to expand from the root.
        expand(root, 0, traversalResult);
        // Return the result.
        return traversalResult;
    }
}
