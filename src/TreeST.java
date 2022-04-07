import java.util.Hashtable;

public class TreeST {
    public static Node root = null;
    public static Node current = null;
    public static AbstractSyntaxTree ast = null;

    public TreeST(AbstractSyntaxTree ast){
        this.ast = ast;
        this.root = null;
        this.current = null;
    }

    public static void addNode(String id, String type, boolean isInitialized, boolean isUsed){
        Node node = new Node(); //initialize new Node
        idDetails details = new idDetails(type, current.token, isInitialized, isUsed);
        Hashtable<String,idDetails> ht = new Hashtable<>();
        idDetails a = ht.put(id, details);

        if (root == null) { //case for the first node in tree
            root = node;
            node.parent = null;
        } else {
            node.parent = current; //Assign n's parent to current node

            node.parent.children.add(node); //add n to parent's children array, letting parent know who its children is
        }

    }

    public static void populateST(Node node){ //start will be AbstractSyntaxTree.ast.root
        if (root == null){
            root = node;
            current = root;
        }
        for (Node each : node.children) {
            if(each.name.equals("varDecal")){
                addNode();
            }
            populateST(each);
        }
    }
}
