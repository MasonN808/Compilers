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
    public static Tree ast = null;
    public static boolean verbose;
    //From https://stackoverflow.com/questions/7604814/best-way-to-format-multiple-or-conditions-in-an-if-statement
    //Makes the lengthy boolean statement more efficient
    private static final Set<Compiler808.Grammar> statementListValues = new HashSet<Compiler808.Grammar>(Arrays.asList(
            Compiler808.Grammar.PRINT, Compiler808.Grammar.ID, Compiler808.Grammar.VARIABLE_TYPE/*CASE: VarDecal*/,
            Compiler808.Grammar.WHILE, Compiler808.Grammar.IF, Compiler808.Grammar.L_BRACE/*CASE: Block*/));
    public static boolean foundError = false;


    // Constructor to set new token_stream for parsing
    public AbstractSyntaxTree(ArrayList<Token> tokenStream, boolean verbose){
        this.tokenStream = tokenStream;
        this.verbose = verbose;
        ast = new Tree();
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
            if (expected == given.token_type & (given.token_type == Compiler808.Grammar.VARIABLE_TYPE | given.token_type == Compiler808.Grammar.ID | given.token_type == Compiler808.Grammar.DIGIT)) {
//                System.out.println();
                Tree.addNode("leaf", given.token_type.toString());
            } else { //Error if unexpected token
                System.out.println("AST [ERROR][Unexpected Token] --------> expected [ " + expected + " ], got [" + given.token_type + "] at " + given.line_number + ", " + given.character_number);
                foundError = true;
                return;
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
            //if (verbose) System.out.println("AST -------> parseBlock() ---->  " +  tokenStream.get(getIndex()).s);

            ast.addNode("branch", "block");
            match(Compiler808.Grammar.L_BRACE);
            parseStatementList();
            match(Compiler808.Grammar.R_BRACE);
            ast.moveUp();
        }
    }

    public void parseStatementList(){
        if (foundError) return;
        else {
            //if (verbose) System.out.println("AST -------> parseStatementList() ----> " + tokenStream.get(getIndex()).s);

            ast.addNode("branch", "statementList");
            if (statementListValues.contains(tokenStream.get(getIndex()).token_type)) {
                parseStatement();
                parseStatementList();
            }
            ast.moveUp();
        }
    }

    public void parseStatement(){
        if (foundError) return;
        else {
            //if (verbose) System.out.println("AST -------> parseStatement() ---->  " +  tokenStream.get(getIndex()).s);

            ast.addNode("branch", "statement");
            //switch statement WONT WORK
            if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.PRINT) parsePrintStatement();
            else if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.ID) parseAssignmentStatement();
            else if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.VARIABLE_TYPE) parseVarDecal();
            else if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.WHILE) parseWhileStatement();
            else if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.IF) parseIfStatement();
            else parseBlock();
            ast.moveUp();
        }
    }

    public void parsePrintStatement(){
        if (foundError) return;
        else {
//            //if (verbose) System.out.println("AST -------> parsePrintStatement() ---->  " +  tokenStream.get(getIndex()).s);

            ast.addNode("branch", "printStatement");
            match(Compiler808.Grammar.PRINT);
            match(Compiler808.Grammar.L_PARENTH);
            parseExpr();
            match(Compiler808.Grammar.R_PARENTH);
            ast.moveUp();
        }
    }

    public void parseExpr(){
        if (foundError) return;
        else {
            //if (verbose) System.out.println("AST -------> parseExpr() ---->  " +  tokenStream.get(getIndex()).s);
            ast.addNode("branch", "expr");
            if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.DIGIT) parseIntExpr();
            else if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.QUOTE) parseStringExpr();
            else if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.L_PARENTH | tokenStream.get(getIndex()).token_type == Compiler808.Grammar.FALSE | tokenStream.get(getIndex()).token_type == Compiler808.Grammar.TRUE){
                parseBooleanExpr();
            }
            else parseId();
            ast.moveUp();
        }
    }

    public void parseIntExpr(){
        if (foundError) return;
        else {
            //if (verbose) System.out.println("AST -------> parseIntExpr() ---->  " +  tokenStream.get(getIndex()).s);

            ast.addNode("branch", "intExpr");
            match(Compiler808.Grammar.DIGIT);
            if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.ADDITION_OP) {
                parseIntop();
                parseExpr();
            }
            ast.moveUp();
        }
    }

    public void parseIntop(){
        if (foundError) return;
        else {
            //if (verbose) System.out.println("AST -------> parseIntop() ---->  " +  tokenStream.get(getIndex()).s);

            ast.addNode("branch", "intop");
            match(Compiler808.Grammar.ADDITION_OP);
            ast.moveUp();
        }
    }

    public void parseStringExpr(){
        if (foundError) return;
        else {
            //if (verbose) System.out.println("AST -------> parseStringExpr() ---->  " +  tokenStream.get(getIndex()).s);

            ast.addNode("branch", "stringExpr");
            match(Compiler808.Grammar.QUOTE);
            parseCharList();
            match(Compiler808.Grammar.QUOTE);
            ast.moveUp();
        }
    }

    public void parseCharList(){
        if (foundError) return;
        else {
            //if (verbose) System.out.println("AST -------> parseCharList() ---->  " +  tokenStream.get(getIndex()).s);

            ast.addNode("branch", "charList");
            if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.CHAR) {
                match(Compiler808.Grammar.CHAR);
                parseCharList();
            } else if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.SPACE) {
                match(Compiler808.Grammar.SPACE);
                parseCharList();
            } else {
                //epsilon production
            }
            ast.moveUp();
        }
    }

    public void parseAssignmentStatement(){
        if (foundError) return;
        else {
            //if (verbose) System.out.println("AST -------> parseAssignmentStatement() ---->  " +  tokenStream.get(getIndex()).s);

            ast.addNode("branch", "assignmentStatement");
            match(Compiler808.Grammar.ID);
            match(Compiler808.Grammar.ASSIGNMENT_OP);
            parseExpr();
            ast.moveUp();
        }
    }

    public void parseVarDecal(){
        if (foundError) return;
        else {
            //if (verbose) System.out.println("AST -------> parseVarDecal() ---->  " +  tokenStream.get(getIndex()).s);

            ast.addNode("branch", "varDecal");
            match(Compiler808.Grammar.VARIABLE_TYPE);
            match(Compiler808.Grammar.ID);
            ast.moveUp();
        }
    }

    public void parseWhileStatement(){
        if (foundError) return;
        else {
            //if (verbose) System.out.println("AST -------> parseWhileStatement() ---->  " +  tokenStream.get(getIndex()).s);

            ast.addNode("branch", "whileStatement");
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

            ast.addNode("branch", "ifStatement");
            match(Compiler808.Grammar.IF);
            parseBooleanExpr();
            parseBlock();
            ast.moveUp();
        }
    }

    public void parseBooleanExpr(){
        if (foundError) return;
        else {
            //if (verbose) System.out.println("AST -------> parseBooleanExpr() ---->  " +  tokenStream.get(getIndex()).s);

            ast.addNode("branch", "booleanExpr");
            if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.L_PARENTH) {
                match(Compiler808.Grammar.L_PARENTH);
                parseExpr();
                parseBoolOp();
                parseExpr();
                match(Compiler808.Grammar.R_PARENTH);
            } else {
                if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.TRUE | tokenStream.get(getIndex()).token_type == Compiler808.Grammar.FALSE) {
                    parseBoolVal();
                }
            }
            ast.moveUp();
        }
    }

    public void parseBoolOp(){
        if (foundError) return;
        else {
            //if (verbose) System.out.println("AST -------> parseBoolOp() ---->  " +  tokenStream.get(getIndex()).s);

            ast.addNode("branch", "boolOp");
            if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.EQUALITY_OP) {
                match(Compiler808.Grammar.EQUALITY_OP);
            } else {
                match(Compiler808.Grammar.INEQUALITY_OP);
            }
            ast.moveUp();
        }
    }

    public void parseBoolVal(){
        if (foundError) return;
        else {
            //if (verbose) System.out.println("AST -------> parseBoolVal() ---->  " +  tokenStream.get(getIndex()).s);

            ast.addNode("branch", "boolVal");
            if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.TRUE) {
                match(Compiler808.Grammar.TRUE);
            } else {
                match(Compiler808.Grammar.FALSE);
            }
            ast.moveUp();
        }
    }

    public void parseId(){
        if (foundError) return;
        else {
            //if (verbose) System.out.println("AST -------> parseId() ---->  " +  tokenStream.get(getIndex()).s);

            ast.addNode("branch", "id");
            match(Compiler808.Grammar.ID);
            ast.moveUp();
        }
    }

}
