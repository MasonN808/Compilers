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
        this.jumps = new ArrayList<>();
        this.numJumps = 0;
        this.verbose = verbose;
        this.POTfirstDigit = true;
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
            OpCode opCode = new OpCode();
            opCode.code = "00";
            opsArray[curIndex] = opCode;
            incrementIndex(1);

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

                    // Debugging
//                System.out.println();
//                printScope(currentScope);
                    break;

                case ("varDecal"):
                    Node id = node.children.get(0); // Pulling the id being declared in varDecal Statement
                    if (verbose) {
                        System.out.println("CODE GEN -------> Generating Op Codes for VARIABLE DECLARATION on line " + id.token.line_number);
                    }
                    codeGenVarDecal(node);
                    break;

                case ("assignmentStatement"):
                    id = node.children.get(0); // Pulling the variable being declared in varDecal Statement
                    if (verbose) {
                        System.out.println("CODE GEN -------> Generating Op Codes for ASSIGNMENT STATEMENT on line " + id.token.line_number);
                    }
                    codeGenAssignment(node);
                    break;

                case ("printStatement"):
                    if (verbose) {
                        System.out.println("CODE GEN -------> Generating Op Codes for PRINT STATEMENT on line " + node.token.line_number);
                    }
                    codeGenPrint(node);
                    break;

                case ("ifStatement"):
                    if (verbose) {
                        System.out.println("CODE GEN -------> Generating Op Codes for IF STATEMENT on line " + node.token.line_number);
                    }
                    codeGenIf(node);
                    break;

                case ("whileStatement"):
                    if (verbose) {
                        System.out.println("CODE GEN -------> Generating Op Codes for WHILE STATEMENT on line " + node.token.line_number);
                    }
//                Node expr = node.children.get(0);
                    break;

                default:
                    //Everything else that needs nothing
            }

            // If not known already --> doing a Pseudo-breadth-first traversal
            //                          (pseudo since we go deeper in the tree for every block node)
            for (Node each : node.children) {
                processNode(each);
                if (each.name == "block") {
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

    }

    /**
     * Generates Op Codes for variable declaration
     * @param node The current node in the AST
     */
    public static void codeGenVarDecal(Node node){
        Node type = node.children.get(0);
        Node key = node.children.get(1);

        // initialize the opcode that we'll be using
        OpCode opCode0 = new OpCode();

        opCode0.code = "A9"; // Load the accumulator with a constant
        if (!checkStackOverflow(curIndex, "stack")){
            opsArray[curIndex] = opCode0;
            System.out.println("CODE GEN -------> A9 -------> Load the accumulator with a constant");
            incrementIndex(1);
        }

        OpCode opCode1 = new OpCode();
        opCode1.code = "00"; // Set to NUL
        if (!checkStackOverflow(curIndex, "stack")){
            opsArray[curIndex] = opCode1;
            System.out.println("CODE GEN -------> 00 -------> Break");
            incrementIndex(1);
        }

        OpCode opCode2 = new OpCode();
        opCode2.code = "8D"; // Store the accumulator in memory
        if (!checkStackOverflow(curIndex, "stack")){
            opsArray[curIndex] = opCode2;
            System.out.println("CODE GEN -------> 8D -------> Store the accumulator in memory");
            incrementIndex(1);
        }

        OpCode opCode3 = new OpCode();
        opCode3.code = "T" + numTemps;
        if (!checkStackOverflow(curIndex, "stack")){
            opsArray[curIndex] = opCode3;
            System.out.println("CODE GEN -------> T" + numTemps + " -------> Temporary memory location before backpatching");
            incrementIndex(1);
        }

        OpCode opCode4 = new OpCode();
        opCode4.code = "00";
        if (!checkStackOverflow(curIndex, "stack")){
            opsArray[curIndex] = opCode4;
            System.out.println("CODE GEN -------> 00 -------> Break");
            incrementIndex(1);
        }

        // add a data entry to the Static data table to be replace later for backpatching
        DataEntry dataEntry = new DataEntry(opCode3.code, key.value, currentScope.scope, numTemps);
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
                    String tempAssignedExprValue = removeFirstandLast(assignedExpr.value); // remove the quotes from the string
                    addInHeap(tempAssignedExprValue, heapIndex); // add the string into the heap

                    OpCode opCode0 = new OpCode();
                    opCode0.code = "A9"; // Load the accumulator with a constant
                    if (!checkStackOverflow(curIndex, "stack")) {
                        opsArray[curIndex] = opCode0;
                        System.out.println("CODE GEN -------> A9 -------> Load the accumulator with a constant");
                        incrementIndex(1);
                    }

                    OpCode opCode1 = new OpCode();
                    opCode1.code = Integer.toHexString(heapIndex).toUpperCase(); // Get the location of the string in heap
                    if (!checkStackOverflow(curIndex, "stack")) {
                        opsArray[curIndex] = opCode1;
                        System.out.println("CODE GEN -------> " + Integer.toHexString(heapIndex).toUpperCase() + " -------> Memory Location in Heap");
                        incrementIndex(1);
                    }

                    OpCode opCode2 = new OpCode();
                    opCode2.code = "8D"; // Store the accumulator in memory
                    if (!checkStackOverflow(curIndex, "stack")) {
                        opsArray[curIndex] = opCode2;
                        System.out.println("CODE GEN -------> 8D -------> Store the accumulator in memory");
                        incrementIndex(1);
                    }

                    // Check for the assigned ID in static table of the temporary scope
                    // where the ID was found to assign the temp value
                    String temp = null;
                    for (DataEntry entry : staticData) {
                        if (entry.var.equals(assignedID.value)
                                & entry.scope == tempScope.scope) { // Check value is in there and scope are equivalent
                            temp = entry.temp;
                        }
                    }

                    OpCode opCode3 = new OpCode();
                    opCode3.code = temp;
                    if (!checkStackOverflow(curIndex, "stack")) {
                        opsArray[curIndex] = opCode3;
                        System.out.println("CODE GEN -------> " + temp + " -------> ID Memory Location");
                        incrementIndex(1);
                    }

                    OpCode opCode4 = new OpCode();
                    opCode4.code = "00";
                    if (!checkStackOverflow(curIndex, "stack")) {
                        opsArray[curIndex] = opCode4;
                        System.out.println("CODE GEN -------> 00 -------> Break");
                        incrementIndex(1);
                    }
                }
                else if (tempScope.hashTable.get(assignedID.value).type.equals("int")){ // if the id being assigned is of int type
                    idFound = true; // To get out of the while loop
                    POT(assignedExpr, assignedID); // Doing a depth-first post-order traversal on assigned expression (RHS)
                    POTfirst = true;  //reset pointer

                    OpCode opCode5 = new OpCode();
                    opCode5.code = "8D"; // Store the accumulator in memory
                    if (!checkStackOverflow(curIndex, "stack")) {
                        opsArray[curIndex] = opCode5;
                        System.out.println("CODE GEN -------> 8D -------> Store the accumulator in memory");
                        incrementIndex(1);
                    }

                    // Check for the assigned ID in static table of the temporary scope
                    // where the ID was found to assign the temp value
                    String temp = null;
                    for (DataEntry entry : staticData) {
                        if (entry.var.equals(assignedID.value)
                                & entry.scope == tempScope.scope) { // Check value is in there and scope are equivalent
                            temp = entry.temp;
                        }
                    }

                    OpCode opCode6 = new OpCode();
                    opCode6.code = temp;
                    if (!checkStackOverflow(curIndex, "stack")) {
                        opsArray[curIndex] = opCode6;
                        System.out.println("CODE GEN -------> " + temp + " -------> ID Memory Location");
                        incrementIndex(1);
                    }

                    OpCode opCode7 = new OpCode();
                    opCode7.code = "00"; // Break
                    if (!checkStackOverflow(curIndex, "stack")) {
                        opsArray[curIndex] = opCode7;
                        System.out.println("CODE GEN -------> 00 -------> Break");
                        incrementIndex(1);
                    }
                }

                else if (tempScope.hashTable.get(assignedID.value).type.equals("boolean")) { // if the id being assigned is of boolean type
                    //TODO finish this 9:41 am 5/15 just copied the int expr if block
                    secondPass = false; // Reset the pointer
                    idFound = true; // To get out of the while loop
                    POT(assignedExpr, assignedID); // Doing a depth-first post-order traversal on assigned expression (RHS)
                    POTfirst = true;  //reset pointer

                    OpCode opCode5 = new OpCode();
                    opCode5.code = "8D"; // Store the accumulator in memory
                    if (!checkStackOverflow(curIndex, "stack")) {
                        opsArray[curIndex] = opCode5;
                        System.out.println("CODE GEN -------> 8D -------> Store the accumulator in memory");
                        incrementIndex(1);
                    }

                    // Check for the assigned ID in static table of the temporary scope
                    // where the ID was found to assign the temp value
                    String temp = null;
                    for (DataEntry entry : staticData) {
                        if (entry.var.equals(assignedID.value)
                                & entry.scope == tempScope.scope) { // Check value is in there and scope are equivalent
                            temp = entry.temp;
                        }
                    }

                    OpCode opCode6 = new OpCode();
                    opCode6.code = temp;
                    if (!checkStackOverflow(curIndex, "stack")) {
                        opsArray[curIndex] = opCode6;
                        System.out.println("CODE GEN -------> " + temp + " -------> ID Memory Location");
                        incrementIndex(1);
                    }

                    OpCode opCode7 = new OpCode();
                    opCode7.code = "00"; // Break
                    if (!checkStackOverflow(curIndex, "stack")) {
                        opsArray[curIndex] = opCode7;
                        System.out.println("CODE GEN -------> 00 -------> Break");
                        incrementIndex(1);
                    }
                }

                else{ // if the id being assigned is of boolean type
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

        if (printKey.name.equals("ID")) { // If printing an ID
            OpCode opCode0 = new OpCode();
            opCode0.code = "AC"; // Load the Y register from memory
            if (!checkStackOverflow(curIndex, "stack")) {
                opsArray[curIndex] = opCode0;
                System.out.println("CODE GEN -------> AC -------> Load the Y register from memory");
                incrementIndex(1);
            }

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
                tempScope = tempScope.prev; //go up a scope if nothing found
            }

            OpCode opCode1 = new OpCode();
            opCode1.code = temp;
            if (!checkStackOverflow(curIndex, "stack")) {
                opsArray[curIndex] = opCode1;
                System.out.println("CODE GEN -------> " + temp + " -------> ID Memory Location");
                incrementIndex(1);
            }

            OpCode opCode2 = new OpCode();
            opCode2.code = "00"; // Break
            if (!checkStackOverflow(curIndex, "stack")) {
                opsArray[curIndex] = opCode2;
                System.out.println("CODE GEN -------> 00 -------> Break");
                incrementIndex(1);
            }

            OpCode opCode3 = new OpCode();
            opCode3.code = "A2"; // Load the X register with a constant
            if (!checkStackOverflow(curIndex, "stack")) {
                opsArray[curIndex] = opCode3;
                System.out.println("CODE GEN -------> A2 -------> Load the X register with a constant");
                incrementIndex(1);
            }

            if (currentScope.hashTable.get(printKey.value).type.equals("int")) {
                OpCode opCode4 = new OpCode();
                opCode4.code = "01"; // Print the integer stored in the Y register
                if (!checkStackOverflow(curIndex, "stack")) {
                    opsArray[curIndex] = opCode4;
                    System.out.println("CODE GEN -------> 01 -------> Print the integer stored in the Y register");
                    incrementIndex(1);
                }
            } else if (currentScope.hashTable.get(printKey.value).type.equals("string")
                    | currentScope.hashTable.get(printKey.value).type.equals("boolean")) { // for string and booleans in the heap
                OpCode opCode5 = new OpCode();
                opCode5.code = "02";
                if (!checkStackOverflow(curIndex, "stack")) {
                    opsArray[curIndex] = opCode5;
                    System.out.println("CODE GEN -------> 02 -------> Print the 00-terminated string stored at the address in the Y register");
                    incrementIndex(1);
                }
            }
        }
        else if (printKey.name.equals("stringExpr")){ // if printing a string (not an id of type string)
            OpCode opCode6 = new OpCode();
            opCode6.code = "A0"; // Load the Y register from memory
            if (!checkStackOverflow(curIndex, "stack")) {
                opsArray[curIndex] = opCode6;
                System.out.println("CODE GEN -------> A0 -------> Load the Y register from memory");
                incrementIndex(1);
            }
            String tempPrintKeyValue = removeFirstandLast(printKey.value);
            addInHeap(tempPrintKeyValue, heapIndex); // add the print string into the heap


            OpCode opCode7 = new OpCode();
            opCode7.code = Integer.toHexString(heapIndex).toUpperCase(); // Get the location of the string in the heap
            if (!checkStackOverflow(curIndex, "stack")) {
                opsArray[curIndex] = opCode7;
                System.out.println("CODE GEN -------> " + Integer.toHexString(heapIndex).toUpperCase() + " -------> Memory Location in Heap");
                incrementIndex(1);
            }

            OpCode opCode8 = new OpCode();
            opCode8.code = "A2"; // Load the X register with a constant
            if (!checkStackOverflow(curIndex, "stack")) {
                opsArray[curIndex] = opCode8;
                System.out.println("CODE GEN -------> A2 -------> Load the X register with a constant");
                incrementIndex(1);
            }

            OpCode opCode9 = new OpCode();
            opCode9.code = "02"; // Print the 00-terminated string stored at the address in the Y register
            if (!checkStackOverflow(curIndex, "stack")) {
                opsArray[curIndex] = opCode9;
                System.out.println("CODE GEN -------> 02 -------> Print the 00-terminated string stored at the address in the Y register");
                incrementIndex(1);
            }
        }

        else if (printKey.name.equals("intExpr")){ // for integer Expressions
            POT(printKey, null);

            OpCode opCode0 = new OpCode();
            opCode0.code = "AC"; // Load the Y register from memory
            if (!checkStackOverflow(curIndex, "stack")) {
                opsArray[curIndex] = opCode0;
                System.out.println("CODE GEN -------> AC -------> Load the Y register from memory");
                incrementIndex(1);
            }

            OpCode opCode1 = new OpCode();
            opCode1.code = "00"; // Break
            if (!checkStackOverflow(curIndex, "stack")) {
                opsArray[curIndex] = opCode1;
                System.out.println("CODE GEN -------> 00 -------> Memory Location being loaded into Y register");
                incrementIndex(1);
            }

            OpCode opCode2 = new OpCode();
            opCode2.code = "00"; // Break
            if (!checkStackOverflow(curIndex, "stack")) {
                opsArray[curIndex] = opCode2;
                System.out.println("CODE GEN -------> 00 -------> Break");
                incrementIndex(1);
            }

            OpCode opCode3 = new OpCode();
            opCode3.code = "A2"; // Load the X register with a constant
            if (!checkStackOverflow(curIndex, "stack")) {
                opsArray[curIndex] = opCode3;
                System.out.println("CODE GEN -------> A2 -------> Load the X register with a constant");
                incrementIndex(1);
            }

            OpCode opCode4 = new OpCode();
            opCode4.code = "01"; // Print the integer stored in the Y register
            if (!checkStackOverflow(curIndex, "stack")) {
                opsArray[curIndex] = opCode4;
                System.out.println("CODE GEN -------> 01 -------> Print the integer stored in the Y register");
                incrementIndex(1);
            }
        }
        else if (printKey.name.equals("boolExpr")) {// For boolean expressions
            // TODO: take into account == and != operator later
            //Very similar to the stringExpr if statement, just changing the Temp value to the memory location of boolean
            OpCode opCode6 = new OpCode();
            opCode6.code = "A0"; // Load the Y register from memory
            if (!checkStackOverflow(curIndex, "stack")) {
                opsArray[curIndex] = opCode6;
                System.out.println("CODE GEN -------> A0 -------> Load the Y register from memory");
                incrementIndex(1);
            }
            if (printKey.value.equals("false")) {
                OpCode opCode7 = new OpCode();
                opCode7.code = FALSE_LOCATION; // Get the location of the string in the heap
                if (!checkStackOverflow(curIndex, "stack")) {
                    opsArray[curIndex] = opCode7;
                    System.out.println("CODE GEN -------> " + FALSE_LOCATION + " -------> Memory Location in Heap");
                    incrementIndex(1);
                }
            }
            else if (printKey.value.equals("true")){
                OpCode opCode7 = new OpCode();
                opCode7.code = TRUE_LOCATION; // Get the location of the string in the heap
                if (!checkStackOverflow(curIndex, "stack")) {
                    opsArray[curIndex] = opCode7;
                    System.out.println("CODE GEN -------> " + TRUE_LOCATION + " -------> Memory Location in Heap");
                    incrementIndex(1);
                }
            }

            OpCode opCode8 = new OpCode();
            opCode8.code = "A2"; // Load the X register with a constant
            if (!checkStackOverflow(curIndex, "stack")) {
                opsArray[curIndex] = opCode8;
                System.out.println("CODE GEN -------> A2 -------> Load the X register with a constant");
                incrementIndex(1);
            }

            OpCode opCode9 = new OpCode();
            opCode9.code = "02"; // Print the 00-terminated string stored at the address in the Y register
            if (!checkStackOverflow(curIndex, "stack")) {
                opsArray[curIndex] = opCode9;
                System.out.println("CODE GEN -------> 02 -------> Print the 00-terminated string stored at the address in the Y register");
                incrementIndex(1);
            }

        }



        OpCode opCode6 = new OpCode();
        opCode6.code = "FF"; // System Call
        if (!checkStackOverflow(curIndex, "stack")) {
            opsArray[curIndex] = opCode6;
            System.out.println("CODE GEN -------> FF -------> System Call");
            incrementIndex(1);
        }

    }

    /**
     * Generates Op Codes for if statement
     * @param node The current node in the AST
     */
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

    /**
     * Generates Op Codes for while statement
     * @param node The current node in the AST
     */
    public static void codeGenWhile(Node node){

    }

//    public static String findMemoryLocation(int index){
//        //Need this for assignment of strings and the pointers to the memory location in heap
//        return Integer.toHexString(index);
//    }


    /**
     * Does a depth-first post-order traversal
     * (Mainly for lengthy/embedded int and boolean expressions in the AST).
     * Also allows us to check for IDs in the expressions
     * @param node The current node in the AST
     */
    // Adapted from https://stackoverflow.com/questions/19338009/traversing-a-non-binary-tree-in-java
    public static void POT(Node node, Node id) { // post order traversal
        if (node != null) {
            if (!node.children.isEmpty()) {
                POT(node.children.get(1), id);
                POT(node.children.get(0), id);
            }
            System.out.println(node.value);
            // For digits and Ids
            if (node.name.equals("intExpr") & !isNumeric(node.value) | node.name.equals("ID")) { // Then its an ID in the int expression
                OpCode opCode0 = new OpCode();
                opCode0.code = "AD"; // Load the accumulator from memory
                if (!checkStackOverflow(curIndex, "stack")) {
                    opsArray[curIndex] = opCode0;
                    System.out.println("CODE GEN -------> AD -------> Load the accumulator from memory");
                    incrementIndex(1);
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

                OpCode opCode1 = new OpCode();
                opCode1.code = temp;
                if (!checkStackOverflow(curIndex, "stack")) {
                    opsArray[curIndex] = opCode1;
                    System.out.println("CODE GEN -------> " + temp + " -------> Temporary memory location");
                    incrementIndex(1);
                }
                OpCode opCode2 = new OpCode();
                opCode2.code = "00";
                if (!checkStackOverflow(curIndex, "stack")) {
                    opsArray[curIndex] = opCode2;
                    System.out.println("CODE GEN -------> 00 -------> Break");
                    incrementIndex(1);
                }

                // Now store the accumulator in memory at 00
                OpCode opCode5 = new OpCode();
                opCode5.code = "8D";
                if (!checkStackOverflow(curIndex, "stack")) {
                    opsArray[curIndex] = opCode5;
                    System.out.println("CODE GEN -------> 8D -------> Store the accumulator in memory");
                    incrementIndex(1);
                }
                OpCode opCode6 = new OpCode();
                opCode6.code = "00";
                if (!checkStackOverflow(curIndex, "stack")) {
                    opsArray[curIndex] = opCode6;
                    System.out.println("CODE GEN -------> 00 -------> Memory Location for accumulator");
                    incrementIndex(1);
                }
                OpCode opCode7 = new OpCode();
                opCode7.code = "00";
                if (!checkStackOverflow(curIndex, "stack")) {
                    opsArray[curIndex] = opCode7;
                    System.out.println("CODE GEN -------> 00 -------> Break");
                    incrementIndex(1);
                }
            } else if (node.name.equals("intExpr")) { // for non id intExprs (i.e., + and digit)
                if (node.parent != null) {
                    if (node.parent.name.equals("intOp") & !POTfirstDigit) { // For children of addition operator and not the first digit
                        OpCode opCode0 = new OpCode();
                        opCode0.code = "A9"; // Load the accumulator with a constant
                        if (!checkStackOverflow(curIndex, "stack")) {
                            opsArray[curIndex] = opCode0;
                            System.out.println("CODE GEN -------> 0" + opCode0.code + " -------> Load the accumulator with a constant");
                            incrementIndex(1);
                        }

                        OpCode opCode1 = new OpCode();
                        opCode1.code = "0" + Integer.toHexString(Integer.parseInt(node.value)).toUpperCase(); // Load this constant to accumulator
                        if (!checkStackOverflow(curIndex, "stack")) {
                            opsArray[curIndex] = opCode1;
                            System.out.println("CODE GEN -------> 0" + opCode1.code + " -------> Load this constant to accumulator");
                            incrementIndex(1);
                        }

                        OpCode opCode2 = new OpCode();
                        opCode2.code = "6D"; // Add with carry
                        if (!checkStackOverflow(curIndex, "stack")) {
                            opsArray[curIndex] = opCode2;
                            System.out.println("CODE GEN -------> 6D -------> Add with carry");
                            incrementIndex(1);
                        }

                        OpCode opCode3 = new OpCode();
                        opCode3.code = "00"; //TODO: add a temp value here
                        if (!checkStackOverflow(curIndex, "stack")) {
                            opsArray[curIndex] = opCode3;
                            System.out.println("CODE GEN -------> 00 -------> Adds content in here to accumulator via carry");
                            incrementIndex(1);
                        }


                        OpCode opCode4 = new OpCode();
                        opCode4.code = "00";
                        if (!checkStackOverflow(curIndex, "stack")) {
                            opsArray[curIndex] = opCode4;
                            System.out.println("CODE GEN -------> 00 -------> Break");
                            incrementIndex(1);
                        }

                        OpCode opCode5 = new OpCode();
                        opCode5.code = "8D";
                        if (!checkStackOverflow(curIndex, "stack")) {
                            opsArray[curIndex] = opCode5;
                            System.out.println("CODE GEN -------> 8D -------> Store the accumulator in memory");
                            incrementIndex(1);
                        }
                        OpCode opCode6 = new OpCode();
                        opCode6.code = "00";
                        if (!checkStackOverflow(curIndex, "stack")) {
                            opsArray[curIndex] = opCode6;
                            System.out.println("CODE GEN -------> 00 -------> Memory Location for accumulator");
                            incrementIndex(1);
                        }
                        OpCode opCode7 = new OpCode();
                        opCode7.code = "00";
                        if (!checkStackOverflow(curIndex, "stack")) {
                            opsArray[curIndex] = opCode7;
                            System.out.println("CODE GEN -------> 00 -------> Break");
                            incrementIndex(1);
                        }
                    } else if (node.parent.name.equals("intOp")) { // IS the first digit so store it in memory location
                        OpCode opCode0 = new OpCode();
                        opCode0.code = "A9"; // Load the accumulator with a constant
                        if (!checkStackOverflow(curIndex, "stack")) {
                            opsArray[curIndex] = opCode0;
                            System.out.println("CODE GEN -------> A9 -------> Load the accumulator with a constant");
                            incrementIndex(1);
                        }

                        OpCode opCode1 = new OpCode();
                        opCode1.code = "0" + Integer.toHexString(Integer.parseInt(node.value)).toUpperCase(); // Load this constant to accumulator
                        if (!checkStackOverflow(curIndex, "stack")) {
                            opsArray[curIndex] = opCode1;
                            System.out.println("CODE GEN -------> 0" + opCode1.code + " -------> Load this constant to accumulator");
                            incrementIndex(1);
                        }

                        OpCode opCode2 = new OpCode();
                        opCode2.code = "8D"; // Store the accumulator in memory
                        if (!checkStackOverflow(curIndex, "stack")) {
                            opsArray[curIndex] = opCode2;
                            System.out.println("CODE GEN -------> 8D -------> Store the accumulator in memory");
                            incrementIndex(1);
                        }

                        OpCode opCode3 = new OpCode();
                        opCode3.code = "00"; //TODO: add a temp value here
                        if (!checkStackOverflow(curIndex, "stack")) {
                            opsArray[curIndex] = opCode3;
                            System.out.println("CODE GEN -------> 00 -------> Memory Location for accumulator");
                            incrementIndex(1);
                        }

                        OpCode opCode4 = new OpCode();
                        opCode4.code = "00";
                        if (!checkStackOverflow(curIndex, "stack")) {
                            opsArray[curIndex] = opCode4;
                            System.out.println("CODE GEN -------> 00 -------> Break");
                            incrementIndex(1);
                        }
                        POTfirstDigit = false; // set to false since just traversed the first/deepest digit to store initial memory
                    } else { // catch all the assignments for only digits
                        OpCode opCode0 = new OpCode();
                        opCode0.code = "A9"; // Load the accumulator with a constant
                        if (!checkStackOverflow(curIndex, "stack")) {
                            opsArray[curIndex] = opCode0;
                            System.out.println("CODE GEN -------> A9 -------> Load the accumulator with a constant");
                            incrementIndex(1);
                        }

                        OpCode opCode1 = new OpCode();
                        opCode1.code = "0" + Integer.toHexString(Integer.parseInt(node.value)).toUpperCase(); // Load this constant to accumulator
                        if (!checkStackOverflow(curIndex, "stack")) {
                            opsArray[curIndex] = opCode1;
                            System.out.println("CODE GEN -------> " + opCode1.code + " -------> Load this constant to accumulator");
                            incrementIndex(1);
                        }
                        POTfirstDigit = false; // set to false since just traversed the first/deepest id to store initial memory

                    }

                }
            } else if (node.name.equals("boolExpr")) {
                //TODO finish this
//                System.out.println(node.value);
                if ((node.value.equals("false") | node.value.equals("true")) & (!node.parent.value.equals("==") & !node.parent.value.equals("!="))) {
                    // For simple boolean expressions with no boolean operators

                    // Initialize register
                    OpCode opCode0 = new OpCode();
                    opCode0.code = "A9"; // Load the accumulator with a constant
                    if (!checkStackOverflow(curIndex, "stack")) {
                        opsArray[curIndex] = opCode0;
                        System.out.println("CODE GEN -------> A9 -------> Load the accumulator with a constant");
                        incrementIndex(1);
                    }

                    // Assign the appropriate value to register
                    if (node.value.equals("false")) {
                        OpCode opCode1= new OpCode();
                        opCode1.code = FALSE_LOCATION; // Load the accumulator with a constant
                        if (!checkStackOverflow(curIndex, "stack")) {
                            opsArray[curIndex] = opCode1;
                            System.out.println("CODE GEN -------> " + FALSE_LOCATION + " -------> Memory location of false string in heap");
                            incrementIndex(1);
                        }

                    } else if (node.value.equals("true")) {
                        OpCode opCode1= new OpCode();
                        opCode1.code = TRUE_LOCATION; // Load the accumulator with a constant
                        if (!checkStackOverflow(curIndex, "stack")) {
                            opsArray[curIndex] = opCode1;
                            System.out.println("CODE GEN -------> " + TRUE_LOCATION + " -------> Memory location of false string in heap");
                            incrementIndex(1);
                        }
                    }
                }

                else if (node.parent.value.equals("==") | node.parent.value.equals("!=")){ // If have a boolean expression with boolean operators
                    /*
                     Pseudo Code:
                        1) Load two registers and assign the values from both sides of the operator
                        2) Store one of the registers in memory for comparison using EC (Compare a byte in memory to the X register)
                        3) Load a register (the accumulator) with any boolean value (we choose false)
                        4) Branch n bytes if z flag == 0, so skip the accumulator from being assigned something else
                        5) Store the accumulator in memory at Ti 00
                     */

                    // Check what register to assign value to
                    if (!secondPass) { // second pass tells the program whether to use a different register for comparison,
                        // otherwise the same register will continually be overwritten
                        OpCode opCode0 = new OpCode();
                        opCode0.code = "A2"; // Load the X register with a constant
                        if (!checkStackOverflow(curIndex, "stack")) {
                            opsArray[curIndex] = opCode0;
                            System.out.println("CODE GEN -------> A2 -------> Load the X register with a constant");
                            incrementIndex(1);
                        }
                        secondPass = true;

                        if (node.value.equals("false") | node.value.equals("true")) { // if either side of operator is false or true, a simple case
                            if (node.value.equals("false")) {
                                OpCode opCode1 = new OpCode();
                                opCode1.code = FALSE_LOCATION; // Load the accumulator with a constant
                                if (!checkStackOverflow(curIndex, "stack")) {
                                    opsArray[curIndex] = opCode1;
                                    System.out.println("CODE GEN -------> " + FALSE_LOCATION + " -------> Memory location of false string in heap");
                                    incrementIndex(1);
                                }
                            } else {
                                OpCode opCode1 = new OpCode();
                                opCode1.code = TRUE_LOCATION; // Load the accumulator with a constant
                                if (!checkStackOverflow(curIndex, "stack")) {
                                    opsArray[curIndex] = opCode1;
                                    System.out.println("CODE GEN -------> " + TRUE_LOCATION + " -------> Memory location of false string in heap");
                                    incrementIndex(1);
                                }
                            }
                        }
                        else {
                            // TODO finish this for complex boolean expressions
//                            evalBooleanExpr();
                        }
                    }
                    else {
                        OpCode opCode0 = new OpCode();
                        opCode0.code = "A9"; // Load the accumulator with a constant
                        if (!checkStackOverflow(curIndex, "stack")) {
                            opsArray[curIndex] = opCode0;
                            System.out.println("CODE GEN -------> A9 -------> Load the accumulator with a constant");
                            incrementIndex(1);
                        }

                        if (node.value.equals("false") | node.value.equals("true")) { // if either side of operator is false or true, a simple case
                            if (node.value.equals("false")) {
                                OpCode opCode1 = new OpCode();
                                opCode1.code = FALSE_LOCATION; // Load the accumulator with a constant
                                if (!checkStackOverflow(curIndex, "stack")) {
                                    opsArray[curIndex] = opCode1;
                                    System.out.println("CODE GEN -------> " + FALSE_LOCATION + " -------> Memory location of false string in heap");
                                    incrementIndex(1);
                                }
                            } else {
                                OpCode opCode1 = new OpCode();
                                opCode1.code = TRUE_LOCATION; // Load the accumulator with a constant
                                if (!checkStackOverflow(curIndex, "stack")) {
                                    opsArray[curIndex] = opCode1;
                                    System.out.println("CODE GEN -------> " + TRUE_LOCATION + " -------> Memory location of false string in heap");
                                    incrementIndex(1);
                                }
                            }
                        }
                        else {
                            // TODO finish this for complex boolean expressions
                        }

                        OpCode opCode1 = new OpCode();
                        opCode1.code = "8D"; // Store the accumulator in memory
                        if (!checkStackOverflow(curIndex, "stack")) {
                            opsArray[curIndex] = opCode1;
                            System.out.println("CODE GEN -------> 8D -------> Store the accumulator in memory");
                            incrementIndex(1);
                        }

                        OpCode opCode2 = new OpCode();
                        opCode2.code = "00"; // Store it here
                        if (!checkStackOverflow(curIndex, "stack")) {
                            opsArray[curIndex] = opCode2;
                            System.out.println("CODE GEN -------> 00 -------> Store the accumulator here");
                            incrementIndex(1);
                        }

                        OpCode opCode3 = new OpCode();
                        opCode3.code = "00"; // Break
                        if (!checkStackOverflow(curIndex, "stack")) {
                            opsArray[curIndex] = opCode3;
                            System.out.println("CODE GEN -------> 00 -------> Break");
                            incrementIndex(1);
                        }

                        OpCode opCode4 = new OpCode();
                        opCode4.code = "EC"; // Compare the byte in memory to the X register
                        if (!checkStackOverflow(curIndex, "stack")) {
                            opsArray[curIndex] = opCode4;
                            System.out.println("CODE GEN -------> 00 -------> Compare the byte in memory to the X register");
                            incrementIndex(1);
                        }

                        OpCode opCode5 = new OpCode();
                        opCode5.code = "00"; // Compare from this memory
                        if (!checkStackOverflow(curIndex, "stack")) {
                            opsArray[curIndex] = opCode5;
                            System.out.println("CODE GEN -------> 00 -------> Compare from this memory");
                            incrementIndex(1);
                        }

                        OpCode opCode6 = new OpCode();
                        opCode6.code = "00"; // Break
                        if (!checkStackOverflow(curIndex, "stack")) {
                            opsArray[curIndex] = opCode6;
                            System.out.println("CODE GEN -------> 00 -------> Break");
                            incrementIndex(1);
                        }

                        OpCode opCode7 = new OpCode();
                        opCode7.code = "A9"; // Load the accumulator with a constant
                        if (!checkStackOverflow(curIndex, "stack")) {
                            opsArray[curIndex] = opCode7;
                            System.out.println("CODE GEN -------> A9 -------> Load the accumulator with a constant");
                            incrementIndex(1);
                        }

                        // We use false (but could use true)
                        OpCode opCode8= new OpCode();
                        opCode8.code = FALSE_LOCATION; // Load the accumulator with a constant
                        if (!checkStackOverflow(curIndex, "stack")) {
                            opsArray[curIndex] = opCode8;
                            System.out.println("CODE GEN -------> " + FALSE_LOCATION + " -------> Memory location of false string in heap");
                            incrementIndex(1);
                        }

                        OpCode opCode9= new OpCode();
                        opCode9.code = "D0"; // Branch n bytes if Z flag = 0 from EC op code before
                        if (!checkStackOverflow(curIndex, "stack")) {
                            opsArray[curIndex] = opCode9;
                            System.out.println("CODE GEN -------> D0 -------> Branch n bytes if Z flag = 0");
                            incrementIndex(1);
                        }

                        OpCode opCode10= new OpCode();
                        opCode10.code = "J" + numJumps; // Branch n bytes if Z flag = 0 from EC op code before
                        if (!checkStackOverflow(curIndex, "stack")) {
                            opsArray[curIndex] = opCode10;
                            System.out.println("CODE GEN -------> " + numJumps + " -------> Branch n bytes if Z flag = 0");
                            incrementIndex(1);
                        }


                        OpCode opCode11 = new OpCode();
                        opCode11.code = "A9"; // Load the accumulator with a constant
                        if (!checkStackOverflow(curIndex, "stack")) {
                            opsArray[curIndex] = opCode11;
                            System.out.println("CODE GEN -------> A9 -------> Load the accumulator with a constant");
                            incrementIndex(1);
                        }
                        incrementJumpsValue(1); // increase the jump value

                        // We use false (but could use true)
                        OpCode opCode12= new OpCode();
                        opCode12.code = FALSE_LOCATION; // Load the accumulator with a constant
                        if (!checkStackOverflow(curIndex, "stack")) {
                            opsArray[curIndex] = opCode12;
                            System.out.println("CODE GEN -------> " + FALSE_LOCATION + " -------> Memory location of false string in heap");
                            incrementIndex(1);
                        }
                        incrementJumpsValue(1);


                        // add a data entry to the Jumps data table to be replace later for backpatching
                        JumpEntry jumpEntry = new JumpEntry(opCode10.code, jumpValue);
                        jumps.add(jumpEntry);

                        incrementNumJumps(1); // Go up a temp value for next declaration
                    }

//                    if (node.value.equals("false") | node.value.equals("true")){ // if either side of operator is false or true, a simple case
//                        if (node.value.equals("false")){
//                            OpCode opCode1= new OpCode();
//                            opCode1.code = FALSE_LOCATION; // Load the accumulator with a constant
//                            if (!checkStackOverflow(curIndex, "stack")) {
//                                opsArray[curIndex] = opCode1;
//                                System.out.println("CODE GEN -------> " + FALSE_LOCATION + " -------> Memory location of false string in heap");
//                                incrementIndex(1);
//                            }
//                        }
//                        else {
//                            OpCode opCode1= new OpCode();
//                            opCode1.code = TRUE_LOCATION; // Load the accumulator with a constant
//                            if (!checkStackOverflow(curIndex, "stack")) {
//                                opsArray[curIndex] = opCode1;
//                                System.out.println("CODE GEN -------> " + TRUE_LOCATION + " -------> Memory location of false string in heap");
//                                incrementIndex(1);
//                            }
//                        }
//                    }
//                    else {
//                        // TODO finish this for complex boolean expressions
//                    }
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

}
