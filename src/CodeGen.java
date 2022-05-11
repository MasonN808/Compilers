import javax.xml.crypto.Data;
import java.util.*;

public class CodeGen {
    public static OpCode[] opsArray = null; // Might make into matrix
    public static TreeST symbolTable = null;
    public static TreeAST ast = null;
    public static int curIndex = 0;
    public static TreeST.ScopeNode stRoot = null;
    public static TreeST.ScopeNode currentScope = null;
    public static ArrayList<DataEntry> staticData = new ArrayList<>(); // Used to store the static data table as an arrayList
    public static int numTemps = 0;

    public static int childIndex = 0;

    public static int numErrors = 0; // Keep track of errors in code gen

    public CodeGen(TreeST symbolTable, TreeAST ast){
        // Reset opsArray to empty string of certain length
        this.opsArray = new OpCode[256]; // TODO: make sure 256 is the right length and not 255
        this.symbolTable = symbolTable; // Might not actually need the symbol table since semantic completed successfully (prereq)'
//        this.stRoot = null;
        this.ast = ast;
    }

    public static void generateOpCodes() {
        /*
            -Important Pre-requisites:
                -No redeclared errors
                -....
            -Pseudo Code:
                -Traverse through each node in the AST
                -Assign particular opt code for certain nodes
         */
        processNode(ast.root);
    }

    public static void processNode(Node node) {
        switch (node.name) {
            case ("block"):
                // first block instance
                if (currentScope == null){
                    currentScope = symbolTable.root;
                }
                else{
                    // Get the scope for the current block
                    try{
                        currentScope = currentScope.children.get(childIndex);
//                        childIndex += 1;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                System.out.println();
                printScope(currentScope);
                break;
            case ("varDecal"):
                Node type = node.children.get(0);
                Node key = node.children.get(1);
                codeGenVarDecal();
                break;
            case ("assignmentStatement"):
                Node assignedID = node.children.get(0);
                Node assignedExpr = node.children.get(1);
                break;
            case ("printStatement"):
                Node printKey = node.children.get(0);
                break;

            case ("ifStatement"), ("whileStatement"):
                Node expr = node.children.get(0);
                break;
            default:
                //Everything else that needs nothing
        }

        for (Node each : node.children) {
            processNode(each);
            if (each.name == "block"){
                childIndex += 1;
            }
        }

        if (node.name.equals("block")) {
            // Go back up the tree at outer scope
            currentScope = currentScope.prev;
            childIndex = 0; // reset the child index
        }
    }
    public static void codeGenVarDecal(){
        // TODO: Actually, pull the values from symbol table here ---> if we add the ID names in
        //  DataEntry object for staticData arrayList
        OpCode opCode0 = new OpCode();
        opCode0.code = "A9"; // Load the accumulator with a constant
        opsArray[curIndex] = opCode0;
        incrementIndex(1);

        OpCode opCode1 = new OpCode();
        opCode1.code = "00"; // Set to default?  TODO: what if it's a string?
        opsArray[curIndex] = opCode1;
        incrementIndex(1);

        OpCode opCode2 = new OpCode();
        opCode2.code = "8D"; // Store the accumulator in memory
        opsArray[curIndex] = opCode2;
        incrementIndex(1);

        OpCode opCode3 = new OpCode();
        opCode3.code = "T" + numTemps;
        opsArray[curIndex] = opCode3;
        incrementIndex(1);

        OpCode opCode4 = new OpCode();
        opCode4.code = "XX"; // TODO: Could be set to 00?
        opsArray[curIndex] = opCode4;
        incrementIndex(1);

        // add a data entry to the Static data table to be replace later for backpatching
        DataEntry dataEntry = new DataEntry(opCode3.code, numTemps);
        staticData.add(dataEntry);

        incrementNumTemps(1); // Go up a temp value for next declaration

    }
    public static void codeGenAssignment(){
        // TODO: access the symbol table to check the type and get the value
        // TODO: need to fix getting the value from TreeST
    }
    public static void codeGenPrint(){

    }
    public static void codeGenVarIf(){

    }

    public static String findMemoryLocation(int index){
        //TODO: Need this for assignment of strings and the pointers to the memory location in heap
        return null;
    }



    public static void incrementIndex(int increment){
        curIndex += increment;
    }

    public static void incrementNumTemps(int increment){
        numTemps += increment;
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

    // Mainly for debugging
    // Prints the the data in the current scopeNode
    public static void printScope(TreeST.ScopeNode v){
        Set<String> keys = v.hashTable.keySet();
        for (String key : keys) {
            // Put data in a row to print elegantly in a table format
            String[] row = new String[]{key, v.hashTable.get(key).type, Boolean.toString(v.hashTable.get(key).isInitialized),
                    Boolean.toString(v.hashTable.get(key).isUsed), Integer.toString(v.scope),
                    Integer.toString(v.hashTable.get(key).token.line_number),  (v.hashTable.get(key).value)};
            // TODO: assign values in hashtable for code gen. and debug
            System.out.format("%4s%10s%20s%15s%15s%15s%15s%n", row);
        }
    }

}
