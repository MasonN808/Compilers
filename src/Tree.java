
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

    public static void moveUp(){
        current = current.parent; //Go up the tree
    }
}
