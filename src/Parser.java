import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * <h1>Parser (Part 2)</h1>
 * We design a Parser to produce a Concrete Syntax Tree (CST)
 * using recursive descent parsing
 * <p>
 * <b>Note:</b> Still in progress
 *
 * @author  Mason Nakamura
 * @since   03-01-2022
 */
public class Parser {

    private static int index = 0;
    private static ArrayList<Token> tokenStream;
    public static Tree cst = null;
    public static boolean verbose;
    //From https://stackoverflow.com/questions/7604814/best-way-to-format-multiple-or-conditions-in-an-if-statement
    //Makes the lengthy boolean statement more efficient
    private static final Set<Compiler808.Grammar> statementListValues = new HashSet<Compiler808.Grammar>(Arrays.asList(
            Compiler808.Grammar.PRINT, Compiler808.Grammar.ID, Compiler808.Grammar.VARIABLE_TYPE/*CASE: VarDecal*/,
            Compiler808.Grammar.WHILE, Compiler808.Grammar.IF, Compiler808.Grammar.L_BRACE/*CASE: Block*/));
    public static boolean foundError = false;


    // Constructor to set new token_stream for parsing
    public Parser(ArrayList<Token> tokenStream, boolean verbose){
        this.tokenStream = tokenStream;
        this.verbose = verbose;
        cst = new Tree();
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
            if (expected == given.token_type) {
//                System.out.println();
                Tree.addNode("leaf", given.token_type.toString());
            } else { //Error if unexpected token
                System.out.println("Parser [ERROR][Unexpected Token] --------> expected [ " + expected + " ], got [" + given.token_type + "] at " + given.line_number + ", " + given.character_number);
                foundError = true;
                return;
            }
            addIndex();
        }
    }

    public void parseProgram(){
        if (foundError) return;
        else {
            if (verbose) System.out.println("Parser -------> parseProgram() ---->  " +  tokenStream.get(getIndex()).s);

            cst.addNode("root", "program");
            parseBlock();
            match(Compiler808.Grammar.EOP);
//            cst.moveUp();
        }
    }

    public void parseBlock(){
        if (foundError) return;
        else {
            if (verbose) System.out.println("Parser -------> parseBlock() ---->  " +  tokenStream.get(getIndex()).s);

            cst.addNode("branch", "block");
            match(Compiler808.Grammar.L_BRACE);
            parseStatementList();
            match(Compiler808.Grammar.R_BRACE);
            cst.moveUp();
        }
    }

    public void parseStatementList(){
        if (foundError) return;
        else {
            if (verbose) System.out.println("Parser -------> parseStatementList() ----> " + tokenStream.get(getIndex()).s);

            cst.addNode("branch", "statementList");
            if (statementListValues.contains(tokenStream.get(getIndex()).token_type)) {
                parseStatement();
                parseStatementList();
            }
            cst.moveUp();
        }
    }

    public void parseStatement(){
        if (foundError) return;
        else {
            if (verbose) System.out.println("Parser -------> parseStatement() ---->  " +  tokenStream.get(getIndex()).s);

            cst.addNode("branch", "statement");
            //switch statement WONT WORK
            if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.PRINT) parsePrintStatement();
            else if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.ID) parseAssignmentStatement();
            else if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.VARIABLE_TYPE) parseVarDecal();
            else if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.WHILE) parseWhileStatement();
            else if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.IF) parseIfStatement();
            else parseBlock();
            cst.moveUp();
        }
    }

    public void parsePrintStatement(){
        if (foundError) return;
        else {
            if (verbose) System.out.println("Parser -------> parsePrintStatement() ---->  " +  tokenStream.get(getIndex()).s);

            cst.addNode("branch", "printStatement");
            match(Compiler808.Grammar.PRINT);
            match(Compiler808.Grammar.L_PARENTH);
            parseExpr();
            match(Compiler808.Grammar.R_PARENTH);
            cst.moveUp();
        }
    }

    public void parseExpr(){
        if (foundError) return;
        else {
            if (verbose) System.out.println("Parser -------> parseExpr() ---->  " +  tokenStream.get(getIndex()).s);
            cst.addNode("branch", "expr");
            if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.DIGIT) parseIntExpr();
            else if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.QUOTE) parseStringExpr();
            else if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.L_PARENTH | tokenStream.get(getIndex()).token_type == Compiler808.Grammar.FALSE | tokenStream.get(getIndex()).token_type == Compiler808.Grammar.TRUE){
                parseBooleanExpr();
            }
            else parseId();
            cst.moveUp();
        }
    }

    public void parseIntExpr(){
        if (foundError) return;
        else {
            if (verbose) System.out.println("Parser -------> parseIntExpr() ---->  " +  tokenStream.get(getIndex()).s);

            cst.addNode("branch", "intExpr");
            match(Compiler808.Grammar.DIGIT);
            if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.ADDITION_OP) {
                parseIntop();
                parseExpr();
            }
            cst.moveUp();
        }
    }

    public void parseIntop(){
        if (foundError) return;
        else {
            if (verbose) System.out.println("Parser -------> parseIntop() ---->  " +  tokenStream.get(getIndex()).s);

            cst.addNode("branch", "intop");
            match(Compiler808.Grammar.ADDITION_OP);
            cst.moveUp();
        }
    }

    public void parseStringExpr(){
        if (foundError) return;
        else {
            if (verbose) System.out.println("Parser -------> parseStringExpr() ---->  " +  tokenStream.get(getIndex()).s);

            cst.addNode("branch", "stringExpr");
            match(Compiler808.Grammar.QUOTE);
            parseCharList();
            match(Compiler808.Grammar.QUOTE);
            cst.moveUp();
        }
    }

    public void parseCharList(){
        if (foundError) return;
        else {
            if (verbose) System.out.println("Parser -------> parseCharList() ---->  " +  tokenStream.get(getIndex()).s);

            cst.addNode("branch", "charList");
            if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.CHAR) {
                match(Compiler808.Grammar.CHAR);
                parseCharList();
            } else if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.SPACE) {
                match(Compiler808.Grammar.SPACE);
                parseCharList();
            } else {
                //epsilon production
            }
            cst.moveUp();
        }
    }

    public void parseAssignmentStatement(){
        if (foundError) return;
        else {
            if (verbose) System.out.println("Parser -------> parseAssignmentStatement() ---->  " +  tokenStream.get(getIndex()).s);

            cst.addNode("branch", "assignmentStatement");
            match(Compiler808.Grammar.ID);
            match(Compiler808.Grammar.ASSIGNMENT_OP);
            parseExpr();
            cst.moveUp();
        }
    }

    public void parseVarDecal(){
        if (foundError) return;
        else {
            if (verbose) System.out.println("Parser -------> parseVarDecal() ---->  " +  tokenStream.get(getIndex()).s);

            cst.addNode("branch", "varDecal");
            match(Compiler808.Grammar.VARIABLE_TYPE);
            match(Compiler808.Grammar.ID);
            cst.moveUp();
        }
    }

    public void parseWhileStatement(){
        if (foundError) return;
        else {
            if (verbose) System.out.println("Parser -------> parseWhileStatement() ---->  " +  tokenStream.get(getIndex()).s);

            cst.addNode("branch", "whileStatement");
            match(Compiler808.Grammar.WHILE);
            parseBooleanExpr();
            parseBlock();
            cst.moveUp();
        }
    }

    public void parseIfStatement(){
        if (foundError) return;
        else {
            if (verbose) System.out.println("Parser -------> parseIfStatement() ---->  " +  tokenStream.get(getIndex()).s);

            cst.addNode("branch", "ifStatement");
            match(Compiler808.Grammar.IF);
            parseBooleanExpr();
            parseBlock();
            cst.moveUp();
        }
    }

    public void parseBooleanExpr(){
        if (foundError) return;
        else {
            if (verbose) System.out.println("Parser -------> parseBooleanExpr() ---->  " +  tokenStream.get(getIndex()).s);

            cst.addNode("branch", "booleanExpr");
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
            cst.moveUp();
        }
    }

    public void parseBoolOp(){
        if (foundError) return;
        else {
            if (verbose) System.out.println("Parser -------> parseBoolOp() ---->  " +  tokenStream.get(getIndex()).s);

            cst.addNode("branch", "boolOp");
            if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.EQUALITY_OP) {
                match(Compiler808.Grammar.EQUALITY_OP);
            } else {
                match(Compiler808.Grammar.INEQUALITY_OP);
            }
            cst.moveUp();
        }
    }

    public void parseBoolVal(){
        if (foundError) return;
        else {
            if (verbose) System.out.println("Parser -------> parseBoolVal() ---->  " +  tokenStream.get(getIndex()).s);

            cst.addNode("branch", "boolVal");
            if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.TRUE) {
                match(Compiler808.Grammar.TRUE);
            } else {
                match(Compiler808.Grammar.FALSE);
            }
            cst.moveUp();
        }
    }

    public void parseId(){
        if (foundError) return;
        else {
            if (verbose) System.out.println("Parser -------> parseId() ---->  " +  tokenStream.get(getIndex()).s);

            cst.addNode("branch", "id");
            match(Compiler808.Grammar.ID);
            cst.moveUp();
        }
    }
}
