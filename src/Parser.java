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
            Compiler808.Grammar.PRINT, Compiler808.Grammar.ASSIGNMENT_OP, Compiler808.Grammar.VARIABLE_TYPE/*CASE: VarDecal*/,
            Compiler808.Grammar.WHILE, Compiler808.Grammar.IF, Compiler808.Grammar.L_BRACE/*CASE: Block*/));



    // Constructor to set new token_stream for parsing
    public Parser(ArrayList<Token> tokenStream, boolean verbose){
        this.tokenStream = tokenStream;
        this.verbose = verbose;
        cst = new Tree();

    }

    private static int getIndex(){
        return index;
    }

    private static void addIndex(){
        index = index + 1;
    }

    public static void match(Compiler808.Grammar expected){
        Token given = tokenStream.get(getIndex());
        // Check the expected token_type is the same as the one given
         if (expected == given.token_type){
             Tree.addNode("leaf", given.token_type.toString());
         }
         else{ //Error if unexpected token
             System.out.println("PARSE ERROR (Unexpected Token) --------> expected [ " + expected + " ], got [" + given.token_type + "]"); //TODO: maybe use logger.log() instead
         }
         addIndex();
    }

    public void parseProgram(){
        if (verbose) System.out.println("parseProgram()");
        cst.addNode("root", "goal");
        parseBlock();
        match(Compiler808.Grammar.EOP);
    }

    public void parseBlock(){
        if (verbose) System.out.println("parseBlock()");
        cst.addNode("branch", "block");
        match(Compiler808.Grammar.L_BRACE);
        parseStatementList();
        match(Compiler808.Grammar.R_BRACE);
    }

    public void parseStatementList(){
        if (verbose) System.out.println("parseStatementList()");
        cst.addNode("branch", "statementList");
        if(statementListValues.contains(tokenStream.get(getIndex()).token_type)){
            parseStatement();
            parseStatementList();
        }
        else{
            //epsilon production (nothing happens; skips)
        }
    }

    public void parseStatement(){
        if (verbose) System.out.println("parseStatement()");
        cst.addNode("branch", "statement");
        //switch statement WONT WORK
            if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.PRINT) parsePrintStatement();
            else if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.ASSIGNMENT_OP) parseAssignmentStatement();
            else if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.VARIABLE_TYPE) parseVarDecal();
            else if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.WHILE) parseWhileStatement();
            else if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.IF) parseIfStatement();
            else if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.L_BRACE) parseBlock();
            else System.out.println("ERROR: parseStatement() FAILED"); //This should never happen
    }

    public void parsePrintStatement(){
        if (verbose) System.out.println("parsePrintStatement()");
        cst.addNode("branch", "printStatement");
        match(Compiler808.Grammar.PRINT);
        match(Compiler808.Grammar.L_PARENTH);
        parseExpr();
        match(Compiler808.Grammar.R_PARENTH);
    }

    public void parseExpr(){
        if (verbose) System.out.println("parseExpr()");
        cst.addNode("branch", "expr");
        if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.DIGIT) parseIntExpr();
        else if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.QUOTE) parseStringExpr();
        else if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.L_PARENTH | tokenStream.get(getIndex()).token_type == Compiler808.Grammar.FALSE | tokenStream.get(getIndex()).token_type == Compiler808.Grammar.TRUE) parseBooleanExpr();
        else if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.CHAR) parseId();
        else System.out.println("ERROR: parseExpr() FAILED"); //This should never happen //TODO: implement line number and char number error
    }

    public void parseIntExpr(){
        if (verbose) System.out.println("parseIntExpr()");
        cst.addNode("branch", "intExpr");
        match(Compiler808.Grammar.DIGIT);
        if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.ADDITION_OP){
            parseIntop();
            parseExpr();
        }
    }

    public void parseIntop(){
        if (verbose) System.out.println("parseIntop()");
        cst.addNode("branch", "intop");
        match(Compiler808.Grammar.ADDITION_OP);
    }

    public void parseStringExpr(){
        if (verbose) System.out.println("parseStringExpr()");
        match(Compiler808.Grammar.QUOTE);
        parseCharList();
        match(Compiler808.Grammar.QUOTE);
    }

    public void parseCharList(){
        if (verbose) System.out.println("parseCharList()");
        cst.addNode("branch", "charList");
        if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.CHAR){
            match(Compiler808.Grammar.CHAR);
            parseCharList();
        }
        else if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.SPACE){
            match(Compiler808.Grammar.SPACE);
            parseCharList();
        }
        else{
            //epsilon production
        }
    }

    public void parseAssignmentStatement(){
        if (verbose) System.out.println("parseAssignmentStatement()");
        cst.addNode("branch", "assignmentStatement");
        match(Compiler808.Grammar.ID);
        match(Compiler808.Grammar.ASSIGNMENT_OP);
        parseExpr();
    }

    public void parseVarDecal(){
        if (verbose) System.out.println("parseVarDecal()");
        cst.addNode("branch", "varDecal");
        match(Compiler808.Grammar.VARIABLE_TYPE);
        match(Compiler808.Grammar.ID);
    }

    public void parseWhileStatement(){
        if (verbose) System.out.println("parseWhileStatement()");
        cst.addNode("branch", "whileStatement");
        match(Compiler808.Grammar.WHILE);
        parseBooleanExpr();
        parseBlock();
    }

    public void parseIfStatement(){
        if (verbose) System.out.println("parseIfStatement()");
        cst.addNode("branch", "ifStatement");
        match(Compiler808.Grammar.IF);
        parseBooleanExpr();
        parseBlock();
    }

    public void parseBooleanExpr(){
        if (verbose) System.out.println("parseBooleanExpr()");
        cst.addNode("branch", "booleanExpr");
        if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.L_PARENTH){
            match(Compiler808.Grammar.L_PARENTH);
            parseExpr();
            if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.INEQUALITY_OP){
                match(Compiler808.Grammar.INEQUALITY_OP);
            }
            else {
                match(Compiler808.Grammar.EQUALITY_OP);
            }
            parseExpr();
        }
        else {
            if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.TRUE | tokenStream.get(getIndex()).token_type == Compiler808.Grammar.FALSE){
                parseBoolOp();
            }
        }
    }

    public void parseBoolOp(){
        if (verbose) System.out.println("parseBoolOp()");
        cst.addNode("branch", "boolOp");
        if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.TRUE){
            match(Compiler808.Grammar.TRUE);
        }
        else {
            match(Compiler808.Grammar.FALSE);
        }
    }

    public void parseId(){
        if (verbose) System.out.println("parseId()");
        cst.addNode("branch", "id");
        match(Compiler808.Grammar.CHAR);
    }
}
