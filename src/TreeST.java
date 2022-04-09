import java.util.Hashtable;

public class TreeST {
    public static Node root = null;
    public static Node current = null;
    public static TreeST tree = null;
    public static TreeAST ast = null;

    public TreeST(TreeAST ast){
        this.ast = ast;
        this.root = null;
        this.current = null;
    }

//    public static void addNode(String id, String type, boolean isInitialized, boolean isUsed){
//        Node node = new Node(); //initialize new Node
//        idDetails details = new idDetails(type, isInitialized, isUsed, current.token);
//        Hashtable<String,idDetails> ht = new Hashtable<>();
//        ht.put(id, details);
//
//        if (root == null) { //case for the first node in tree
//            root = node;
//            node.parent = null;
//        } else {
//            node.parent = current; //Assign n's parent to current node
//
//            node.parent.children.add(node); //add n to parent's children array, letting parent know who its children is
//        }
//    }
//    public static void editNode(Node node, String id, String type, boolean isInitialized, boolean isUsed){
//
//    }
//
//    public static void findNode()
//    /**
//     * Populates the symbol tree of the current program using the Abstract Syntax Tree (AST)
//     * @param node populate the symbol tree from this node in AST
//     */
//    public static void populateST(Node node){ //start will be AbstractSyntaxTree.ast.root
//        for (Node each : node.children) {
//            if (each.name.equals("block")){
//                Node tempNode = new Node();
//                if (root == null) { //case for the first node in tree
//                    root = tempNode;
//                    tempNode.parent = null;
//                    current = tempNode;
//                }
//                else{
//                    tempNode.parent = current; //Assign n's parent to current node
//                    tempNode.parent.children.add(tempNode); //add n to parent's children array, letting parent know who its children is
//                }
//                populateST(each);
//            }
//            if (each.name.equals("varDecal")){
//                // TODO: put error exceptions here
//                idDetails details = new idDetails(node.children.get(0).value, false, false, node.token);
//                current.ht.put(each.children.get(1).value, details);
////                addNode(node.children.get(1).value,node.children.get(0).value, false, true);
//                continue; // go to next child ===> don't do recursion on varDecal node
//            }
//            if (each.name.equals("assignmentStatement")){
//                // TODO: put error exceptions here
//                if ()
//                idDetails details = new idDetails(node.children.get(0).value, true, false, node.token);
//                current.ht.put(each.children.get(1).value, details);
//                continue; // go to next child ===> don't do recursion on varDecal node
//            }
//            if (each.name.equals("printStatement")){
//                // TODO: put error exceptions here
//                idDetails details = new idDetails(node.children.get(0).value, true, true, node.token);
//                current.ht.put(each.children.get(1).value, details);
//            }
//            if (each.name.equals("whileStatement") | each.name.equals("ifStatement")){
//
//            }
//            populateST(each);
//        }
//
//
//
//    }

    public static void buildSymbolTable(){
        processNode(ast.root);
    }
}
