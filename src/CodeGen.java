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
    public static ArrayList<JumpEntry> jumps = new ArrayList<>(); // Used to store the jumps for if statements

    public static int numTemps = 0;

    public static int childIndex = 0;

    public static int heapIndex;

    public static int numErrors = 0; // Keep track of errors in code gen

    public static final int OPS_MATRIX_HEIGHT = 32;
    public static final int OPS_MATRIX_WIDTH = 8;

    public static boolean POTfirst = true;

    public static boolean found = false;

    public static int numJumps = 0;



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
        this.heapIndex = opsArray.length;
        this.staticData = new ArrayList<>(); // Used to store the static data table as an arrayList
        this.currentScope = null;
        this.POTfirst = true;
        this.jumps = new ArrayList<>();
        this.numJumps = 0;



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
        // NOTE: ALWAYS increment index AFTER operation

        System.out.println();
        initFalseTrueInHeap(); // set true and false in heap
        processNode(ast.root);

        // Add break at end of code
        OpCode opCode = new OpCode();
        opCode.code = "00";
        opsArray[curIndex] = opCode;
        incrementIndex(1);


        //Back Patching
        for (DataEntry element: staticData){ // Loop through all elements in staticData
            for (int i = 0; i < curIndex; i++){ // Loop through entire code area; note, doesn't go past static or heap
//                System.out.println(opsArray[i].code +" "+ curIndex);
                if (opsArray[i].code.equals(element.temp)){
                    if(Integer.toHexString(curIndex).length() == 1){ // Add a leading 0
                        opsArray[i].code = '0' + Integer.toHexString(curIndex).toUpperCase(); // Replace the temp value with pointer to static memory after code
                    }
                    else{
                        opsArray[i].code = Integer.toHexString(curIndex).toUpperCase(); // Replace the temp value with pointer to static memory after code
                    }
                }
            }
            // For Debugging to see the static variables
            OpCode opCode1 = opCode;
            opCode1.code = "00"; // TODO: Change to 00 when submitting
            opsArray[curIndex] = opCode1;
            incrementIndex(1);
        }

        // Fill the rest
        for (int i = 0; i < opsArray.length; i++)
            if (opsArray[i] == null){
                OpCode opCode1 = new OpCode();
                opCode1.code = "00";
                opsArray[i] = opCode1;
            }

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
                codeGenVarDecal(node);
                break;

            case ("assignmentStatement"):
                codeGenAssignment(node);
                break;

            case ("printStatement"):
                codeGenPrint(node);
                break;

            case ("ifStatement"):
                codeGenIf(node);
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
//        System.out.println(heapIndex);
//        System.out.println(opsArray[heapIndex].code);
        addInHeap("true", heapIndex);
//        System.out.println(heapIndex);
//        System.out.println(opsArray[heapIndex].code);

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
        index -= 1;
        //create break after each string
        OpCode opCode0 = new OpCode();
        opCode0.code = "00";
        opsArray[index] = opCode0;
//        index += 1;

        while (!stack.isEmpty()) { // popping element until
            index -= 1;
            // stack become empty
            // get the character from the top of the stack
            OpCode opCode1 = new OpCode();
            // Transforms from char to hex
            // Uppercase since lower case originally for hex with letters
            opCode1.code = Integer.toHexString((int) stack.pop()).toUpperCase();
            opsArray[index] = opCode1;
//            System.out.println(opCode1.code);
        }
        heapIndex = index; // Reassign heapIndex for next String

    }


    public static void codeGenVarDecal(Node node){
        // TODO: Actually, pull the values from symbol table here ---> if we add the ID names in
        //  DataEntry object for staticData arrayList

        Node type = node.children.get(0);
        Node key = node.children.get(1);

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
        opCode4.code = "00";
        opsArray[curIndex] = opCode4;
        incrementIndex(1);

        // add a data entry to the Static data table to be replace later for backpatching
        DataEntry dataEntry = new DataEntry(opCode3.code, key.value, currentScope.scope, numTemps);
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
            assignedExpr.value = removeFirstandLast(assignedExpr.value); // remove the quotes from the string
            addInHeap(assignedExpr.value, heapIndex); //

            OpCode opCode0 = new OpCode();
            opCode0.code = "A9"; // Load the accumulator with a constant
            opsArray[curIndex] = opCode0;
            incrementIndex(1);

            OpCode opCode1 = new OpCode();
//            System.out.println(heapIndex);
            opCode1.code = Integer.toHexString(heapIndex).toUpperCase(); // Get the location of the string in heap
            opsArray[curIndex] = opCode1;
            incrementIndex(1);
//            System.out.println(opCode1.code);

            OpCode opCode2 = new OpCode();
            opCode2.code = "8D"; // Store the accumulator in memory
            opsArray[curIndex] = opCode2;
            incrementIndex(1);

            // Check for the assigned ID in static table of the current scope to assign temp value--> should be in there
            String temp = null;
            for (DataEntry entry: staticData){
                if (entry.var.equals(assignedID.value)
                        & entry.scope == currentScope.scope){ // Check value is in there and scope are equivalent
                    temp = entry.temp;
                }
            }

            OpCode opCode3 = new OpCode();
            opCode3.code = temp;
            opsArray[curIndex] = opCode3;
            incrementIndex(1);

            OpCode opCode4 = new OpCode();
            opCode4.code = "00";
            opsArray[curIndex] = opCode4;
            incrementIndex(1);
        }
        else if (currentScope.hashTable.get(assignedID.value).type.equals("int")){
//            if (currentScope.hashTable.get(assignedID.value).value.equals("+")){ // Do a post order traversal
                POT(assignedExpr, assignedID);
                POTfirst = true;  //reset pointer

                OpCode opCode5 = new OpCode();
                opCode5.code = "8D"; // Store the accumulator in memory
                opsArray[curIndex] = opCode5;
                incrementIndex(1);

                // Check for the assigned ID in static table of the current scope to assign temp value--> should be in there
                String temp = null;
                for (DataEntry entry: staticData){
                    if (entry.var.equals(assignedID.value)
                            & entry.scope == currentScope.scope){ // Check value is in there and scope are equivalent
                        temp = entry.temp;
                    }
                }

                // Scan the static Data table
                // Check for the assigned ID in static table of the current scope to assign temp value--> should be in there
//                String temp = null;
                ArrayList<DataEntry> validEntries = new ArrayList<>();
                for (DataEntry entry: staticData){
                    if (entry.var.equals(node.value)){
                        validEntries.add(entry);
                    }
                }
                if (validEntries.size() == 1){
                    temp = validEntries.get(0).temp;
                    OpCode opCode6 = new OpCode();
                    opCode6.code = temp;
                    opsArray[curIndex] = opCode6;
                    incrementIndex(1);
                }
                else { // Takes into account multiple ids with same name in different scopes
                    ArrayList<Integer> scopeDifferences = new ArrayList<>();
                    for (DataEntry entry : validEntries) {
                        int difference = 0;
                        TreeST.ScopeNode tempScope = currentScope;
                        while (tempScope != null) {
                            difference += 1;
                            if (entry.scope == tempScope.scope) {
                                scopeDifferences.add(difference);
                                found = true;
                                break;
                            } else {
                                tempScope = tempScope.prev;
                            }
                        }
                        if (!found) {
                            scopeDifferences.add(100);
                        }
                        found = false;
                    }
                    if (!scopeDifferences.isEmpty()) {
                        int minimum = scopeDifferences.get(0);
                        int minIndex = 0;
                        for (int i = 1; i < scopeDifferences.size(); i++) {
                            if (minimum > scopeDifferences.get(i)) {
                                minimum = scopeDifferences.get(i);
                                minIndex = i;
                            }
                        }
                        temp = validEntries.get(minIndex).temp;
                        OpCode opCode6 = new OpCode();
                        opCode6.code = temp;
                        opsArray[curIndex] = opCode6;
                        incrementIndex(1);
                    }
                }

                OpCode opCode6 = new OpCode();
                opCode6.code = temp;
//                System.out.println(temp);
                opsArray[curIndex] = opCode6;
                incrementIndex(1);

                OpCode opCode7 = new OpCode();
                opCode7.code = "00";
                opsArray[curIndex] = opCode7;
                incrementIndex(1);
//            }
        }
//        else{ // Assume it's of boolean type
//
//        }

    }



    public static void codeGenPrint(Node node){
        Node printKey = node.children.get(0);

        OpCode opCode5 = new OpCode();
        opCode5.code = "AC"; // Store the accumulator in memory
        opsArray[curIndex] = opCode5;
        incrementIndex(1);

        // Check for the assigned ID in static table of the current scope to assign temp value--> should be in there
        String temp = null;
        for (DataEntry entry: staticData){
            if (entry.var.equals(printKey.value)
                    & entry.scope == currentScope.scope){ // Check value is in there and scope are equivalent
                temp = entry.temp;
            }
        }



        OpCode opCode6 = new OpCode();
        opCode6.code = temp;
        opsArray[curIndex] = opCode6;
        incrementIndex(1);

        OpCode opCode4 = new OpCode();
        opCode4.code = "00"; // Store the accumulator in memory
        opsArray[curIndex] = opCode4;
        incrementIndex(1);

        OpCode opCode7 = new OpCode();
        opCode7.code = "A2";
        opsArray[curIndex] = opCode7;
        incrementIndex(1);

        if (currentScope.hashTable.get(printKey.value).type.equals("int")){
            OpCode opCode8 = new OpCode();
            opCode8.code = "01";
            opsArray[curIndex] = opCode8;
            incrementIndex(1);
        }
        else if (currentScope.hashTable.get(printKey.value).type.equals("string")){
            OpCode opCode8 = new OpCode();
            opCode8.code = "02";
            opsArray[curIndex] = opCode8;
            incrementIndex(1);
        };


        OpCode opCode9 = new OpCode();
        opCode9.code = "FF";
        opsArray[curIndex] = opCode9;
        incrementIndex(1);

    }

    public static void codeGenIf(Node node){
        Node expr = node.children.get(0);
        numJumps += 1; // for number of jump variables
        // Check what type first child is ==> embedded if statements not supported
        if (expr.children.get(0).name.equals("intExpr")){
            OpCode code = new OpCode();
            code.code = "AC";
            opsArray[curIndex] = code;
            incrementIndex(1);

            code.code = Integer.toHexString(Integer.parseInt(expr.children.get(0).value));
            opsArray[curIndex] = code;
            incrementIndex(1);

            code.code = "A9";
            opsArray[curIndex] = code;
            incrementIndex(1);

            code.code = Integer.toHexString(Integer.parseInt(expr.children.get(1).value));
            opsArray[curIndex] = code;
            incrementIndex(1);

            code.code = "8D";
            opsArray[curIndex] = code;
            incrementIndex(1);

            code.code = "00";
            opsArray[curIndex] = code;
            incrementIndex(1);

            code.code = "00";
            opsArray[curIndex] = code;
            incrementIndex(1);

            code.code = "EC";
            opsArray[curIndex] = code;
            incrementIndex(1);

            code.code = "00";
            opsArray[curIndex] = code;
            incrementIndex(1);

            code.code = "00";
            opsArray[curIndex] = code;
            incrementIndex(1);

            code.code = "A9";
            opsArray[curIndex] = code;
            incrementIndex(1);

            code.code = "00";
            opsArray[curIndex] = code;
            incrementIndex(1);

            code.code = "D0";
            opsArray[curIndex] = code;
            incrementIndex(1);

            code.code = Integer.toHexString(Integer.parseInt(expr.children.get(1).value));
            opsArray[curIndex] = code;
            incrementIndex(1);

            code.code = "A9";
            opsArray[curIndex] = code;
            incrementIndex(1);

            code.code = Integer.toHexString(Integer.parseInt(expr.children.get(0).value));
            opsArray[curIndex] = code;
            incrementIndex(1);

            code.code = "A2";
            opsArray[curIndex] = code;
            incrementIndex(1);

            code.code = "00";
            opsArray[curIndex] = code;
            incrementIndex(1);

            code.code = "8D";
            opsArray[curIndex] = code;
            incrementIndex(1);

            code.code = "00";
            opsArray[curIndex] = code;
            incrementIndex(1);

            code.code = "00";
            opsArray[curIndex] = code;
            incrementIndex(1);

            code.code = "EC";
            opsArray[curIndex] = code;
            incrementIndex(1);

            code.code = "00";
            opsArray[curIndex] = code;
            incrementIndex(1);

            code.code = "00";
            opsArray[curIndex] = code;
            incrementIndex(1);

            code.code = "D0";
            opsArray[curIndex] = code;
            incrementIndex(1);

            code.code = "J" + numJumps;
            opsArray[curIndex] = code;
            incrementIndex(1);

        }else if (expr.children.get(0).name.equals("boolExpr")){

        }else if (expr.children.get(0).name.equals("stringExpr")){

        }
        if (expr.value.equals("==")){
            expr.children.get(0);
        }
        else{

        }

    }

    public static void codeGenWhile(){

    }

//    public static String findMemoryLocation(int index){
//        //Need this for assignment of strings and the pointers to the memory location in heap
//        return Integer.toHexString(index);
//    }

    // Adapted from https://stackoverflow.com/questions/19338009/traversing-a-non-binary-tree-in-java
    public static void POT(Node node, Node id) { // post order traversal
        if (node != null) {
            if (!node.children.isEmpty()) {
                POT(node.children.get(1), id);
                POT(node.children.get(0), id);
            }

            if (node.value.equals("+")){
                OpCode opCode2 = new OpCode();
                opCode2.code = "6D"; // Store the accumulator in memory
                opsArray[curIndex] = opCode2;
                incrementIndex(1);

                OpCode opCode3 = new OpCode();
                opCode3.code = "00";
                opsArray[curIndex] = opCode3;
                incrementIndex(1);

                OpCode opCode4 = new OpCode();
                opCode4.code = "00";
                opsArray[curIndex] = opCode4;
                incrementIndex(1);
//                if (!node.value.equals("+")) {

                OpCode opCode5 = new OpCode();
                opCode5.code = "8D"; // Store the accumulator in memory
                opsArray[curIndex] = opCode5;
                incrementIndex(1);

                OpCode opCode6 = new OpCode();
                opCode6.code = "00";
                opsArray[curIndex] = opCode6;
                incrementIndex(1);

                OpCode opCode7 = new OpCode();
                opCode7.code = "00";
                opsArray[curIndex] = opCode7;
                incrementIndex(1);
//                }
            }
            else{
                OpCode opCode0 = new OpCode();
                opCode0.code = "A9"; // Load the accumulator with a constant
                opsArray[curIndex] = opCode0;
                incrementIndex(1);

                // TODO: scan for ids
                if (node.name.equals("intExpr") & !isNumeric(node.value)){
//                    System.out.println(node.value);
                    // Check for the assigned ID in static table of the current scope to assign temp value--> should be in there
                    String temp = null;
                    ArrayList<DataEntry> validEntries = new ArrayList<>();
                    for (DataEntry entry: staticData){
                        if (entry.var.equals(node.value)){
                            validEntries.add(entry);
                        }
                    }
                    if (validEntries.size() == 1){
                        temp = validEntries.get(0).temp;
                        OpCode opCode6 = new OpCode();
                        opCode6.code = temp;
                        opsArray[curIndex] = opCode6;
                        incrementIndex(1);
                    }
                    else { // Takes into account multiple ids with same name in different scopes
                        ArrayList<Integer> scopeDifferences = new ArrayList<>();
                        for (DataEntry entry : validEntries) {
                            int difference = 0;
                            TreeST.ScopeNode tempScope = currentScope;
                            while (tempScope != null) {
                                difference += 1;
                                if (entry.scope == tempScope.scope) {
                                    scopeDifferences.add(difference);
                                    found = true;
                                    break;
                                } else {
                                    tempScope = tempScope.prev;
                                }
                            }
                            if (!found) {
                                scopeDifferences.add(100);
                            }
                            found = false;
                        }
                        if (!scopeDifferences.isEmpty()) {
                            int minimum = scopeDifferences.get(0);
                            int minIndex = 0;
                            for (int i = 1; i < scopeDifferences.size(); i++) {
                                if (minimum > scopeDifferences.get(i)) {
                                    minimum = scopeDifferences.get(i);
                                    minIndex = i;
                                }
                            }
                            temp = validEntries.get(minIndex).temp;
                            OpCode opCode6 = new OpCode();
                            opCode6.code = temp;
                            opsArray[curIndex] = opCode6;
                            incrementIndex(1);
                        }
                    }
                }
//                else if ()
                else if (node.name.equals("stringExpr")){

                }
                else if (node.name.equals("boolExpr")){

                }
                else{
                    System.out.println(node.value);
                    OpCode opCode1 = new OpCode();
                    if(Integer.toHexString(Integer.parseInt(node.value)).length() == 1){ // Add a leading 0
                        opCode1.code = '0' + Integer.toHexString(Integer.parseInt(node.value)).toUpperCase(); // Replace the temp value with pointer to static memory after code
                    }
                    else{
                        opCode1.code = Integer.toHexString(Integer.parseInt(node.value)).toUpperCase(); // Replace the temp value with pointer to static memory after code
                    }
                    opsArray[curIndex] = opCode1;
                    incrementIndex(1);
                }




                if (POTfirst){
                    OpCode opCode5 = new OpCode();
                    opCode5.code = "8D"; // Store the accumulator in memory
                    opsArray[curIndex] = opCode5;
                    incrementIndex(1);

                    OpCode opCode6 = new OpCode();
                    opCode6.code = "00";
                    opsArray[curIndex] = opCode6;
                    incrementIndex(1);

                    OpCode opCode7 = new OpCode();
                    opCode7.code = "00";
                    opsArray[curIndex] = opCode7;
                    incrementIndex(1);
                    POTfirst = false;
                }


            }
        }


    }

    public static void incrementIndex(int increment){
        curIndex += increment;
    }

    public static void incrementNumTemps(int increment){ //TODO: might just use the length of Static table array instead dynamically
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

    // Used in deleting quotes from string: assignment statement
    public static String removeFirstandLast(String str){
        // Creating a StringBuilder object
        StringBuilder sb = new StringBuilder(str);

        // Removing the last character
        // of a string
        sb.deleteCharAt(str.length() - 1);

        // Removing the first character
        // of a string
        sb.deleteCharAt(0);

        // Converting StringBuilder into a string
        // and return the modified string
        return sb.toString();
    }

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Integer.parseInt(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

}
