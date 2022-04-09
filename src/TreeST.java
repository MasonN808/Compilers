import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Hashtable;

public class TreeST {
    public static Node root = null;
    public static Node current = null;
    public static TreeST tree = null;
    public static TreeAST ast = null;

    public static int depth = 0; //Initialize depth of scopeDisplay
    public static ArrayList<ScopeNode> scopeDisplay; //Initialize an Array list of Scope Nodes to assign children to each node and an INT for depth
    public static Hashtable<String,idDetails> hashTable = new Hashtable<>();
    public static ScopeNode currentScope = null; // The current scope node


//        idDetails details = new idDetails(type, isInitialized, isUsed, current.token);
//        ht.put(id, details);

    public TreeST(TreeAST ast){
        this.ast = ast;
        this.root = null;
        this.current = null;
        this.depth = 0;
    }

    // Created a node class for scopeDisplay to assign pointers to children
    // TODO: might need to assign parent for children (MIGHT)
    public static class ScopeNode{
        public ArrayList<Node> children = new ArrayList<>();
        public Hashtable<String,idDetails> hashTable;
        public int depth;
        public ScopeNode prev = null; // Make pointers to see next scope or previous scope for checking symbol out of valid scope
        public ScopeNode next = null;

        public ScopeNode(Hashtable<String,idDetails> hashTable){
            this.hashTable = hashTable;
        }
    }

    public static void buildSymbolTable(){
        processNode(ast.root);
    }

    public static void processNode(Node node){
        switch (node.name){
            case ("block"):
                Hashtable<String,idDetails> hashTable = new Hashtable<>(); // Create hashtable in new scope
                ScopeNode scopeNode = new ScopeNode(hashTable); // Create new scope node
                if (depth == 0){
                    scopeNode.prev = null;
                }
                else {
                    scopeNode.prev = currentScope; // set the previous scope to outer scope
                }
                currentScope = scopeNode; // reinitialize current scope
                depth = depth + 1; // increase depth of tree

            case ("varDecal"):
                Node key = node.children.get(1);
                Node type = node.children.get(0);
                if (currentScope.hashTable.get(key.value) == null){
                    idDetails details = new idDetails(type.value, false, false, node.token);
                    currentScope.hashTable.put(key.value, details);
                }
                else{
                    System.out.println("SEMANTIC ANALYSIS [ERROR]: -------> Identifier redeclared error at line " +
                            node.children.get(1).token.line_number + ", char " + node.children.get(1).token.character_number);
                }
            case ("assignmentStatement"):
                Node assignedKey = node.children.get(0);
                Node assignedValue = node.children.get(1);
                boolean foundKey = false;
                ScopeNode tempCurrentScope = currentScope;
                if (currentScope.hashTable.get(assignedKey.value) == null){ // if identifier is undeclared in current scope
                    while (tempCurrentScope.prev != null & !foundKey){
                        if (tempCurrentScope.prev.hashTable.get(assignedKey.value) != null){
                            tempCurrentScope = tempCurrentScope.prev; //redefine tempCurrentScope to be used later
                            foundKey = true; // Found key in a different scope (we use this key for assignment
                        }
                        tempCurrentScope = tempCurrentScope.prev;
                        // Check if previous (outer) scope declared the variable being assigned and keep going to outer scope until no scopes left
                    }
                    if (!foundKey){
                        System.out.println("SEMANTIC ANALYSIS [ERROR]: -------> Undeclared Identifier at line " +
                                assignedKey.token.line_number + ", char " + assignedKey.token.character_number);
                    }
                }
                if (!checkAssignmentTypes(currentScope.hashTable.get(assignedKey.value).type, assignedValue.value) & foundKey) { // arg[0] and arg[1] will be strings
                    // if type-mismatch occurs in assignment
                    System.out.println("SEMANTIC ANALYSIS [ERROR]: -------> Type Mismatch: Expected " + currentScope.hashTable.get(assignedKey.value).type +  " at " +
                            assignedValue.token.line_number + ", char " + assignedValue.token.character_number);
                }
                else{ // key found
                    System.out.println("SEMANTIC ANALYSIS [ERROR]: -------> UNCAUGHT ERROR at assignmentStatement case ");
                    // First get original details from hashtable of key
                    // tempCurrentScope is the scope the key value is in
                    boolean wasInitialized = tempCurrentScope.hashTable.get(assignedKey.value).isInitialized;
                    boolean wasUsed = tempCurrentScope.hashTable.get(assignedKey.value).isUsed;
                    Token wasToken = tempCurrentScope.hashTable.get(assignedKey.value).token;
                    // Then assign accordingly
                    if (wasInitialized == false){ // mark key as is Initialized and keep wasUsed the same
                        idDetails details = new idDetails(assignedValue.value, true, wasUsed, wasToken);
                        tempCurrentScope.hashTable.put(assignedKey.value, details);
                    }

                }
            case ("printStatement"):
                Node printKey = node.children.get(0);
                foundKey = false;
                // TODO: Maybe make this it's own method
                if (currentScope.hashTable.get(printKey.value) == null){ // if identifier is undeclared in current scope
                    ScopeNode tempCurrentScope = currentScope;
                    while (tempCurrentScope.prev != null & !foundKey){
                        if (tempCurrentScope.prev.hashTable.get(printKey.value) != null){
                            foundKey = true; // Found key in a different scope (we use this key for assignment
                        }
                        tempCurrentScope = tempCurrentScope.prev;
                        // Check if previous (outer) scope declared the variable being assigned and keep going to outer scope until no scopes left
                    }
                    if (!foundKey){
                        System.out.println("SEMANTIC ANALYSIS [ERROR]: -------> Undeclared Identifier at line " +
                                printKey.token.line_number + ", char " + printKey.token.character_number);
                    }
                }

                // If the key is found in some scope, mark key as isUsed
                if (foundKey){

                }
            default:

                String symbol = retrieveSymbol(node.value);
                if (symbol == null){
                    System.out.println("Semantic ERROR: Undeclared Symbol");
                }
        }
        for (Node each: node.children){
            processNode(each);
        }
        if (node.name.equals("block")){
            closeScope();
        }
    }

    public static boolean checkAssignmentTypes(String type, String assigned){
        if (Character.toString(assigned.charAt(0)).equals("\"") & type.equals("string")){
            return true;
        }
        else if ((assigned.charAt(0)) == (int) assigned.charAt(0) & type.equals("int")){
            return true;
        }
        else if ((assigned.equals("false") | assigned.equals("true")) & type.equals("boolean")){
            return true;
        }
        else{
            return false;
        }
    }

//    public static void processNode(Node node){
//        switch (node.name){
//            case ("block"):
//                openScope();
//            case ("varDecal"):
//                enterSymbol(node.value, node.type);
//            default:
//                String symbol = retrieveSymbol(node.value);
//                if (symbol == null){
//                    System.out.println("Semantic ERROR: Undeclared Symbol");
//                }
//        }
//        for (Node each: node.children){
//            processNode(each);
//        }
//        if (node.name.equals("block")){
//            closeScope();
//        }
//    }


//    /**
//     * opens a new scope for symbol table
//     */
//    public static void openScope(){
//        depth = depth + 1; // add depth
//        scopeDisplay.set(depth, null); // set it to null
//    }
//
//
//    /**
//     * closes current scope
//     */
//    public static void closeScope(){
//        ArrayList<Node> tempChildren = scopeDisplay.get(depth).children;
//        for (Node sym: tempChildren){ // Do for loop on each child of scopeDisplay element at some depth
//            String prevSym = sym.value;
//            hashTable.remove(sym.value);
//            tempChildren.set(tempChildren.indexOf(sym), null); // Delete (set to null) the sym element in children
//            if (prevSym != null){
//                scopeDisplay.get(depth).children.set(tempChildren.indexOf(sym), null);
//            }
//        }
//    }



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


}
