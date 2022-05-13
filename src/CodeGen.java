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

    public static int heapIndex;

    public static int numErrors = 0; // Keep track of errors in code gen

    public static final int OPS_MATRIX_HEIGHT = 32;
    public static final int OPS_MATRIX_WIDTH = 8;


    public CodeGen(TreeST symbolTable, TreeAST ast){
        // Reset opsArray to empty string of certain length
        this.opsArray = new OpCode[256]; // TODO: make sure 256 is the right length and not 255
        this.symbolTable = symbolTable; // Might not actually need the symbol table since semantic completed successfully (prereq)'
//        this.stRoot = null;
        this.ast = ast;
        this.numErrors = 0; // reset the errors
        this.childIndex = 0; // reset the childIndex
        this.numTemps = 0; // reset the numTemps
        this.curIndex = 0; // reset the current index pointer
//        this.lastStackIndex = 0; //TODO: finish this
        this.heapIndex = opsArray.length - 1; // -1, since length is one greater than index

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

        System.out.println();
        initFalseTrueInHeap(); // set true and false in heap
        processNode(ast.root);

        // OpMatrix final output
        printMatrix(arrayToMatrix());
        // Debugging
//        System.out.println(opsArray);
//        printOpsArray();
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
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // Debugging
//                System.out.println();
//                printScope(currentScope);
                break;

            case ("varDecal"):
                Node type = node.children.get(0);
                Node key = node.children.get(1);
                codeGenVarDecal();
                break;

            case ("assignmentStatement"):
                Node assignedID = node.children.get(0);
                Node assignedExpr = node.children.get(1);
                codeGenAssignment(node);
                break;

            case ("printStatement"):
                Node printKey = node.children.get(0);
                break;

            case ("ifStatement"):
                Node expr = node.children.get(0);
                break;

            case ("whileStatement"):
//                Node expr = node.children.get(0);
                break;

            default:
                //Everything else that needs nothing
        }

        for (Node each : node.children) {
            processNode(each);
            if (each.name == "block"){
                //Go to next child
                childIndex += 1;
            }
        }

        if (node.name.equals("block")) {
            // Go back up the tree at outer scope
            currentScope = currentScope.prev;
            childIndex = 0; // reset the child index
        }
    }

    public static void checkStackOverflow(int index){
        if (opsArray[index].code != null){
            System.out.println("CODE GEN [ERROR]: Stack Overflow --> Heap overflowed into stack"); //Stack overflow error
        }
    }

    public static void initFalseTrueInHeap(){
        addInHeap("false", heapIndex);
        addInHeap("true", heapIndex);
    }

    public static void addInHeap(String newString, int index){
        // Create a stack to reverse the string
        Stack<Character> stack = new Stack<Character>();

        // Traverse the String and push the character one by
        // one into the Stack
        for (int i = 0; i < newString.length(); i++) {
            // push the character into the Stack
            stack.push(newString.charAt(i));
        }

        // Now Pop the Characters from the stack until it
        // becomes empty

        //create break after each string
        OpCode opCode0 = new OpCode();
        opCode0.code = "00";
        opsArray[index] = opCode0;
        index -= 1; //reduce index

        int i = 0;
        while (!stack.isEmpty()) { // popping element until
            // stack become empty
            // get the character from the top of the stack
            OpCode opCode1 = new OpCode();
            // Transforms from char to hex
            // Uppercase since lower case originally for hex with letters
            opCode1.code = Integer.toHexString((int) stack.pop()).toUpperCase();

            opsArray[index] = opCode1;
            index -= 1;

        }

        heapIndex = index; // Reassign heapIndex for next String
    }


    public static void codeGenVarDecal(){
        // TODO: Actually, pull the values from symbol table here ---> if we add the ID names in
        //  DataEntry object for staticData arrayList
        OpCode opCode0 = new OpCode();
        opCode0.code = "A9"; // Load the accumulator with a constant
        opsArray[curIndex] = opCode0;
        incrementIndex(1);

        OpCode opCode1 = new OpCode();
        opCode1.code = "00"; // Set to NUL
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
    public static void codeGenAssignment(Node node){
        // TODO: access the symbol table to check the type and get the value
        // TODO: need to fix getting the value from TreeST

        Node assignedID = node.children.get(0);
        Node assignedExpr = node.children.get(1);
        // get certain attributes from key values (i.e., the IDs in certain scopes)
        if (currentScope.hashTable.get(assignedID.value).type.equals("string")){

        }
        else if (currentScope.hashTable.get(assignedID.value).type.equals("int")){

        }
        else{ // Assume it's of boolean type

        }

    }
    public static void codeGenPrint(){

    }

    public static void codeGenIf(){

    }

    public static void codeGenWhile(){

    }

//    public static String findMemoryLocation(int index){
//        //Need this for assignment of strings and the pointers to the memory location in heap
//        return Integer.toHexString(index);
//    }



    public static void incrementIndex(int increment){
        curIndex += increment;
    }

    public static void incrementNumTemps(int increment){
        numTemps += increment;
    }


    // To convert the ops array to a matrix
    // Citation: https://stackoverflow.com/questions/5134555/how-to-convert-a-1d-array-to-2d-array
    public static OpCode[][] arrayToMatrix(){
        OpCode opsMatrix[][] = new OpCode[OPS_MATRIX_HEIGHT][OPS_MATRIX_WIDTH];
        for(int i = 0; i < OPS_MATRIX_HEIGHT; i++){
            for(int j = 0; j < OPS_MATRIX_WIDTH; j++){
                // TODO: REMOVE THIS LATER ---> Just for debugging wiht null values
                if (opsArray[(i*OPS_MATRIX_WIDTH) + j] == null){
                    OpCode opCode = new OpCode();
                    opCode.code = "NULL";
                    opsArray[(i*OPS_MATRIX_WIDTH) + j] = opCode;
                }
                opsMatrix[i][j] = opsArray[(i*OPS_MATRIX_WIDTH) + j];
            }
        }
        return opsMatrix;
    }

    // To print the opsMatrix in matrix form
    public static void printMatrix(OpCode[][] opsMatrix){
        for(int i = 0; i < OPS_MATRIX_HEIGHT; i++){
            for(int j = 0; j < OPS_MATRIX_WIDTH; j++){
                System.out.print(opsMatrix[i][j].code + " ");
            }
            System.out.println();
        }
    }

    // To print the ops array
    public static void printOpsArray(){
        for(int i = 0; i < opsArray.length; i++){
            System.out.print(opsArray[i].code + " ");
        }
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
