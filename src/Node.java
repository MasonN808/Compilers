import java.sql.Array;
import java.util.ArrayList;
/**
 * <h1>Node Class</h1>
 * We use this Node class to construct our Concrete Syntax Tree in the Trees Class.
 * Part of Parser (Project 2)
 * <p>
 * <b>Note:</b> Still in progress
 *
 * @author  Mason Nakamura
 * @since   03-04-2022
 */
public class Node {
    public Node parent = null; //parent node of current node
    public String name = null; //name of the token_type
    public String value = null; //value of the token_type, if it exists
    public ArrayList<Node> children = new ArrayList<>(); //children of current node
    public boolean traversed = false;

//    public Node(Node parent, String name, String value, ArrayList<Node> children){
//        this.parent = parent;
//        this.name = name;
//        this.value = value;
//        this.children = children;
//    }
}
