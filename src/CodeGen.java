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
        // Putting BFS implementation here ...
//        Queue queue = new LinkedList();
//        queue.add(ast.root);
//        System.out.println("AST ROOT:" + ast.root.name); //Debugging
//        ast.root.visited = true;
//        while(!queue.isEmpty()) {
//            Node node = (Node)queue.remove();
//            Node child = null;
//            while((child = getUnvisitedChildNode(node))!=null) {
//                child.visited=true;
//                System.out.println("AST child:" + child.name); // Debugging
//                queue.add(child);
//            }
//        }
        public static void processNode(Node node) {
//        System.out.println(node.name);
//        System.out.println(currentScope != null);
            switch (node.name) {
                case ("block"):
                    depth = depth + 1; // increase depth of tree // might not need this
                    Hashtable<String, idDetails> hashTable = new Hashtable<>(); // Create hashtable in new scope
                    TreeST.ScopeNode scopeNode = new TreeST.ScopeNode(hashTable); // Create new scope node
                    scopeNode.scope = scopeNum;
                    scopeNum = scopeNum + 1;
                    if (root == null) {
                        root = scopeNode;
                        scopeNode.prev = null;
                        currentScope = scopeNode;
                    } else {
                        currentScope.children.add(scopeNode); // Create the children for current scope/parent scope
                        scopeNode.prev = currentScope; // set the previous scope to outer scope
                    }
                    currentScope = scopeNode; // reinitialize current scope
                    break;
                case ("varDecal"):
                    Node type = node.children.get(0);
                    Node key = node.children.get(1);
//                System.out.println(type.value);
                    if (currentScope.hashTable.get(key.value) == null) {
                        idDetails details = new idDetails(type.value, false, false, key.token);
                        currentScope.hashTable.put(key.value, details);
                    } else {
                        // CASE: redeclared identifier in same scope
                        System.out.println("SEMANTIC ANALYSIS [ERROR]: -------> Identifier [" + key.value + "] redeclared error at line " +
                                key.token.line_number + ", char " + key.token.character_number);
                        numErrors = numErrors + 1;
                    }
//                System.out.println("DEBUG: " + currentScope.hashTable.get(key.value).type);
                    break;
                case ("assignmentStatement"):
                    //reset Pointer
                    assignedExprTraverse = null;
                    Node assignedID = node.children.get(0);
                    Node assignedExpr = node.children.get(1);
                /*
                    -Pseudo Code
                        - First search for IDs in the boolean expression that may be labelled as mixed
                        - Then search for IDs labelled as IDs and make sure of declarations
                        - Then reassign the IDs as either intExpression, stringExpression, or booleanExpression
                        - Then typeCheck in the boolean expression (locally, unlike in AbstractSyntaxTree.java)
                 */


                    // Search for IDs in the boolean expression that may be labelled as mixed

//                ArrayList<Node> mixedIdNodes = new ArrayList<>();

                    //COMMENTED OUT SINCE GOT RID OF MIXED
//                traverseFind(assignedExpr, mixedIdNodes, "mixedExpr"); // Find nodes labelled as mixedExpr and append to ArrayList()
////                for (Node i: mixedIdNodes){
////                    System.out.println(i.value);
////                }
//                ScopeNode tempCurrentScope = currentScope;
//                for (Node mixedIdNode: mixedIdNodes){ // This only catches IDs that are in some scope; if undeclared, will output undeclared ERROR
//                    boolean foundID = false;
//                    if (tempCurrentScope.hashTable.get(mixedIdNode.value) == null){ //If can't find ID in current scope
//                        // Check if previous (outer) scope declared the variable being assigned and keep going to outer scope until no scopes left
//                        while (tempCurrentScope != null & !foundID) {
//                            if (tempCurrentScope.hashTable.get(mixedIdNode.value) != null) {
//                                foundID = true; // Found key in a different scope (we use this key for assignment
//                            } else {
//                                // Go up a scope
//                                tempCurrentScope = tempCurrentScope.prev;
//                            }
//                        }
//                        if (!foundID) {
//                            mixedIdNodes.remove(mixedIdNode); // remove the nodes with mixedExpr as their name since not IDs
//                        }
//                    }
//                }

                    // We should get an ArrayList of just IdNodes with mixedExpr as their names here but are actually IDs declared in some valid scope

                    // First search for IDs in the int expression
                    ArrayList<Node> idNodes = new ArrayList<>();
                    traverseFind(assignedExpr, idNodes, "ID"); //Finds all nodes with name=="ID" and appends to an ArrayList()

                    // Check if all IDs in idNodes are in current scope or outer scope; else, output undeclared ERROR
                    boolean foundUndeclared = false;
                    TreeST.ScopeNode tempCurrentScope = currentScope;
                    for (Node i: idNodes){
                        boolean foundID = false;
                        if (tempCurrentScope.hashTable.get(i.value) == null){ //If can't find ID in current scope
                            // Check if previous (outer) scope declared the variable being assigned and keep going to outer scope until no scopes left
                            while (tempCurrentScope != null & !foundID) {
                                if (tempCurrentScope.hashTable.get(i.value) != null) {
                                    foundID = true; // Found key in a different scope (we use this key for assignment
                                } else {
                                    // Go up a scope
                                    tempCurrentScope = tempCurrentScope.prev;
                                }
                            }
                            if (!foundID) {
                                foundUndeclared = true;
                                System.out.println("SEMANTIC ANALYSIS [ERROR]: -------> Undeclared Identifier [" + i.value + "] at line " +
                                        i.token.line_number + ", char " + i.token.character_number);
                                numErrors = numErrors + 1;
                            }
                        }
                    }
                    if (!foundUndeclared){
                        tempCurrentScope = currentScope;
                        // Rename each idNode with respect to how they're declared
                        for (Node idNode: idNodes){ // We can get rid of foundID since we know its in a scope
                            // Check if current/previous (outer) scope declared the variable being assigned and keep going to outer scope until no scopes left
                            boolean foundId = false;
                            while (tempCurrentScope != null & !foundId) {
                                if (tempCurrentScope.hashTable.get(idNode.value) != null) {
                                    foundId = true;
                                    String declaredType = tempCurrentScope.hashTable.get(idNode.value).type;
                                    switch (declaredType){ // Redefine their names with respect to what type the ID was declared as
                                        case ("int"):
                                            idNode.name = "intExpr";
                                            break;
                                        case ("string"):
                                            idNode.name = "stringExpr";
                                            break;
                                        case ("boolean"):
                                            idNode.name = "boolExpr";
                                            break;
                                    }
                                } else {
                                    // Go up a scope
                                    tempCurrentScope = tempCurrentScope.prev;
                                }
                            }
                        }

//                    // Change all boolOps to boolExpr
//                    ArrayList<Node> boolOps = new ArrayList<>();
//                    traverseFind(assignedExpr, boolOps, "boolOp");
//                    for (Node boolOp: boolOps){
//                        boolOp.name = "boolExpr";
//                    }
//
//                    // Change all intOps to intExpr
//                    ArrayList<Node> intOps = new ArrayList<>();
//                    traverseFind(assignedExpr, intOps, "intOp");
//                    for (Node boolOp: boolOps){
//                        boolOp.name = "intExpr";
//                    }

                        Node testMixed = postOrderFindIsMixed(assignedExpr, "", null);

//                    for (Node idNode: idNodes){
//                        System.out.println(idNode.name);
//                    }
                        // At this point, we should have a subtree with no ID names

                        // Do a pseudo parse using depth-first post-order traversal for the branch to see if anymore mixed Expressions
                        // If found a Node, output type mismatch
                        if (testMixed != null){
                            if (testMixed.name.equals("ID")){ // In the case of an undeclared identifier
                                System.out.println("SEMANTIC ANALYSIS [ERROR]: -------> Type Mismatch in assignment statement at " +
                                        testMixed.token.line_number + ", char " + testMixed.token.character_number + ". Didn't expect an undeclared identifier");
                                numErrors = numErrors + 1;
                            }
                            else {
//                        System.out.println("SEMANTIC ANALYSIS [ERROR]: -------> Type Mismatch in assignment statement at " +
//                                testMixed.token.line_number + ", char " + testMixed.token.character_number + ". Didn't expect [" + testMixed.name + "]");
                                System.out.println("SEMANTIC ANALYSIS [ERROR]: -------> Type Mismatch in assignment statement at " +
                                        testMixed.token.line_number + ", char " + testMixed.token.character_number);
//                            System.out.println(testMixed.name);
                                numErrors = numErrors + 1;
                            }
                        }
                    }

                    // If we got here, the assigned right side/value is all of one type
                    // Now, we check that what the ID is being assigned to is type compatible and say its been initialized
//                System.out.println(assignedExprTraverse.name);
                    Node testMixed = postOrderFindIsMixed(assignedExpr, "", null); //TODO: set a pointer so dont have to do another post Order after the switch statement
//                System.out.println(assignedExprTraverse.name);

                    if (testMixed == null){ // testMixed==null implies that the right side is all of one type
                        // See if the ID is in some scope
                        boolean foundKey = false;
                        tempCurrentScope = currentScope;
                        if (tempCurrentScope.hashTable.get(assignedID.value) == null) { // if identifier is undeclared in current scope try an outer scope
                            while (tempCurrentScope != null & !foundKey) {
                                if (tempCurrentScope.hashTable.get(assignedID.value) != null) {
                                    //                            tempCurrentScope = tempCurrentScope.prev; //redefine tempCurrentScope to be used later
                                    foundKey = true; // Found key in a different scope (we use this key for assignment
                                } else {
                                    tempCurrentScope = tempCurrentScope.prev;
                                }
                                // Check if previous (outer) scope declared the variable being assigned and keep going to outer scope until no scopes left
                            }
                            if (!foundKey) {
                                if (assignedID.name.equals("ID")){
                                    System.out.println("SEMANTIC ANALYSIS [ERROR]: -------> Undeclared Identifier [" + assignedID.value + "] at line " +
                                            assignedID.token.line_number + ", char " + assignedID.token.character_number);
                                    numErrors = numErrors + 1;
                                }
                            }
                        } else {
                            foundKey = true;
                        }

                        if (foundKey){
                            if (!checkAssignmentTypesExpr(tempCurrentScope.hashTable.get(assignedID.value).type, assignedExprTraverse)){
//                            System.out.println(tempCurrentScope.hashTable.get(assignedID.value).type);
//                            System.out.println(assignedExprTraverse.value);
//                            System.out.println(tempCurrentScope.hashTable.get(assignedID.value).token.s);
                                System.out.println("SEMANTIC ANALYSIS [ERROR]: -------> Type Mismatch: Expected [" + tempCurrentScope.hashTable.get(assignedID.value).type + "] at " +
                                        tempCurrentScope.hashTable.get(assignedID.value).token.line_number + ", char " + tempCurrentScope.hashTable.get(assignedID.value).token.character_number);
                                numErrors = numErrors + 1;
                            }
                            else { // key found
                                // First get original details from hashtable of key
                                // tempCurrentScope is the scope the key value is in
                                String wasType = tempCurrentScope.hashTable.get(assignedID.value).type;
                                boolean wasInitialized = tempCurrentScope.hashTable.get(assignedID.value).isInitialized;
                                boolean wasUsed = tempCurrentScope.hashTable.get(assignedID.value).isUsed;
                                Token wasToken = tempCurrentScope.hashTable.get(assignedID.value).token;
                                // Then assign accordingly
                                if (wasInitialized == false) { // mark key as is Initialized and keep wasUsed the same
                                    idDetails details = new idDetails(wasType, true, wasUsed, wasToken);
                                    tempCurrentScope.hashTable.put(assignedID.value, details); // Remake the hashvalue with edits to idDetails
                                }
                            }
                        }
                    }





//                Node assignedKey = node.children.get(0);
//                Node assignedValue = node.children.get(1);
////                System.out.println("DEBUG: " + assignedValue.name);
////                System.out.println("DEBUG: " + tempCurrentScope.hashTable.get(assignedKey.value).type);
//                boolean foundKey = false;
//                ScopeNode tempCurrentScope = currentScope;
//                //Traverse subtree to find mixed expressions or type mismatch
//                Node isMixed = traverseFind(assignedValue, null); //set the isMixed Node to null
//                if (isMixed != null){ // the assigned value is of mixed types
//                    System.out.println("SEMANTIC ANALYSIS [ERROR]: -------> Type Mismatch in assigned expression at " +
//                            isMixed.token.line_number + ", char " + isMixed.token.character_number);
//                    numErrors = numErrors + 1;
//                }
//                if (tempCurrentScope.hashTable.get(assignedKey.value) == null) { // if identifier is undeclared in current scope try an outer scope
//                    while (tempCurrentScope != null & !foundKey) {
//                        if (tempCurrentScope.hashTable.get(assignedKey.value) != null) {
////                            tempCurrentScope = tempCurrentScope.prev; //redefine tempCurrentScope to be used later
//                            foundKey = true; // Found key in a different scope (we use this key for assignment
//                        } else {
//                            tempCurrentScope = tempCurrentScope.prev;
//                        }
//                        // Check if previous (outer) scope declared the variable being assigned and keep going to outer scope until no scopes left
//                    }
//                    if (!foundKey) {
//                        if (assignedKey.name.equals("ID")){
//                            System.out.println("SEMANTIC ANALYSIS [ERROR]: -------> Undeclared Identifier [" + assignedKey.value + "] at line " +
//                                    assignedKey.token.line_number + ", char " + assignedKey.token.character_number);
//                            numErrors = numErrors + 1;
//                        }
//                    }
//                } else {
//                    foundKey = true;
//                }
//                // Debugging checking assignment mismatch
////                System.out.println("DEBUG: " + tempCurrentScope.hashTable.get(assignedKey.value).type);
////                System.out.println("DEBUG: " + assignedValue.value);
//                if (foundKey){
//                    if (!checkAssignmentTypes(tempCurrentScope.hashTable.get(assignedKey.value).type, assignedValue.value)){
//                        System.out.println("SEMANTIC ANALYSIS [ERROR]: -------> Type Mismatch: Expected [" + tempCurrentScope.hashTable.get(assignedKey.value).type + "] at " +
//                                assignedValue.token.line_number + ", char " + assignedValue.token.character_number);
//                        numErrors = numErrors + 1;
//                    }
//                    else { // key found
//                        // First get original details from hashtable of key
//                        // tempCurrentScope is the scope the key value is in
//                        String wasType = tempCurrentScope.hashTable.get(assignedKey.value).type;
//                        boolean wasInitialized = tempCurrentScope.hashTable.get(assignedKey.value).isInitialized;
//                        boolean wasUsed = tempCurrentScope.hashTable.get(assignedKey.value).isUsed;
//                        Token wasToken = tempCurrentScope.hashTable.get(assignedKey.value).token;
//                        // Then assign accordingly
//                        if (wasInitialized == false) { // mark key as is Initialized and keep wasUsed the same
//                            idDetails details = new idDetails(wasType, true, wasUsed, wasToken);
//                            tempCurrentScope.hashTable.put(assignedKey.value, details); // Remake the hashvalue with edits to idDetails
//                        }
//                    }
//                }
                    break;
                case ("printStatement"):
                    Node printKey = node.children.get(0);
//                System.out.println(printKey.value);
                    boolean foundKey = false;
                    // TODO: Maybe make this it's own method
                    tempCurrentScope = currentScope;
                    if (tempCurrentScope.hashTable.get(printKey.value) == null) { // if identifier is undeclared in current scope
                        // Check if previous (outer) scope declared the variable being assigned and keep going to outer scope until no scopes left
                        while (tempCurrentScope != null & !foundKey) {
                            if (tempCurrentScope.hashTable.get(printKey.value) != null) {
                                foundKey = true; // Found key in a different scope (we use this key for assignment
                            } else {
                                tempCurrentScope = tempCurrentScope.prev;
                            }
                        }
                        if (!foundKey) {
                            if (printKey.name.equals("ID")){
                                System.out.println("SEMANTIC ANALYSIS [ERROR]: -------> Undeclared Identifier [" + printKey.value + "] at line " +
                                        printKey.token.line_number + ", char " + printKey.token.character_number);
                                numErrors = numErrors + 1;
                            }
                        }
                    } else {
                        foundKey = true;
                    }
//                System.out.println("FOUND KEY " + foundKey);
                    // If the key is found in some scope, mark key as isUsed
                    if (foundKey) {
//                    System.out.println(printKey.name);
//                    System.out.println("FOUND KEY");
                        // First get original details from hashtable of key
                        // tempCurrentScope is the scope the key value is in
                        String wasType = tempCurrentScope.hashTable.get(printKey.value).type;
                        boolean wasInitialized = tempCurrentScope.hashTable.get(printKey.value).isInitialized;
                        boolean wasUsed = tempCurrentScope.hashTable.get(printKey.value).isUsed;
                        Token wasToken = tempCurrentScope.hashTable.get(printKey.value).token;
                        // check if key was not initialized but is being used
                        if (wasInitialized == false) {
                            System.out.println("SEMANTIC ANALYSIS [ERROR]: -------> Uninitialized Identifier while being used at " +
                                    printKey.token.line_number + ", char " + printKey.token.character_number);
                            numErrors = numErrors + 1;
                        }
                        // Then assign accordingly
                        if (wasUsed == false) { // mark key as is Initialized and keep wasUsed the same
                            idDetails details = new idDetails(wasType, wasInitialized, true, wasToken);
                            tempCurrentScope.hashTable.put(printKey.value, details); // Remake the hashvalue with edits to idDetails
                        }
                    }
                    break;

                case ("ifStatement"), ("whileStatement"):
                    Node expr = node.children.get(0);
                    // First search for IDs in the boolean expression that may be labelled as mixed
                    // Then search for IDs labelled as IDs and make sure of declarations
                    // Then reassign the IDs as either intExpression, stringExpression, or booleanExpression
                    // Then typeCheck in the boolean expression (locally, unlike in AbstractSyntaxTree.java)

                    // Search for IDs in the boolean expression that may be labelled as mixed

                    ArrayList<Node> mixedIdNodes = new ArrayList<>();
                    traverseFind(expr, mixedIdNodes, "mixedExpr"); // Find nodes labelled as mixedExpr and append to ArrayList()

                    tempCurrentScope = currentScope;
                    for (Node mixedIdNode: mixedIdNodes){ // This only catches IDs that are in some scope; if undeclared, will be removed and left as mixedExpr --> this error is catched (hopefully)
                        boolean foundID = false;
                        if (tempCurrentScope.hashTable.get(mixedIdNode.value) == null){ //If can't find ID in current scope
                            // Check if previous (outer) scope declared the variable being assigned and keep going to outer scope until no scopes left
                            while (tempCurrentScope != null & !foundID) {
                                if (tempCurrentScope.hashTable.get(mixedIdNode.value) != null) {
                                    foundID = true; // Found key in a different scope (we use this key for assignment
                                } else {
                                    // Go up a scope
                                    tempCurrentScope = tempCurrentScope.prev;
                                }
                            }
                            if (!foundID) {
                                mixedIdNodes.remove(mixedIdNode); // remove the nodes with mixedExpr as their name since not IDs
                            }
                        }
                    }

                    // We should get an ArrayList of just IdNodes with mixedExpr as their names here but are actually IDs declared in some valid scope

                    // First search for IDs in the boolean expression since they may be labelled as mixed
                    idNodes = new ArrayList<>();
                    traverseFind(expr, idNodes, "ID"); //Finds all nodes with name=="ID" and appends to an ArrayList()

                    // Check if all IDs in idNodes are in current scope or outer scope; else, output undeclared ERROR
                    foundUndeclared = false;
                    tempCurrentScope = currentScope;
                    for (Node i: idNodes){
                        boolean foundID = false;
                        if (tempCurrentScope.hashTable.get(i.value) == null){ //If can't find ID in current scope
                            // Check if previous (outer) scope declared the variable being assigned and keep going to outer scope until no scopes left
                            while (tempCurrentScope != null & !foundID) {
                                if (tempCurrentScope.hashTable.get(i.value) != null) {
                                    foundID = true; // Found key in a different scope (we use this key for assignment
                                } else {
                                    // Go up a scope
                                    tempCurrentScope = tempCurrentScope.prev;
                                }
                            }
                            if (!foundID) {
                                foundUndeclared = true;
                                System.out.println("SEMANTIC ANALYSIS [ERROR]: -------> Undeclared Identifier [" + i.value + "] at line " +
                                        i.token.line_number + ", char " + i.token.character_number);
                                numErrors = numErrors + 1;
                            }
                        }
                    }

                    if (!foundUndeclared){
                        tempCurrentScope = currentScope;
                        // Append the two arraylists of IDs
                        idNodes.addAll(mixedIdNodes); //Append all of mixedIdNodes into idNodes // ALL of these IDs are in some scope
                        // Rename each idNode with respect to how they're declared
                        for (Node idNode: idNodes){ // We can get rid of foundID since we know its in a scope
                            // Check if current/previous (outer) scope declared the variable being assigned and keep going to outer scope until no scopes left
                            foundKey = false;
                            while (tempCurrentScope != null & !foundKey) {
                                if (tempCurrentScope.hashTable.get(idNode.value) != null) {
                                    foundKey = true;
                                    String declaredType = tempCurrentScope.hashTable.get(idNode.value).type;
                                    switch (declaredType){ // Redefine their names with respect to what type the ID was declared as
                                        case ("int"):
                                            idNode.name = "intExpr";
                                            break;
                                        case ("string"):
                                            idNode.name = "stringExpr";
                                            break;
                                        case ("boolean"):
                                            idNode.name = "boolExpr";
                                            break;
                                    }
                                } else {
                                    // Go up a scope
                                    tempCurrentScope = tempCurrentScope.prev;
                                }
                            }
                        }

                        // At this point, we should have a subtree with no ID and possibly some mixed Expr that are actually mixed or possibly mixed due to ID comparison.
                        // If there were noe IDs, we're here by default
                        // Do a pseudo parse using depth-first post-order traversal for the branch to see if anymore mixed Expressions
                        // If found a Node, output type mismatch
                        testMixed = postOrderFindIsMixed(expr, "", null);
                        if (testMixed != null){
                            if (testMixed.name.equals("ID")){ // In the case of an undeclared identifier
                                System.out.println("SEMANTIC ANALYSIS [ERROR]: -------> Type Mismatch in boolean expression at " +
                                        testMixed.token.line_number + ", char " + testMixed.token.character_number + ". Didn't expect an undeclared identifier");
                                numErrors = numErrors + 1;
                            }
                            else {
                                System.out.println("SEMANTIC ANALYSIS [ERROR]: -------> Type Mismatch in boolean expression at " +
                                        testMixed.token.line_number + ", char " + testMixed.token.character_number + ". Didn't expect [" + testMixed.name + "]");
                                numErrors = numErrors + 1;
                            }
                        }
                    }
                    break;
                default:
                    //Everything else that needs nothing
            }

            for (Node each : node.children) {
                processNode(each);
            }
            if (node.name.equals("block")) {
//            System.out.println("Block Done");
                currentScope = currentScope.prev; // Go back up the tree at outer scope
            }



        // Clear visited property of nodes
//        clearNodes(ast.root);

    }
    public static void codeGenVarDecal(){

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
