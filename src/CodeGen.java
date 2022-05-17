import javax.xml.crypto.Data;
import java.util.*;

public class CodeGen {
    public static boolean verbose = false;
    public static OpCode[] opsArray = null; // Might make into matrix
    public static TreeST symbolTable = null;
    public static TreeAST ast = null;
    public static int curIndex = 0;
    public static TreeST.ScopeNode stRoot = null;
    public static TreeST.ScopeNode currentScope = null;
    public static ArrayList<DataEntry> staticData = new ArrayList<>(); // Used to store the static data table as an arrayList
    public static ArrayList<JumpEntry> jumps = new ArrayList<>(); // Used to store the jumps for if statements
    public static ArrayList<StringEntry> strings = new ArrayList<StringEntry>();

    public static int numTemps = 0;
    public static int numJumps = 0;

    public static int childIndex = 0;

    public static int heapIndex;

    public static int numErrors = 0; // Keep track of errors in code gen

    public static final int OPS_MATRIX_HEIGHT = 32;
    public static final int OPS_MATRIX_WIDTH = 8;

    public static boolean POTfirst = true;

    public static boolean found = false;

    public static int jumpValue = 0;

    public static boolean POTfirstDigit = true;

    // make constant pointers to false and true in heap in hex
    public static final String FALSE_LOCATION = "FA";
    public static final String TRUE_LOCATION = "F5";

    public static boolean secondPass = false; // Used in POT() for boolean expressions block
    public static boolean inPrint = false;
    public static boolean inIf = false;
    public static boolean inWhile = false;
    public static int startJumpIndex = 0;
    public static int jumpDifference = 0;
    public static String tempJumpVariable = null;
    public static boolean inBoolExpr = false;
    public static boolean setFirstRegister = false;
    public static boolean complexIntExpr = false;
    public static int startWhileIndex = 0;





    public CodeGen(TreeST symbolTable, TreeAST ast, boolean verbose){
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
        this.verbose = verbose;
        this.POTfirstDigit = true;
        this.jumpValue = 0;
        this.numJumps = 0;
        this.jumps = new ArrayList<>();
        this.secondPass = false;
        this.inPrint = false;
        this.inIf = false;
        this.inWhile = false;
        this.startJumpIndex = 0;
        this.jumpDifference = 0;
        this.tempJumpVariable = null;
        this.inBoolExpr = false;
        this.setFirstRegister = false;
        this.strings = new ArrayList<>();
        this.complexIntExpr = false;
        this.startWhileIndex = 0;

    }


    /**
     * Starts the generation of Op Code, does the backpatching, fills the unused memory with 00,
     * and prints the runtime stack as a matrix // TODO: make sure "runtime stack" is corect
     */
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
        if (numErrors > 0){
            System.out.println("------------------------------------------------------------");
            System.out.println("CODE GEN -------> CODE GEN terminated UNSUCCESSFULLY");
        }
        else{
            // Add break at end of code
            addCode("00", "Break");

            if (verbose){
                System.out.println("CODE GEN -------> Beginning Backpatching");
            }

            //Backpatching here
            backpatch();

            if (verbose){
                System.out.println("CODE GEN -------> Ending Backpatching");
            }

            if (verbose){
                System.out.println("CODE GEN -------> Filling Empty Memory with 00");
            }
            // Fill the rest
            for (int i = 0; i < opsArray.length; i++)
                if (opsArray[i] == null){
                    OpCode opCode1 = new OpCode();
                    opCode1.code = "00";
                    opsArray[i] = opCode1;
                }

            if (verbose){
                System.out.println("CODE GEN -------> Done Filling");
            }

            // OpMatrix final output
            if (verbose){
                System.out.println("CODE GEN -------> Outputting Matrix of 256 bit Op Codes");
                System.out.println("------------------------------------------------------------");

            }
            printMatrix(arrayToMatrix());

            System.out.println("------------------------------------------------------------");
            System.out.println("CODE GEN -------> CODE GEN finished SUCCESSFULLY");
        }
    }

    /**
     * Backpatching through jumps and static tables //TODO: implement the backpatching for jump table
     */
    private static void backpatch() {
        for (DataEntry element: staticData){ // Loop through all elements in staticData
            for (int i = 0; i < curIndex; i++){ // Loop through entire code area; note, doesn't go past static or heap
//                System.out.println(opsArray[i].code +" "+ curIndex);
                if (opsArray[i].code.equals(element.temp)){
                    if(Integer.toHexString(curIndex).length() == 1){ // Add a leading 0 for the memory location of the static variable if index < 16
                        opsArray[i].code = '0' + Integer.toHexString(curIndex).toUpperCase(); // Replace the temp value with pointer to static memory after code
                    }
                    else{
                        opsArray[i].code = Integer.toHexString(curIndex).toUpperCase(); // Replace the temp value with pointer to static memory after code
                    }
                    if(Integer.toHexString(i).length() == 1) { // Add a leading 0 for the memory location of the static variable if index < 16
                        System.out.println("CODE GEN -------> Backpatching " + element.temp + " -----> " + opsArray[i].code + " at " +  "0" + Integer.toHexString(i).toUpperCase());
                    }
                    else {
                        System.out.println("CODE GEN -------> Backpatching " + element.temp + " -----> " + opsArray[i].code + " at " + Integer.toHexString(i).toUpperCase());

                    }
                }
            }

            // For Debugging to see the static variables
            OpCode opCode1 = new OpCode();
            opCode1.code = "00";
            opsArray[curIndex] = opCode1;
            incrementIndex(1);
        }
        for (JumpEntry element: jumps){ // Loop through all elements in jumps table
            for (int i = 0; i < curIndex; i++){ // Loop through entire code area; note, doesn't go past static or heap
//                System.out.println(opsArray[i].code +" "+ curIndex);
                if (opsArray[i].code.equals(element.J)){
                    if(Integer.toHexString(element.jump).length() == 1){ // Add a leading 0 for the memory location of the static variable if index < 16
                        opsArray[i].code = '0' + Integer.toHexString(element.jump).toUpperCase(); // Replace the temp value with pointer to static memory after code
                    }
                    else{
                        opsArray[i].code = Integer.toHexString(element.jump).toUpperCase(); // Replace the temp value with pointer to static memory after code
                    }
                    if(Integer.toHexString(i).length() == 1) { // Add a leading 0 for the memory location of the static variable if index < 16
                        System.out.println("CODE GEN -------> Backpatching " + element.J + " -----> " + opsArray[i].code + " at " +  "0" + Integer.toHexString(i).toUpperCase());
                    }
                    else {
                        System.out.println("CODE GEN -------> Backpatching " + element.J + " -----> " + opsArray[i].code + " at " + Integer.toHexString(i).toUpperCase());

                    }
                }
            }

            // For Debugging to see the static variables
            OpCode opCode1 = new OpCode();
            opCode1.code = "00";
            opsArray[curIndex] = opCode1;
            incrementIndex(1);
        }
    }

    /**
     * Processes each node in the AST that are either blocks, varDecals, assignments,
     * if statements, or while statements
     * @param node a node in the AST
     */
    public static void processNode(Node node) {
        if (numErrors == 0) {
            switch (node.name) {
                case ("block"):
                    if (verbose) {
                        System.out.println("CODE GEN -------> Entering block on line " + node.token.line_number);
                    }
                    // first block instance
                    if (currentScope == null) {
                        currentScope = symbolTable.root;
                    } else {
                        // Get the scope for the current block
                        try {
                            currentScope = currentScope.children.get(childIndex);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (verbose) {
                        System.out.println("CODE GEN -------> Current Scope: " + currentScope.scope);
                    }

                    break;

                case ("varDecal"):
                    Node id = node.children.get(0); // Pulling the id being declared in varDecal Statement
                    if (verbose) {
                        System.out.println("CODE GEN -------> Generating Op Codes for VARIABLE DECLARATION on line " + id.token.line_number);
                    }
                    codeGenVarDecal(node);
                    break;

                case ("assignmentStatement"):
                    //reset pot first digit
                    POTfirstDigit = true;
                    id = node.children.get(0); // Pulling the variable being declared in varDecal Statement
                    if (verbose) {
                        System.out.println("CODE GEN -------> Generating Op Codes for ASSIGNMENT STATEMENT on line " + id.token.line_number);
                    }
                    codeGenAssignment(node);
                    inBoolExpr = false;
                    break;

                case ("printStatement"):
                    POTfirstDigit = true;
                    if (verbose) {
                        System.out.println("CODE GEN -------> Generating Op Codes for PRINT STATEMENT on line " + node.token.line_number);
                    }
                    codeGenPrint(node);
                    inBoolExpr = false;

                    break;

                case ("ifStatement"):
                    POTfirstDigit = true;
                    if (verbose) {
                        System.out.println("CODE GEN -------> Generating Op Codes for IF STATEMENT on line " + node.token.line_number);
                    }
                    codeGenIf(node);
                    startJumpIndex = curIndex;
                    inBoolExpr = false;
                    break;

                case ("whileStatement"):
                    POTfirstDigit = true;
                    if (verbose) {
                        System.out.println("CODE GEN -------> Generating Op Codes for WHILE STATEMENT on line " + node.token.line_number);
                    }
                    startWhileIndex = curIndex;
                    codeGenWhile(node);
                    inBoolExpr = false;
                    startJumpIndex = curIndex;

                    break;

                default:
                    //Everything else that needs nothing
            }

            // If not known already --> doing a Pseudo-breadth-first traversal
            //                          (pseudo since we go deeper in the tree for every block node)
            for (Node each : node.children) {
                processNode(each);
                if (each.name.equals("block")) {
                    //Go to next child
                    childIndex += 1;
                }
            }
            if (node.parent != null) {
                if ((node.name.equals("block") & (node.parent.name.equals("ifStatement")) | (node.name.equals("block") & node.parent.name.equals("whileStatement")))) {
//                    System.out.println(startJumpIndex);
                    System.out.println(curIndex + "--" + startJumpIndex);
                    if (node.parent.name.equals("whileStatement")){ // need to add the op codes that involve the loop around
                        jumpDifference = curIndex - startJumpIndex + 12;
                    }
                    else {
                        jumpDifference = curIndex - startJumpIndex;
                    }
                    System.out.println("JUMP DIFFERENCE: " + jumpDifference);
                    // find the number of stuff in the block to see how far to jump ahead
                    JumpEntry jumpEntry = new JumpEntry(tempJumpVariable, jumpDifference); //jump Difference from processNode()
                    jumps.add(jumpEntry);
                    incrementNumJumps(1); // Go up a temp value for next declaration
                }
            }
            if (node.name.equals("whileStatement")){
                generateWhileOpCodes("end");
            }
            if (node.name.equals("block")) {
                // Go back up the tree at outer scope
                currentScope = currentScope.prev;
                childIndex = 0; // reset the child index
            }
        }
    }

    /**
     * Checks to see if the memory space at the particular of the Ops Array is occupied;
     * if so, we output stack overflow error
     * @param index The current index of the Op Array
     * @return boolean
     */
    public static boolean checkStackOverflow(int index, String space){
        if (opsArray[index] != null){
            if (space.equals("heap")){
                System.out.println("CODE GEN [ERROR]: -------> Stack Overflow --> Heap overflowed into Stack"); //Stack overflow error
            }
            else if (space.equals("stack")){
                System.out.println("CODE GEN [ERROR]: -------> Stack Overflow --> Stack overflowed into Heap"); //Stack overflow error
            }
            numErrors += 1;
            return true;
        }
        else{
            return false;
        }
    }

    /**
     * Fills the heap with the strings "true" and "false" for future reference
     * in Boolean statements and expressions
     */
    public static void initFalseTrueInHeap(){
        addInHeap("false", heapIndex);
//        System.out.println(heapIndex);
//        System.out.println(opsArray[heapIndex].code);
        addInHeap("true", heapIndex);
//        System.out.println(heapIndex);
//        System.out.println(opsArray[heapIndex].code);
    }

    /**
     * Adds a string in the heap at the current heap index (index in the heap), and
     * sets current heapIndex to be the first Char of the added string.  Additionally,
     * it checks for stack overflow
     * @param index The current index of the Op Array
     */
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
            if (!checkStackOverflow(index, "heap")){
                opsArray[index] = opCode1;
            }
            else {
                break;
            }
//            System.out.println(opCode1.code);
        }
        heapIndex = index; // Reassign heapIndex for next String

        StringEntry stringEntry = new StringEntry(newString, Integer.toHexString(heapIndex).toUpperCase());
        strings.add(stringEntry); // add a string entry for later referral in boolean expressions and possibly other statements

    }

    /**
     * Generates Op Codes for variable declaration
     * @param node The current node in the AST
     */
    public static void codeGenVarDecal(Node node){
        Node type = node.children.get(0); // the type of the variable
        Node key = node.children.get(1); //variable being declared

        // initialize the register that we'll be using
        addCode("A9", "Load the accumulator with a constant");
        addCode("00", "Break");
        addCode("8D", "Store the accumulator in memory");
        addCode("T" + numTemps, "Temporary memory location before backpatching");
        addCode("00", "Break");

        // add a data entry to the Static data table to be replace later for backpatching
        DataEntry dataEntry = new DataEntry("T" + numTemps, key.value, currentScope.scope, numTemps);
        staticData.add(dataEntry);
        incrementNumTemps(1); // Go up a temp value for next declaration

    }

    /**
     * Generates Op Codes for assignment statement
     * @param node The current node in the AST
     */
    public static void codeGenAssignment(Node node){
        Node assignedID = node.children.get(0);
        Node assignedExpr = node.children.get(1);

        TreeST.ScopeNode tempScope = currentScope;
        boolean idFound = false;
        // Look for the ID in the symbol table
        while (tempScope != null & !idFound){ // Doing a while loop to find the ID from assignedID in some previous scope (or even current scope)
            if (tempScope.hashTable.get(assignedID.value) != null) { // make sure the query from the hashtable isn't null
                if (tempScope.hashTable.get(assignedID.value).type.equals("string")) { // if the id being assigned is of string type
                    idFound = true; // To get out of the while loop

                    addCode("A9", "Load the accumulator with a constant");
                    if (findStringInMemory(assignedExpr.value) == null) { // Implies that the string being assigned is not in heap
                        String tempAssignedExprValue = removeFirstandLast(assignedExpr.value); // remove the quotes from the string
                        addInHeap(tempAssignedExprValue, heapIndex); // add the string into the heap
                        addCode(getLocationInHeap(heapIndex), "Memory Location in Heap"); // Get the location of the string in heap
                    }
                    else{
                        addCode(findStringInMemory(assignedExpr.value), "Memory Location in Heap from previous string generation in heap"); // Get the location of the string in heap
                    }
                    addCode("8D", "Store the accumulator in memory");

                    // Check for the assigned ID in static table of the temporary scope
                    // where the ID was found to assign the temp value
                    // i.e. find the temp value associated with the ID being assigned for memory storage
                    String temp = null;
                    for (DataEntry entry : staticData) {
                        if (entry.var.equals(assignedID.value)
                                & entry.scope == tempScope.scope) { // Check value is in there and scope are equivalent
                            temp = entry.temp;
                        }
                    }

                    addCode(temp, "ID Memory Location");
                    addCode("00", "Break");
                }
                else if (tempScope.hashTable.get(assignedID.value).type.equals("int")){ // if the id being assigned is of int type
                    idFound = true; // To get out of the while loop
                    POT(assignedExpr, assignedID); // Doing a depth-first post-order traversal on assigned expression (RHS)
                    POTfirst = true;  //reset pointer

                    addCode("8D", "Store the accumulator in memory");

                    // Check for the assigned ID in static table of the temporary scope
                    // where the ID was found to assign the temp value
                    // i.e. find the temp value associated with the ID being assigned for memory storage
                    String temp = null;
                    for (DataEntry entry : staticData) {
                        if (entry.var.equals(assignedID.value)
                                & entry.scope == tempScope.scope) { // Check value is in there and scope are equivalent
                            temp = entry.temp;
                        }
                    }

                    addCode(temp, "ID Memory Location");
                    addCode("00", "Break");
                }

                else if (tempScope.hashTable.get(assignedID.value).type.equals("boolean")) { // if the id being assigned is of boolean type
                    secondPass = false; // Reset the pointer
                    idFound = true; // To get out of the while loop
                    POT(assignedExpr, assignedID); // Doing a depth-first post-order traversal on assigned expression (RHS)
                    POTfirst =  true;  //reset pointer

                    addCode("8D", "Store the accumulator in memory");

                    // Check for the assigned ID in static table of the temporary scope
                    // where the ID was found to assign the temp value
                    String temp = null;
                    for (DataEntry entry : staticData) {
                        if (entry.var.equals(assignedID.value)
                                & entry.scope == tempScope.scope) { // Check value is in there and scope are equivalent
                            temp = entry.temp;
                        }
                    }

                    addCode(temp, "ID Memory Location");
                    addCode("00", "Break");
                }

                else{ // if the id being assigned is of boolean type //TODO: get rid of this; probably wont need this
                    idFound = true; // To get out of the while loop
                }
            }
            tempScope = tempScope.prev; // Go up a scope if didn't find it
        }
        numJumps = 0; // reset pointer
    }


    /**
     * Generates Op Codes for print statement
     * @param node The current node in the AST
     */
    public static void codeGenPrint(Node node){
        Node printKey = node.children.get(0);
        inPrint = true;

        if (printKey.name.equals("ID")) { // If printing an ID

            addCode("AC", "Load the Y register from memory");

            // Check for the assigned ID in static table of the current scope to assign temp value--> should be in there
            String temp = null;
            TreeST.ScopeNode tempScope = currentScope;
            boolean idFound = false;
            while (tempScope != null & !idFound) { // Doing a while loop to find the variable entry in static data in some previous scope (or even current scope)
                for (DataEntry entry : staticData) {
                    if (entry.var.equals(printKey.value) & entry.scope == tempScope.scope) { // Check value is in there and scope are equivalent
                        temp = entry.temp;
                        idFound = true;
                    }
                }
                if (!idFound) {
                    tempScope = tempScope.prev; //go up a scope if nothing found
                }
            }

            addCode(temp, "ID Memory Location");
            addCode("00", "Break");
            addCode("A2", "Load the X register with a constant");


            if (tempScope.hashTable.get(printKey.value).type.equals("int")) {
                addCode("01", "Print the integer stored in the Y register");
            } else if (tempScope.hashTable.get(printKey.value).type.equals("string") | tempScope.hashTable.get(printKey.value).type.equals("boolean")) { // for string and booleans in the heap
                addCode("02", "Print the 00-terminated string stored at the address in the Y register");
            }
        }

        else if (printKey.name.equals("stringExpr")){ // if printing a string (not an id of type string)
            addCode("A0", "Load the Y register from memory");

            String tempPrintKeyValue = removeFirstandLast(printKey.value); // remove quotes from string
            addInHeap(tempPrintKeyValue, heapIndex); // add the print string into the heap
            addCode(getLocationInHeap(heapIndex), "Memory location in heap");

            addCode("A2", "Load the X register with a constant");
            addCode("02", "Print the 00-terminated string stored at the address in the Y register");
        }
        else if (printKey.name.equals("intExpr") | printKey.name.equals("intOp")){ // for integer Expressions
            POT(printKey, null);
            addCode("AC", "Load the Y register from memory");
            addCode("00", "Memory Location being loaded into Y register");
            addCode("00", "Break");
            addCode("A2", "Load the X register with a constant");
            addCode("01", "Print the integer stored in the Y register");
        }
        else if (printKey.name.equals("boolExpr") | printKey.name.equals("boolOp")) {// For boolean expressions
            //Very similar to the stringExpr if statement, just changing the Temp value to the memory location of boolean
            if (printKey.value.equals("false") | printKey.value.equals("true")) { // For the simple boolean case with no boolean operators
                addCode("A0", "Load the Y register from memory");

                if (printKey.value.equals("false")) {
                    addCode(FALSE_LOCATION, "Memory Location of false string in Heap");
                } else if (printKey.value.equals("true")) {
                    addCode(TRUE_LOCATION, "Memory Location of true string in Heap");
                }
            }
            else{ // For more complicated boolean expressions with boolean operators and embedded boolean expressions
                QBFS(printKey);
            }
            addCode("A2", "Load the X register with a constant");
            addCode("02", "Print the 00-terminated string stored at the address in the Y register");
        }
        addCode("FF", "System Call");

        // Reset pointer
        inPrint = false;
    }

    /**
     * Generates Op Codes for if statement
     * @param node The current node in the AST
     */
    public static void codeGenIf(Node node){
        inIf = true;
        Node expr = node.children.get(0);
        numJumps += 1; // for number of jump variables
//        POT(expr, null); // Do a post order traversal on the boolean expression to see if we enter the block statement
        QBFS(node);
        inIf = false;

    }

    /**
     * Generates Op Codes for while statement
     * @param node The current node in the AST
     */
    public static void codeGenWhile(Node node){
        inWhile = true;
        numJumps += 1; // for number of jump variables
//        POT(expr, null); // Do a post order traversal on the boolean expression to see if we enter the block statement
        QBFS(node);
        inWhile = false;
    }

    /**
     * Does a depth-first post-order traversal
     * (Mainly for lengthy/embedded int and boolean expressions in the AST).
     * Also allows us to check for IDs in the expressions
     * @param node The current node in the AST
     */
    // Adapted from https://stackoverflow.com/questions/19338009/traversing-a-non-binary-tree-in-java
    public static void POT(Node node, Node id) { // post order traversal
        if (node != null) {
//            System.out.println("POT" + node.name + node.value);
            if (!node.children.isEmpty()) {
                POT(node.children.get(1), id);
                POT(node.children.get(0), id);
            }
//            System.out.println(node.value);
            // For digits and Ids
            if (node.name.equals("ID")) { // Then its an ID in the int expression
                queryMemoryFromID(node, null);
                if (!POTfirstDigit){
                    addCode("6D", "Add with carry");
                    addCode("00", "Adds digit in this memory location to accumulator via carry" );//TODO: may need to add a temp value here for more complex expressions
                    addCode("00", "Break");

                    addCode("8D", "Store the accumulator in memory");
                    addCode("00", "Memory Location for accumulator");
                    addCode("00", "Break");
                }
                else{
                    POTfirstDigit = false;
                }


            } else if (node.name.equals("intExpr")) { // for non id intExprs (i.e., + and digit)
                if (node.parent != null) {
                    if (node.parent.name.equals("intOp") & !POTfirstDigit) { // For children of addition operator and not the first digit
                        addCode("A9", "Load the accumulator with a constant");
                        addCode(digitToHex(Integer.parseInt(node.value)), "Load this integer constant to accumulator");

                        addCode("6D", "Add with carry");
                        addCode("00", "Adds digit in this memory location to accumulator via carry" );//TODO: may need to add a temp value here for more complex expressions
                        addCode("00", "Break");

                        addCode("8D", "Store the accumulator in memory");
                        addCode("00", "Memory Location for accumulator");
                        addCode("00", "Break");
                    } else if (node.parent.name.equals("intOp")) { // IS the first digit so store it in memory location
                        addCode("A9", "Load the accumulator with a constant");
                        addCode(digitToHex(Integer.parseInt(node.value)), "Load this integer constant to accumulator");
                        addCode("8D", "Store the accumulator in memory");
                        addCode("00", "Adds digit in this memory location to accumulator via carry" );//TODO: may need to add a temp value here for more complex expressions
                        addCode("00", "Break");

                        POTfirstDigit = false; // set to false since just traversed the first/deepest digit to store initial memory
                    } else { // catch all the assignments for only digits
                        addCode("A9", "Load the accumulator with a constant");
                        addCode(digitToHex(Integer.parseInt(node.value)), "Load this integer constant to accumulator");
//                        POTfirstDigit = false; // set to false since just traversed the first/deepest id to store initial memory
                    }

                }
            } else if (node.name.equals("boolExpr")) {
                // TODO put QBFS HERE
                if ((node.value.equals("false") | node.value.equals("true")) & (!node.parent.value.equals("==") & !node.parent.value.equals("!="))) {
                    // For simple boolean expressions with no boolean operators
                    addCode("A9", "Load the accumulator with a constant"); // Initialize

                    // Assign the appropriate value to register
                    if (node.value.equals("false")) {
                        addCode(FALSE_LOCATION, "Memory Location of false string in Heap");
                    } else if (node.value.equals("true")) {
                        addCode(TRUE_LOCATION, "Memory Location of true string in Heap");
                    }
                }

                else if (node.parent.value.equals("==") | node.parent.value.equals("!=")){ // If have a boolean expression with boolean operators
                    inBoolExpr = true;
                    /*
                     Pseudo Code:
                        1) Load two registers and assign the values from both sides of the operator
                        2) Store one of the registers in memory for comparison using EC (Compare a byte in memory to the X register)
                        3) Load a register (the accumulator) with any boolean value (we choose false)
                        4) Branch n bytes if z flag == 0, so skip the accumulator from being assigned something else
                        5) Store the accumulator in memory at Ti 00
                     */
                    //TODO: may put QBFS() here

                    // Check what register to assign value to
                    if (!secondPass) { // second pass tells the program whether to use a different register for comparison,
                        // otherwise the same register will continually be overwritten
                        secondPass = true;
                        addCode("A2", "Load the X register with a constant");

                        if (node.value.equals("false")) {
                            addCode(FALSE_LOCATION, "Memory Location of false string in Heap");
                        } else if (node.value.equals("true")) {
                            addCode(TRUE_LOCATION, "Memory Location of true string in Heap");
                        }
                        else{
                            // TODO: for more complex boolean assignments
                        }
                    }
                    else { // This is the second pass so do this : in node with boolOp == and != as parent
                        if (node.value.length() == 1 & Character.isLetter(node.value.charAt(0))){ // For the ID case in boolean expression

                            queryMemoryFromID(node, null); // Generates op codes that finds the static memory for the ID
                            addCode("EC", "Compare the byte in memory to the X register");
                            addCode("00", "Compare from this memory location");
                            addCode("00", "Break");
                        }
                        else {
//                            System.out.println(node.value);
                            addCode("A9", "Load the accumulator with a constant");

                            if (node.value.equals("false")) {
                                addCode(FALSE_LOCATION, "Memory Location of false string in Heap");
                            } else if (node.value.equals("true")) {
                                addCode(TRUE_LOCATION, "Memory Location of true string in Heap");
                            }
                            else{
                                // TODO: for more complex boolean assignments
                            }
                            addCode("8D", "Store the accumulator in memory");
                            addCode("00", "Store the accumulator here");
                            addCode("00", "Break");

                            addCode("EC", "Compare the byte in memory to the X register");
                            addCode("00", "Compare from this memory location");
                            addCode("00", "Break");
                        }

                        if (inPrint){
                            addCode("A0", "Load the Y register with a constant");
                        }
                        else {
                            addCode("A9", "Load the accumulator with a constant");
                        }

                        // We use false for equality operator
                        // This takes into account differing boolean operators
                        if (node.parent.value.equals("==")){
                            addCode(FALSE_LOCATION, "Memory Location of false string in Heap");
                        }
                        else { // for inequality operator
                            addCode(TRUE_LOCATION, "Memory Location of true string in Heap");
                        }

                        addCode("D0", "Branch n bytes if Z flag = 0 (e.g., false)");
                        addCode("J" + numJumps, "Branch n bytes if Z flag = 0");

                        if (inIf | inWhile){
                            //moved to processNode() method since the scope is not relevant here
                            tempJumpVariable = "J" + numJumps;
                        }
                        else {
                            if (inPrint) {
                                addCode("A0", "Load the Y register with a constant");
                            } else {
                                addCode("A9", "Load the accumulator with a constant");
                            }
                            incrementJumpsValue(1); // increase the jump value

                            // We switch them based on operator
                            if (node.parent.value.equals("==")){
                                addCode(TRUE_LOCATION, "Memory Location of true string in Heap");
                            }
                            else { // for inequality operator
                                addCode(FALSE_LOCATION, "Memory Location of false string in Heap");
                            }
                            incrementJumpsValue(1);

                            // add a data entry to the Jumps data table to be replace later for backpatching
                            JumpEntry jumpEntry = new JumpEntry("J" + numJumps, jumpValue);
                            jumps.add(jumpEntry);

                            incrementNumJumps(1); // Go up a temp value for next declaration
                        }
                    }
                }

            } else if (!node.name.equals("intOp") & !node.name.equals("boolOp")){ //Since we do nothing if intOp is traversed
                    System.out.println(node.name + " " + node.value);
                OpCode opCode1 = new OpCode();
                if (Integer.toHexString(Integer.parseInt(node.value)).length() == 1) { // Add a leading 0
                    opCode1.code = Integer.toHexString(Integer.parseInt(node.value)).toUpperCase(); // Replace the temp value with pointer to static memory after code
                } else {
                    opCode1.code = Integer.toHexString(Integer.parseInt(node.value)).toUpperCase(); // Replace the temp value with pointer to static memory after code
                }
                opsArray[curIndex] = opCode1;
                incrementIndex(1);
            }
        }
    }

    /**
     * Does a Quasi-Breadth First in-order traversal for embedded boolean expressions
     * @param node The current node in the AST
     */
    public static void QBFS(Node node){
//        System.out.println("IN QBFS node.name is "+ node.name + " " + node.value);
        /*
            Pseudo Code:
                1) Do QBFS on boolean expression
                2) if found boolean expression, go deeper in the tree
                3) if found int expr go to POT(), else stay in QBFS
         */
        switch (node.name) {
            case ("boolOp"):
                if (verbose) {
                    System.out.println("CODE GEN -------> Entering boolOp on line " + node.token.line_number);
                }

                break;

            case ("ID"):
                if (verbose) {
                    System.out.println("CODE GEN -------> Generating Op Codes for ID in boolean expression");
                }
                if (!setFirstRegister) { // e.g. a == "some string"
                    addCode("AE", "Load the X register from Memory");
                    queryMemoryFromID(node, "bool"); // Generates op code for that static memory of the ID

                }
                else { // e.g. "some string" == a
                    addCode("EC", "Compare the byte in memory to the X register");
                    queryMemoryFromID(node, "bool");

                    if (node.parent.value.equals("!=")){
                        generateInequalityOpCodes();
                    }
                    if(inWhile){
                        generateWhileOpCodes(null);
                    }

                    addCode("D0", "Branch n bytes if Z flag = 0 (e.g., false)");
                    addCode("J" + numJumps, "Branch n bytes if Z flag = 0");

                    if (inIf | inWhile){
                        //moved to processNode() method since the scope is not relevant here
                        tempJumpVariable = "J" + numJumps;
                    }
                }

                setFirstRegister = !setFirstRegister;

                break;

            case ("stringExpr"):
                if (verbose) {
                    System.out.println("CODE GEN -------> Generating Op Codes for string expression in boolean expression");
                }
                if (!setFirstRegister) { // e.g. "some string" == a
                    addCode("A2", "Load the X register with a constant");
                    //Find the string in heap if it exists, else create it
                    if (findStringInMemory(node.value) == null) { // Implies that the string being assigned is not in heap
//                        System.out.println(node.value);
                        String tempAssignedExprValue = removeFirstandLast(node.value); // remove the quotes from the string
                        addInHeap(tempAssignedExprValue, heapIndex); // add the string into the heap
                        addCode(getLocationInHeap(heapIndex), "Memory Location in Heap"); // Get the location of the string in heap
                    }
                    else{
                        addCode(findStringInMemory(node.value), "Memory Location in Heap from previous string generation in heap"); // Get the location of the string in heap
                    }

                }
                else { // e.g. a == "some string"
                    addCode("A9", "Load the accumulator with a constant");

                    if (findStringInMemory(node.value) == null) { // Implies that the string being assigned is not in heap
                        String tempNodeValue = removeFirstandLast(node.value); // remove the quotes from the string
                        addInHeap(tempNodeValue, heapIndex); // add the string into the heap
                        addCode(getLocationInHeap(heapIndex), "Memory Location in Heap"); // Get the location of the string in heap
                    }
                    else{
                        addCode(findStringInMemory(node.value), "Memory Location in Heap from previous string generation in heap"); // Get the location of the string in heap
                    }
                    addCode("8D", "Store the accumulator in memory");
                    addCode("00", "Store the accumulator here");
                    addCode("00", "Break");
                    addCode("EC", "Compare the byte in memory to the X register");
                    addCode("00", "Compare from this memory location");
                    addCode("00", "Break");

                    if (node.parent.value.equals("!=")){
                        generateInequalityOpCodes();
                    }
                    if(inWhile){
                        generateWhileOpCodes(null);
                    }

                    addCode("D0", "Branch n bytes if Z flag = 0 (e.g., false)");
                    addCode("J" + numJumps, "Branch n bytes if Z flag = 0");

                    if (inIf | inWhile){
                        //moved to processNode() method since the scope is not relevant here
                        tempJumpVariable = "J" + numJumps;
                    }
                }

                setFirstRegister = !setFirstRegister;
                break;

            case ("intExpr"): // TODO : how to find inequality operator in this tree

                if (verbose) {
                    System.out.println("CODE GEN -------> Generating Op Codes for int expression in boolean expression");
                }
                if (!setFirstRegister && !complexIntExpr) { // e.g. 3 == a
                    if (node.parent != null && node.parent.name.equals("intOp")){
                        POT(node.parent, null); // for int expressions with intOp use POT method
                        complexIntExpr = true;
                    }
//                    System.out.println("<" + node.name + ", " + node.value + ">");
                    else {
                        addCode("A2", "Load the X register with a constant");
                        // convert the int to hex and load it to register
                        addCode(digitToHex(Integer.parseInt(node.value)), "Constant to be loaded to X register");
                    }

                }
                else if (setFirstRegister){ // e.g. a == 3 // We don't do this if complex integer expression because did in POT
                    if (node.parent != null && !node.parent.name.equals("intOp")) {
                        addCode("A9", "Load the accumulator with a constant");
                        // convert the int to hex and load it to register
                        addCode(digitToHex(Integer.parseInt(node.value)), "Constant to be loaded to X register");
                        addCode("8D", "Store the accumulator in memory");
                        addCode("00", "Store the accumulator here");
                        addCode("00", "Break");
                        addCode("EC", "Compare the byte in memory to the X register");
                        addCode("00", "Compare from this memory location");
                        addCode("00", "Break");

                        if (node.parent.value.equals("!=")){
                            generateInequalityOpCodes();
                        }
                        if(inWhile){
                            generateWhileOpCodes(null);
                        }

                        addCode("D0", "Branch n bytes if Z flag = 0 (e.g., false)");
                        addCode("J" + numJumps, "Branch n bytes if Z flag = 0");

                        if (inIf | inWhile) {
                            tempJumpVariable = "J" + numJumps;
                        }
                    }
                    else if (node.parent != null && node.parent.name.equals("intOp") && !complexIntExpr){
                        POT(node.parent, null);
                        complexIntExpr = true;
                        addCode("EC", "Compare the byte in memory to the X register");
                        addCode("00", "Compare from this memory location");
                        addCode("00", "Break");
//                        System.out.println("node.parent.value " + node.parent.value);

                        if (node.parent.parent != null && node.parent.parent.value.equals("!=")){
                            generateInequalityOpCodes();
                        }
                        if(inWhile){
                            generateWhileOpCodes(null);
                        }

                        addCode("D0", "Branch n bytes if Z flag = 0 (e.g., false)");
                        addCode("J" + numJumps, "Branch n bytes if Z flag = 0");

                        if (inIf | inWhile){
                            tempJumpVariable = "J" + numJumps;
                        }
                    }
                }
                //reset pointers
//                if (complexIntExpr){
//
//                }


//                setFirstRegister = !setFirstRegister;


                break;

            case ("boolExpr"):
                if (verbose) {
                    System.out.println("CODE GEN -------> Generating Op Codes for boolean expression in boolean expression");
                }
                if (!setFirstRegister) { // e.g. false == a
                    addCode("A2", "Load the X register with a constant");
                    // check if false or true to apply correct memory location
                    if (node.value.equals("false")) {
                        addCode(FALSE_LOCATION, "Memory Location of false string in Heap");
                    } else if (node.value.equals("true")) {
                        addCode(TRUE_LOCATION, "Memory Location of true string in Heap");
                    }

                }
                else { // e.g. a == false
                    addCode("A9", "Load the accumulator with a constant");
                    // check if false or true to apply correct memory location
                    if (node.value.equals("false")) {
                        addCode(FALSE_LOCATION, "Memory Location of false string in Heap");
                    } else if (node.value.equals("true")) {
                        addCode(TRUE_LOCATION, "Memory Location of true string in Heap");
                    }
                    addCode("8D", "Store the accumulator in memory");
                    addCode("00", "Store the accumulator here");
                    addCode("00", "Break");
                    addCode("EC", "Compare the byte in memory to the X register");
                    addCode("00", "Compare from this memory location");
                    addCode("00", "Break");

                    if (node.parent.value.equals("!=")){
                        generateInequalityOpCodes();
                    }
                    if(inWhile){
                        generateWhileOpCodes(null);
                    }

                    addCode("D0", "Branch n bytes if Z flag = 0 (e.g., false)");
                    addCode("J" + numJumps, "Branch n bytes if Z flag = 0");

                    if (inIf | inWhile){
                        tempJumpVariable = "J" + numJumps;
                    }

                }

                setFirstRegister = !setFirstRegister;
                break;

            default:
                //Everything else that needs nothing
        }

        // If not known already --> doing a Quasi-breadth-first traversal
        //                          (quasi/pseudo since we go deeper in the tree for every boolOp node)
        for (Node each : node.children) {
//            System.out.println("Visited child");
//            System.out.println("<" + each.name + ", " + each.value + ">");
            if (!each.name.equals("block")) { // don't go in block statement
                QBFS(each);
            }
        }

        if (node.name.equals("intOp")){
//            System.out.println(complexIntExpr);
            if (complexIntExpr){
                setFirstRegister = true;
            }
            else {
                setFirstRegister = !setFirstRegister;
            }

            complexIntExpr = false;
        }

    }

    /**
     * Finds the Memory location of the id being queried
     * @param node The current node in the AST
     */
    public static void queryMemoryFromID(Node node, String where){
        if (where!= null && where.equals("bool")){ // This just finds the memory location and outputs it
            // Check for the assigned ID in static table of the current scope to assign temp value
            String temp = null;
            TreeST.ScopeNode tempScope = currentScope;
            boolean idFound = false;
            while (tempScope != null & !idFound) { // Doing a while loop to find the variable entry in static data in some previous scope (or even current scope)
                for (DataEntry entry : staticData) {
                    if (entry.var.equals(node.value) & entry.scope == tempScope.scope) { // Check value is in there and scope are equivalent
                        temp = entry.temp;
                        idFound = true;
                    }
                }
                tempScope = tempScope.prev; //go up a scope if nothing found
            }
            addCode(temp, "Temporary Memory Location");
            addCode("00", "Break");
        }
        else {
            if (inIf | inWhile) {
                addCode("AE", "Load the X register from memory"); // for comparisons with the x register
            } else {
                addCode("AD", "Load the accumulator from memory");
            }

            // Check for the assigned ID in static table of the current scope to assign temp value
            String temp = null;
            TreeST.ScopeNode tempScope = currentScope;
            boolean idFound = false;
            while (tempScope != null & !idFound) { // Doing a while loop to find the variable entry in static data in some previous scope (or even current scope)
                for (DataEntry entry : staticData) {
                    if (entry.var.equals(node.value) & entry.scope == tempScope.scope) { // Check value is in there and scope are equivalent
                        temp = entry.temp;
                        idFound = true;
                    }
                }
                tempScope = tempScope.prev; //go up a scope if nothing found
            }
            addCode(temp, "Temporary Memory Location");
            addCode("00", "Break");
            addCode("8D", "Store the accumulator in memory");
            addCode("00", "Memory Location for Storage in Accumulator");
            addCode("00", "Break");
        }
    }

    /**
     * Increments the number of temporary jump values
     * @param increment The amount to increment
     */
    private static void incrementNumJumps(int increment) {
        numJumps += increment;
    }

    /**
     * Increments the number of jumps
     * @param increment The amount to increment
     */
    private static void incrementJumpsValue(int increment){
        jumpValue += increment;
    }

    /**
     * Increments the current Index
     * @param increment The amount to increment
     */
    public static void incrementIndex(int increment){
        curIndex += increment;
    }

    /**
     * Increments the number of Temp variables in the static table
     * @param increment The amount to increment
     */
    public static void incrementNumTemps(int increment){ //TODO: might just use the length of Static table array instead dynamically
        numTemps += increment;
    }

    /**
     * Converts the Ops array to a matrix
     * @return OpCode[][] A matrix
     */
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

    /**
     * Prints the Op code Matrix using system print method
     * @param opsMatrix The Op code matrix
     */
    // To print the opsMatrix in matrix form
    public static void printMatrix(OpCode[][] opsMatrix){
        for(int i = 0; i < OPS_MATRIX_HEIGHT; i++){
            for(int j = 0; j < OPS_MATRIX_WIDTH; j++){
                System.out.print(opsMatrix[i][j].code + " ");
            }
            System.out.println();
        }
    }

    /**
     * Prints the Ops Array (mainly for debugging)
     */
    public static void printOpsArray(){
        for(int i = 0; i < opsArray.length; i++){
            System.out.print(opsArray[i].code + " ");
        }
    }

    // TODO: take the backpatching code from before and input here
    public static String[] replaceTemp(){
        // TODO: make this
        return null;
    }

    /**
     * Pseudo Breadth-First Traversal on the AST (might delete later since unneeded)
     * @param tree The AST
     * @param astRoot root of the AST
     */
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

    /**
     * Used in BFS to reset pointers
     * @param root The current node in the AST
     */
    private static void clearNodes(Node root) { // post order traversal since easy to implement
        root.visited = false; // reset the pointer
        for (Node each : root.children) {
            clearNodes(each);
        }
    }

    /**
     * Used in BFS to visit the unvisited children
     * @param node The current node in the AST
     */
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
    /**
     * Prints the the data in the current scopeNode
     * (Mainly for debugging)
     * @param v A scopeNode object
     */
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

    /**
     * Used in deleting quotes from string mainly in assignment statement and print statement
     * @param str A String
     */
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

    /**
     * Checks to see if String is numeric
     * @param strNum a Sting
     * @return boolean
     */
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

    /**
     * Adds an op code to the run-time environment
     * @param codeString the code being added
     * @param details Details of the Opcode
     */
    public static void addCode(String codeString, String details){
        OpCode opCode = new OpCode();
        opCode.code = codeString; // Load the accumulator with a constant
        if (!checkStackOverflow(curIndex, "stack")) {
            opsArray[curIndex] = opCode;
            System.out.println("CODE GEN -------> " + codeString + " -------> " + details);
            incrementIndex(1);
        }
    }

    /**
     * Adds an op code to the run-time environment
     * @param heapIndex the index in the heap
     * @return String the hex location in the heap
     */
    public static String getLocationInHeap(int heapIndex){
        return Integer.toHexString(heapIndex).toUpperCase();
    }

    /**
     * Converts a digit into hex
     * @param digit digit to be transformed
     * @return String the hex code for the digit
     */
    public static String digitToHex(int digit){
        return "0" + Integer.toHexString(digit).toUpperCase();
    }

    /**
     * Searches for string in the arraylist
     * @param target the target string
     * @return memory location or null
     */
    public static String findStringInMemory(String target){
        //remove quotes from target
        target = removeFirstandLast(target);
        for (StringEntry entry: strings){
//            System.out.println(entry.string);
            if (target.equals(entry.string)){
                return entry.memory; // returns the memory location of the target string
            }
        }
        return null; // if didn't find any location
    }

    /**
     * Generates Op codes if inequality
     */
    public static void generateInequalityOpCodes(){
        addCode("A9", "Load the accumulator with a constant");
        addCode("00", "Load it with integer 0 for false");
        addCode("D0", "Branch n bytes if Z flag = 0");
        addCode("02", "branch 2 bytes");
        addCode("A9", "Load the accumulator with a constant");
        addCode("01", "Load it with integer 1 for true");
        addCode("A2", "Load the X register with a constant");
        addCode("00", "Load it with integer 0 for false");
        addCode("8D", "Store the accumulator in memory");
        addCode("00", "Store it here");
        addCode("00", "Break");
        addCode("EC", "Compare a byte in memory to the X reg");
        addCode("00", "memory to be compared withe the X reg");
        addCode("00", "Break");


    }

    /**
     * Generates Op codes if in while loop to negate the equality or inequality
     */
    public static void generateWhileOpCodes(String where){
        if (where != null && where.equals("end")) {
            addCode("A9", "Load the accumulator with a constant");
            addCode("00", "Load it with integer 1 for true");
            addCode("8D", "Store the accumulator in memory");
            addCode("00", "Store it here");
            addCode("00", "Break");
            addCode("A2", "Load the X register with a constant");
            addCode("01", "Load it with integer 1 for true");
            addCode("EC", "Compare a byte in memory to the X reg");
            addCode("00", "memory to be compared withe the X reg");
            addCode("00", "Break");
            addCode("D0", "Branch n bytes if z flag 0");
            addCode(Integer.toHexString(255 - curIndex + startWhileIndex).toUpperCase(), "number of bytes to jump");

        }
        else{
            addCode("A9", "Load the accumulator with a constant");
            addCode("01", "Load it with integer 1 for true");
            addCode("D0", "Branch n bytes if Z flag = 0");
            addCode("02", "branch 2 bytes");
            addCode("A9", "Load the accumulator with a constant");
            addCode("00", "Load it with integer 0 for false");
            addCode("A2", "Load the X register with a constant");
            addCode("00", "Load it with integer 0 for false");
            addCode("8D", "Store the accumulator in memory");
            addCode("00", "Store it here");
            addCode("00", "Break");
            addCode("EC", "Compare a byte in memory to the X reg");
            addCode("00", "memory to be compared withe the X reg");
            addCode("00", "Break");
        }


    }



}
