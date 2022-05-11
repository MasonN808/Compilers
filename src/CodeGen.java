import java.util.*;

public class CodeGen {
    public static String[] opsArray = null; // Might make into matrix
    public static TreeST symbolTable = null;
    public static TreeAST ast = null;
    public static int curIndex = 0;

    public static int numErrors = 0; // Keep track of errors in code gen

    public CodeGen(TreeST symbolTable, TreeAST ast){
        // Reset opsArray to empty string of certain length
        this.opsArray = new String[256]; // TODO: make sure 256 is the right length and not 255
        this.symbolTable = symbolTable; // Might not actually need the symbol table since semantic completed successfully (prereq)
        this.ast = ast;
    }

    public static void generateOpCodes(){
        /*
            -Important Pre-requisites:
                -No redeclared errors
                -....
            -Pseudo Code:
                -Traverse through each node in the AST
                -Assign particular opt code for certain nodes
         */

    }
    public static void varDecal(){

    }
    public static void incrementIndex(int increment){
        curIndex += increment;
    }

    public static String[][] arrayToMatrix(){
        // TODO: make this
        return null;
    }

    public static String[] replaceTemp(){
        // TODO: make this
        return null;
    }

    // Adapted from https://stackoverflow.com/questions/16380026/implementing-bfs-in-java
    public static void BFS(AbstractSyntaxTree tree, Node astRoot) {
        // create a queue for doing BFS
        Queue queue = new LinkedList();
        queue.add(astRoot);
        System.out.println("AST ROOT:" + astRoot.name); //Debugging
        astRoot.visited = true;
        while(!queue.isEmpty()) {
            Node node = (Node)queue.remove();
            Node child = null;
            while((child = getUnvisitedChildNode(node))!=null) {
                child.visited=true;
                System.out.println("AST child:" + child); // Debugging
                queue.add(child);
            }
        }
        // Clear visited property of nodes
         clearNodes(astRoot);
    }

    private static void clearNodes(Node root) { // post order traversal since easy to implement
        root.visited = false; // reset the pointer
        for (Node each : root.children) {
            clearNodes(each);
        }
    }

    private static Node getUnvisitedChildNode(Node node) {
        Deque<Node> stack = new ArrayDeque<Node>();
        for (Node child: node.children){
            if (!child.visited){
                stack.add(child);
            }
        }
        // To prevent null pointer
        Node out = null;
        if (!stack.isEmpty()){
            out = stack.pop(); // TODO: make sure this pops correctly (check order)
        }
        return out;
    }


}
