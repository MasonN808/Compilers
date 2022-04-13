import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
/**
 * <h1>AST</h1>
 * We design an alternative recursive descent AST to produce an AST
 * <p>
 * <b>Note:</b> Still in progress
 *
 * @author  Mason Nakamura
 * @since   04-04-2022
 */

public class AbstractSyntaxTree {
    private static int index = 0;
    private static ArrayList<Token> tokenStream;
    public static TreeAST ast = null;
    public static boolean verbose;
    //From https://stackoverflow.com/questions/7604814/best-way-to-format-multiple-or-conditions-in-an-if-statement
    //Makes the lengthy boolean statement more efficient
    private static final Set<Compiler808.Grammar> statementListValues = new HashSet<Compiler808.Grammar>(Arrays.asList(
            Compiler808.Grammar.PRINT, Compiler808.Grammar.ID, Compiler808.Grammar.VARIABLE_TYPE/*CASE: VarDecal*/,
            Compiler808.Grammar.WHILE, Compiler808.Grammar.IF, Compiler808.Grammar.L_BRACE/*CASE: Block*/));
    private static final Set<Compiler808.Grammar> matchValues = new HashSet<Compiler808.Grammar>(Arrays.asList(
            Compiler808.Grammar.ID, Compiler808.Grammar.VARIABLE_TYPE));
    public static boolean foundError = false;
    public static ArrayList<String> exprList = new ArrayList<>();
    public static ArrayList<String> exprListTemp = new ArrayList<>();


    public boolean isInt = false;
    public boolean isString = false;
    public boolean isBool = false;
    public boolean isId = false;
    public boolean isMixed = false;  // If there is an expression with mixed types --> becomes a semantic error

    public boolean inAssignment = false;
    public boolean inBooleanExpr = false;
    public int skipMoveUp = 0;


    // Constructor to set new token_stream for parsing
    public AbstractSyntaxTree(ArrayList<Token> tokenStream, boolean verbose){
        this.tokenStream = tokenStream;
        this.verbose = verbose;
        ast = new TreeAST();
        this.foundError = false;
        this.index = 0;
    }

    private static int getIndex(){
        return index;
    }

    private static void addIndex(){
        index = index + 1;
    }

    public static void match(Compiler808.Grammar expected){
        if (foundError) return;
        else {
            Token given = tokenStream.get(getIndex());
            // Check the expected token_type is the same as the one given
            if (expected == given.token_type & matchValues.contains(given.token_type)) {
//                System.out.println();
                ast.addNode("leaf", given.token_type.toString(), given);
            } else { //should not have unexpected token
                // Skip
            }
            addIndex();
        }
    }

    public void parseProgram(){
        if (foundError) return;
        else {
            //if (verbose) System.out.println("AST -------> parseProgram() ---->  " +  tokenStream.get(getIndex()).s);

//            ast.addNode("root", "program");
            parseBlock();
            match(Compiler808.Grammar.EOP);
//            ast.moveUp();
        }
    }

    public void parseBlock(){
        if (foundError) return;
        else {
//            if (verbose) System.out.println("AST -------> parseBlock() ---->  " +  tokenStream.get(getIndex()).s);
            if (ast.root == null){
                ast.addNode("root", "block", tokenStream.get(getIndex()));
            }
            else{
                ast.addNode("branch", "block", tokenStream.get(getIndex()));
            }
            match(Compiler808.Grammar.L_BRACE);
            parseStatementList();
            match(Compiler808.Grammar.R_BRACE);
            if (ast.current != ast.root){
                ast.moveUp();
            }
        }
    }

    public void parseStatementList(){
        if (foundError) return;
        else {
            //if (verbose) System.out.println("AST -------> parseStatementList() ----> " + tokenStream.get(getIndex()).s);

//            ast.addNode("branch", "statementList");
            if (statementListValues.contains(tokenStream.get(getIndex()).token_type)) {
                parseStatement();
                parseStatementList();
            }
//            ast.moveUp();
        }
    }

    public void parseStatement(){
        if (foundError) return;
        else {
            //if (verbose) System.out.println("AST -------> parseStatement() ---->  " +  tokenStream.get(getIndex()).s);

//            ast.addNode("branch", "statement");
            //switch statement WONT WORK
            if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.PRINT) parsePrintStatement();
            else if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.ID) parseAssignmentStatement();
            else if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.VARIABLE_TYPE) parseVarDecal();
            else if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.WHILE) parseWhileStatement();
            else if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.IF) parseIfStatement();
            else parseBlock();
//            ast.moveUp();
        }
    }

    public void parsePrintStatement(){
        if (foundError) return;
        else {
//            //if (verbose) System.out.println("AST -------> parsePrintStatement() ---->  " +  tokenStream.get(getIndex()).s);

            ast.addNode("branch", "printStatement", tokenStream.get(getIndex()));
            match(Compiler808.Grammar.PRINT);
            match(Compiler808.Grammar.L_PARENTH);
            resetPointers();
            parseExprPrint();
            createLeafs();
            match(Compiler808.Grammar.R_PARENTH);
            ast.moveUp();
        }
    }


    // two versions of parseExpr() to differentiate what productions output a list
    public void parseExprPrint(){
        if (foundError) return;
        else {
            if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.DIGIT){
                isInt = true;
                parseIntExpr();
            }
            else if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.QUOTE){
                isString = true;
                parseStringExpr();
            }
            else if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.L_PARENTH | tokenStream.get(getIndex()).token_type == Compiler808.Grammar.FALSE | tokenStream.get(getIndex()).token_type == Compiler808.Grammar.TRUE){
                isBool = true;
                parseBooleanExpr();
            }
            else {
                isId = true;
                parseId();
            }
            createLeafs();
        }
    }

    public void parseExpr(){
        if (foundError) return;
        else {
            if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.DIGIT){
                isInt = true;
                parseIntExpr();
            }
            else if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.QUOTE){
                isString = true;
                parseStringExpr();
            }
            else if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.L_PARENTH | tokenStream.get(getIndex()).token_type == Compiler808.Grammar.FALSE | tokenStream.get(getIndex()).token_type == Compiler808.Grammar.TRUE){
                isBool = true;
                parseBooleanExpr();
            }
            else {
                isId = true;
                parseId();
            }
        }
    }

    public void createLeafs(){
        if (!exprList.isEmpty() & isMixed) {
            ast.addNodeAsStringList("leaf", "mixedExpr", exprList, tokenStream.get(getIndex()));
            exprList.clear(); //Clear the arrayList of strings
            resetPointers();
        } else if (!exprList.isEmpty() & isId) {
            ast.addNodeAsStringList("leaf", "ID", exprList, tokenStream.get(getIndex()));
            exprList.clear(); //Clear the arrayList of strings
            resetPointers();
        } else if (!exprList.isEmpty() & isInt) {
            ast.addNodeAsStringList("leaf", "intExpr", exprList, tokenStream.get(getIndex()));
            exprList.clear(); //Clear the arrayList of strings
            resetPointers();
        } else if (!exprList.isEmpty() & isString) {
            ast.addNodeAsStringList("leaf", "stringExpr", exprList, tokenStream.get(getIndex()));
            exprList.clear(); //Clear the arrayList of strings
            resetPointers();
        } else if (!exprList.isEmpty() & isBool) {
            ast.addNodeAsStringList("leaf", "boolExpr", exprList, tokenStream.get(getIndex()));
            exprList.clear(); //Clear the arrayList of strings
            resetPointers();
        }
        resetPointers();
    }

    public void parseBooleanExpr(){
        if (foundError) return;
        else {
            if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.L_PARENTH) {
                inBooleanExpr = true; // to concatenate the list of tokens outputted in parseExprPrint
//                exprList.add(tokenStream.get(getIndex()).s);
                match(Compiler808.Grammar.L_PARENTH);
                String out1 = parseExprReturn(); // To check if both sides of Bool Op are of same type
                parseExpr();

                parseBoolOp();

                createLeafs();
                String out2 = parseExprReturn();
                if (!out1.equals(out2)){ // Check if the types being operated on are of the same type
                    isMixed = true;
                }

//                skipMoveUp += 1;
                parseExprPrint();
//                if (skipMoveUp != 1){
//                    ast.moveUp();
//                }
                ast.moveUp();

                match(Compiler808.Grammar.R_PARENTH);

            } else {
                if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.TRUE | tokenStream.get(getIndex()).token_type == Compiler808.Grammar.FALSE) {
                    exprList.add(tokenStream.get(getIndex()).s);
                    parseBoolVal();
                }
            }
        }
    }


    public void parseBoolOp(){
        if (foundError) return;
        else {
            ast.addNode("branch", "boolOp", tokenStream.get(getIndex()));
            if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.EQUALITY_OP) {
                match(Compiler808.Grammar.EQUALITY_OP);
            } else {
                match(Compiler808.Grammar.INEQUALITY_OP);
            }
//            ast.moveUp();
        }
    }


    public void parseAssignmentStatement(){
        if (foundError) return;
        else {
            ast.addNode("branch", "assignmentStatement", tokenStream.get(getIndex()));
            match(Compiler808.Grammar.ID);
            match(Compiler808.Grammar.ASSIGNMENT_OP);
//            inAssignment = true;
            parseExprPrint();
//            inAssignment = false;
            ast.moveUp();
        }
    }




    public void resetPointers(){
        isInt = false;
        isString = false;
        isBool = false;
        isId = false;
        isMixed = false;
    }

    public String parseExprReturn(){
        if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.DIGIT) {
            return "Int";
        } else if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.QUOTE) {
            return "String";
        } else if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.L_PARENTH | tokenStream.get(getIndex()).token_type == Compiler808.Grammar.FALSE | tokenStream.get(getIndex()).token_type == Compiler808.Grammar.TRUE) {
            return "Boolean";
        } else {
            return "Id";
        }
    }

    public void parseIntExpr(){
        if (foundError) return;
        else {
            exprList.add(tokenStream.get(getIndex()).s);
            match(Compiler808.Grammar.DIGIT);
            if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.ADDITION_OP) {
                parseIntop();
                String out1 = parseExprReturn();
                if (!out1.equals("Int")){
                    isMixed = true;
                }
                parseExprPrint();
            }
        }
    }

    public void parseIntop(){
        if (foundError) return;
        else {
            //if (verbose) System.out.println("AST -------> parseIntop() ---->  " +  tokenStream.get(getIndex()).s);

//            ast.addNode("branch", "intop");
            exprList.add(tokenStream.get(getIndex()).s);
            match(Compiler808.Grammar.ADDITION_OP);
//            ast.moveUp();
        }
    }

    public void parseStringExpr(){
        if (foundError) return;
        else {
            //if (verbose) System.out.println("AST -------> parseStringExpr() ---->  " +  tokenStream.get(getIndex()).s);

//            ast.addNode("branch", "stringExpr");
            exprList.add(tokenStream.get(getIndex()).s);
            match(Compiler808.Grammar.QUOTE);
            parseCharList();
            exprList.add(tokenStream.get(getIndex()).s);
            match(Compiler808.Grammar.QUOTE);
//            ast.moveUp();
        }
    }

    public void parseCharList(){
        if (foundError) return;
        else {
            //if (verbose) System.out.println("AST -------> parseCharList() ---->  " +  tokenStream.get(getIndex()).s);

//            ast.addNode("branch", "charList");
            if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.CHAR) {
                exprList.add(tokenStream.get(getIndex()).s);
                match(Compiler808.Grammar.CHAR);
                parseCharList();
            } else if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.SPACE) {
                exprList.add(tokenStream.get(getIndex()).s);
                match(Compiler808.Grammar.SPACE);
                parseCharList();
            } else {
                //epsilon production
            }
//            ast.moveUp();
        }
    }


    public void parseVarDecal(){
        if (foundError) return;
        else {
            //if (verbose) System.out.println("AST -------> parseVarDecal() ---->  " +  tokenStream.get(getIndex()).s);

            ast.addNode("branch", "varDecal", tokenStream.get(getIndex()));
            match(Compiler808.Grammar.VARIABLE_TYPE);
            match(Compiler808.Grammar.ID);
            ast.moveUp();
        }
    }

    public void parseWhileStatement(){
        if (foundError) return;
        else {
            //if (verbose) System.out.println("AST -------> parseWhileStatement() ---->  " +  tokenStream.get(getIndex()).s);

            ast.addNode("branch", "whileStatement", tokenStream.get(getIndex()));
            match(Compiler808.Grammar.WHILE);
            parseBooleanExpr();
            parseBlock();
            ast.moveUp();
        }
    }

    public void parseIfStatement(){
        if (foundError) return;
        else {
            //if (verbose) System.out.println("AST -------> parseIfStatement() ---->  " +  tokenStream.get(getIndex()).s);

            ast.addNode("branch", "ifStatement", tokenStream.get(getIndex()));
            match(Compiler808.Grammar.IF);
            resetPointers();
            parseBooleanExpr();
            parseBlock();
            ast.moveUp();
        }
    }





    public void parseBoolVal(){
        if (foundError) return;
        else {
            //if (verbose) System.out.println("AST -------> parseBoolVal() ---->  " +  tokenStream.get(getIndex()).s);

//            ast.addNode("branch", "boolVal", tokenStream.get(getIndex()));
            if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.TRUE) {
                match(Compiler808.Grammar.TRUE);
            } else {
                match(Compiler808.Grammar.FALSE);
            }
//            ast.moveUp();
        }
    }

    public void parseId(){
        if (foundError) return;
        else {
            //if (verbose) System.out.println("AST -------> parseId() ---->  " +  tokenStream.get(getIndex()).s);

//            ast.addNode("branch", "id", tokenStream.get(getIndex()));
            match(Compiler808.Grammar.ID);
//            ast.moveUp();
        }
    }

//    public void rearrangeTree(Node node){// post order traversal
//            for(Node each : node.children){
//                if (each.name.equals("ifStatement")|each.name.equals("whileStatement")) {
//                    Node boolOp = each.children.get(1);
//                    boolOp.children.add(each.children.get(0)); //Add child 0 to child of child 1
//                    boolOp.children.add(each.children.get(2)); //Add child 2 to child of child 1
//                    each.children.get(0).parent = boolOp; //assign the parent of child 0 to be child 1
//                    each.children.get(2).parent = boolOp; //assign the parent of child 2 to be child 1
//                    each.children.remove(2);//remove the children
//                    each.children.remove(0);
//                }
//                rearrangeTree(each);
//            }
//    }

}
