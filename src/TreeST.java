import java.lang.reflect.Array;
import java.util.*;

public class TreeST {
    public static ScopeNode root = null;
    public static Node current = null;
    public static TreeST tree = null;
    public static TreeAST ast = null;

    public static int numErrors = 0;
    public static int numWarnings = 0;
    public static int scopeNum = 0; // How many scopes/nodes in tree

    public static int depth = 0; //Initialize depth of scopeDisplay
    public static ArrayList<ScopeNode> scopeDisplay; //Initialize an Array list of Scope Nodes to assign children to each node and an INT for depth
    public static Hashtable<String, idDetails> hashTable = new Hashtable<>();
    public static ScopeNode currentScope = null; // The current scope node


//        idDetails details = new idDetails(type, isInitialized, isUsed, current.token);
//        ht.put(id, details);

    public TreeST(TreeAST ast) {
        this.ast = ast;
        this.root = null;
        this.current = null;
        this.depth = 0;
        this.numErrors = 0;
        this.scopeNum = 0;
        this.currentScope = null;
    }

    // Created a node class for scopeDisplay to assign pointers to children
    // TODO: might need to assign parent for children (MIGHT)
    public static class ScopeNode {
        public ArrayList<ScopeNode> children = new ArrayList<>();
        public Hashtable<String, idDetails> hashTable;
        public int depth;
        public int scope;
        public ScopeNode prev = null; // Make pointers to see next scope or previous scope for checking symbol out of valid scope
        public ScopeNode next = null;

        public ScopeNode(Hashtable<String, idDetails> hashTable) {
            this.hashTable = hashTable;
        }
    }

    public static void buildSymbolTree() {
        processNode(ast.root);
    }

    public static void processNode(Node node) {
//        System.out.println(node.name);
//        System.out.println(currentScope != null);
        switch (node.name) {
            case ("block"):
                depth = depth + 1; // increase depth of tree // might not need this
                Hashtable<String, idDetails> hashTable = new Hashtable<>(); // Create hashtable in new scope
                ScopeNode scopeNode = new ScopeNode(hashTable); // Create new scope node
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
                Node key = node.children.get(1);
                Node type = node.children.get(0);
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
                Node assignedKey = node.children.get(0);
                Node assignedValue = node.children.get(1);
//                System.out.println("DEBUG: " + assignedValue.name);
//                System.out.println("DEBUG: " + tempCurrentScope.hashTable.get(assignedKey.value).type);
                boolean foundKey = false;
                ScopeNode tempCurrentScope = currentScope;
                //Traverse subtree to find mixed expressions or type mismatch
                boolean isMixed = traverse()
                if (assignedValue.name.equals("mixedExpr")){ // the assigned value is of mixed types
                    System.out.println("SEMANTIC ANALYSIS [ERROR]: -------> Type Mismatch in assigned expression at " +
                            assignedValue.token.line_number + ", char " + assignedValue.token.character_number);
                    numErrors = numErrors + 1;
                }
                if (tempCurrentScope.hashTable.get(assignedKey.value) == null) { // if identifier is undeclared in current scope try an outer scope
                    while (tempCurrentScope != null & !foundKey) {
                        if (tempCurrentScope.hashTable.get(assignedKey.value) != null) {
//                            tempCurrentScope = tempCurrentScope.prev; //redefine tempCurrentScope to be used later
                            foundKey = true; // Found key in a different scope (we use this key for assignment
                        } else {
                            tempCurrentScope = tempCurrentScope.prev;
                        }
                        // Check if previous (outer) scope declared the variable being assigned and keep going to outer scope until no scopes left
                    }
                    if (!foundKey) {
                        if (assignedKey.name.equals("ID")){
                            System.out.println("SEMANTIC ANALYSIS [ERROR]: -------> Undeclared Identifier [" + assignedKey.value + "] at line " +
                                    assignedKey.token.line_number + ", char " + assignedKey.token.character_number);
                            numErrors = numErrors + 1;
                        }
                    }
                } else {
                    foundKey = true;
                }
                // Debugging checking assignment mismatch
//                System.out.println("DEBUG: " + tempCurrentScope.hashTable.get(assignedKey.value).type);
//                System.out.println("DEBUG: " + assignedValue.value);
                if (foundKey){
                    if (!checkAssignmentTypes(tempCurrentScope.hashTable.get(assignedKey.value).type, assignedValue.value)){
                        System.out.println("SEMANTIC ANALYSIS [ERROR]: -------> Type Mismatch: Expected [" + tempCurrentScope.hashTable.get(assignedKey.value).type + "] at " +
                                assignedValue.token.line_number + ", char " + assignedValue.token.character_number);
                        numErrors = numErrors + 1;
                    }
                    else { // key found
                        // First get original details from hashtable of key
                        // tempCurrentScope is the scope the key value is in
                        String wasType = tempCurrentScope.hashTable.get(assignedKey.value).type;
                        boolean wasInitialized = tempCurrentScope.hashTable.get(assignedKey.value).isInitialized;
                        boolean wasUsed = tempCurrentScope.hashTable.get(assignedKey.value).isUsed;
                        Token wasToken = tempCurrentScope.hashTable.get(assignedKey.value).token;
                        // Then assign accordingly
                        if (wasInitialized == false) { // mark key as is Initialized and keep wasUsed the same
                            idDetails details = new idDetails(wasType, true, wasUsed, wasToken);
                            tempCurrentScope.hashTable.put(assignedKey.value, details); // Remake the hashvalue with edits to idDetails
                        }
                    }
                }
                break;
            case ("printStatement"):
                Node printKey = node.children.get(0);
//                System.out.println(printKey.value);
                foundKey = false;
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
                assignedKey = expr.children.get(0); //could be Key or a value
                assignedValue = expr.children.get(1);

                foundKey = false;
                tempCurrentScope = currentScope;
                if (tempCurrentScope.hashTable.get(assignedKey.value) == null) { // if identifier is undeclared in current scope
                    // Check if previous (outer) scope declared the variable being assigned and keep going to outer scope until no scopes left
                    while (tempCurrentScope != null & !foundKey) {
                        if (tempCurrentScope.hashTable.get(assignedKey.value) != null) {
                            foundKey = true; // Found key in a different scope (we use this key for assignment
                        } else {
                            tempCurrentScope = tempCurrentScope.prev;
                        }
                    }
                    if (!foundKey) {
                        if (assignedKey.name.equals("ID")){
                            System.out.println("SEMANTIC ANALYSIS [ERROR]: -------> Undeclared Identifier [" + assignedKey.value + "] at line " +
                                    assignedKey.token.line_number + ", char " + assignedKey.token.character_number);
                            numErrors = numErrors + 1;
                        }
                        else{
//                            System.out.println(assignedKey.name);
                            if (!checkAssignmentTypesExpr(assignedKey, assignedValue)){
                                System.out.println("SEMANTIC ANALYSIS [ERROR]: -------> Type Mismatch: Did you mean for an [" + assignedKey.name + "] at " +
                                        assignedValue.token.line_number + ", char " + assignedValue.token.character_number + "?");
                                numErrors = numErrors + 1;
                            }
                        }
                    }
                } else {
                    foundKey = true;
                }
                if (foundKey){
                    if (!checkAssignmentTypes(tempCurrentScope.hashTable.get(assignedKey.value).type, assignedValue.value)){
                        System.out.println("SEMANTIC ANALYSIS [ERROR]: -------> Type Mismatch: Expected [" + tempCurrentScope.hashTable.get(assignedKey.value).type + "] at " +
                                assignedValue.token.line_number + ", char " + assignedValue.token.character_number);
                        numErrors = numErrors + 1;
                    }
                    else { // key found
                        // First get original details from hashtable of key
                        // tempCurrentScope is the scope the key value is in
                        String wasType = tempCurrentScope.hashTable.get(assignedKey.value).type;
                        boolean wasInitialized = tempCurrentScope.hashTable.get(assignedKey.value).isInitialized;
                        boolean wasUsed = tempCurrentScope.hashTable.get(assignedKey.value).isUsed;
                        Token wasToken = tempCurrentScope.hashTable.get(assignedKey.value).token;
                        // Then assign accordingly
                        if (wasInitialized == false) { // mark key as is Initialized and keep wasUsed the same
                            idDetails details = new idDetails(wasType, true, wasUsed, wasToken);
                            tempCurrentScope.hashTable.put(assignedKey.value, details); // Remake the hashvalue with edits to idDetails
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
    }

    public static boolean checkAssignmentTypes(String type, String assigned) {
        if (Character.toString(assigned.charAt(0)).equals("\"") & type.equals("string")) {
            return true;
        } else if (type.equals("int")) {
            try { // See if String can be parsed to an Int
                int test = Integer.parseInt(Character.toString(assigned.charAt(0)));
            } catch (NumberFormatException nfe) { // If exception reached (not an int)
                return false;
            }
            return true;
        } else if ((assigned.equals("false") | assigned.equals("true")) & type.equals("boolean")) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean checkAssignmentTypesExpr(Node expr1, Node expr2) {
        // Both Strings
        if (expr1.name.equals("stringExpr") & expr2.name.equals("stringExpr")) {
            return true;
        }

        // Both Ints
        else if (expr1.name.equals("intExpr") & expr2.name.equals("intExpr")) {
            return true;
        }

        // Both Bools
        else if (expr1.name.equals("boolExpr") & expr2.name.equals("boolExpr")) {
            return true;
        }

        else return false;
    }

    // An edited implementation of https://www.techiedelight.com/breadth-first-search/ for BFS
    // Perform BFS on the graph starting from vertex `v`
    public static void BFS(TreeST tree, ScopeNode v, boolean[] discovered) {
        // create a queue for doing BFS
        Queue<ScopeNode> q = new ArrayDeque<>();

        // mark the source vertex as discovered
        discovered[v.scope] = true;

        // enqueue source vertex
        q.add(v);

        // loop till queue is empty
        while (!q.isEmpty()) {
            // dequeue front node and print it
            v = q.poll();
            //  For extracting ALL hash value keys from hashtable --> https://www.w3schools.blog/get-all-keys-from-hashtable-in-java#:~:text=We%20can%20use%20keySet(),Set%20object%20with%20all%20keys.
            Set<String> keys = v.hashTable.keySet();
            for (String key : keys) {
                // Put data in a row to print elegantly in a table format
                String[] row = new String[]{key, v.hashTable.get(key).type, Boolean.toString(v.hashTable.get(key).isInitialized), Boolean.toString(v.hashTable.get(key).isUsed), Integer.toString(v.scope), Integer.toString(v.hashTable.get(key).token.line_number)};
                System.out.format("%4s%15s%15s%15s%15s%15s%n", row);
            }


            // do for every edge (v, u)
            for (ScopeNode u : v.children) {
                if (!discovered[u.scope]) {
                    // mark it as discovered and enqueue it
                    discovered[u.scope] = true;
                    q.add(u);
                }
            }
        }
    }

    // Do a depth-first post-order traversal to find a mixed value in subtree
    public static boolean traverse(Node node, boolean foundMixed) { // post order traversal
        if (node.name.equals("mixedExpr")){
            foundMixed = true;
        }
        for (Node each : node.children) {
            traverse(each, foundMixed);
        }
        return foundMixed;
    }

    // Check for warnings using BFS
    public static void checkWarnings(TreeST tree, ScopeNode v, boolean[] discovered) {
        // create a queue for doing BFS
        Queue<ScopeNode> q = new ArrayDeque<>();

        // mark the source vertex as discovered
        discovered[v.scope] = true;

        // enqueue source vertex
        q.add(v);

        // loop till queue is empty
        while (!q.isEmpty()) {
            // dequeue front node and print it
            v = q.poll();
            //  For extracting ALL hash value keys from hashtable --> https://www.w3schools.blog/get-all-keys-from-hashtable-in-java#:~:text=We%20can%20use%20keySet(),Set%20object%20with%20all%20keys.
            Set<String> keys = v.hashTable.keySet();
            for (String key : keys) {
                if (v.hashTable.get(key).isInitialized == true & v.hashTable.get(key).isUsed == false){
                    System.out.println("SEMANTIC ANALYSIS [WARNING]: -------> The identifier [" + v.hashTable.get(key).token.s + "] was declared and initialized but never used at " +
                            v.hashTable.get(key).token.line_number + ", char " + v.hashTable.get(key).token.character_number);
                    numWarnings = numWarnings + 1;
                }
                if (v.hashTable.get(key).isInitialized == false & v.hashTable.get(key).isUsed == false){
                    System.out.println("SEMANTIC ANALYSIS [WARNING]: -------> The identifier [" + v.hashTable.get(key).token.s + "] was declared but never initialized at " +
                            v.hashTable.get(key).token.line_number + ", char " + v.hashTable.get(key).token.character_number);
                    numWarnings = numWarnings + 1;
                }
            }


            // do for every edge (v, u)
            for (ScopeNode u : v.children) {
                if (!discovered[u.scope]) {
                    // mark it as discovered and enqueue it
                    discovered[u.scope] = true;
                    q.add(u);
                }
            }
        }
    }
}