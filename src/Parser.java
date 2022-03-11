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
    public enum Grammar {
        EOP, L_BRACE, R_BRACE, VARIABLE_TYPE, IF, WHILE,
        PRINT, ASSIGNMENT_OP, ID, QUOTE, L_PARENTH,
        R_PARENTH, CHAR, DIGIT, EQUALITY_OP, INEQUALITY_OP,
        BOOL, ADDITION_OP, SPACE
    }

    private static int index;
    private static ArrayList<Token> tokenStream;
    public static Tree cst = null;
    //From https://stackoverflow.com/questions/7604814/best-way-to-format-multiple-or-conditions-in-an-if-statement
    //Makes the lengthy boolean statement more efficient
    private static final Set<Compiler808.Grammar> statementListValues = new HashSet<Compiler808.Grammar>(Arrays.asList(
            Compiler808.Grammar.PRINT, Compiler808.Grammar.ASSIGNMENT_OP, Compiler808.Grammar.VARIABLE_TYPE/*CASE: VarDecal*/,
            Compiler808.Grammar.WHILE, Compiler808.Grammar.IF, Compiler808.Grammar.L_BRACE/*CASE: Block*/));



    // Constructor to set new token_stream for parsing
    public Parser(ArrayList<Token> tokenStream){
        Parser.tokenStream = tokenStream;
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
             System.out.println("PARSE ERROR (Unexpected Token) --------> expected [ " + expected + "], got [" + given.token_type + "]"); //TODO: maybe use logger.log() instead
         }
         addIndex();
    }

    public static void parseProgram(){
        Tree.addNode("root", "goal");
        parseBlock();
        match(Compiler808.Grammar.EOP);
    }

    public static void parseBlock(){
        Tree.addNode("branch", "block");
        match(Compiler808.Grammar.L_BRACE);
        parseStatementList();
        match(Compiler808.Grammar.R_BRACE);
    }

    public static void parseStatementList(){
        Tree.addNode("branch", "statementList");
        if(statementListValues.contains(tokenStream.get(getIndex()).token_type)){
            parseStatement();
            parseStatementList();
        }
        else{
            //epsilon production (nothing happens; skips)
        }
    }

    public static void parseStatement(){
        Tree.addNode("branch", "statement");
        //switch statement WONT WORK
            if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.PRINT) parsePrintStatement();
            else if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.ASSIGNMENT_OP) parseAssignmentStatement();
            else if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.VARIABLE_TYPE) parseVarDecal();
            else if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.WHILE) parseWhileSatement();
            else if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.IF) parseIfStatement();
            else if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.L_BRACE) parseBlock();
            else System.out.println("ERROR: parseStatement() FAILED"); //This should never happen
    }

    public static void parsePrintStatement(){
        Tree.addNode("branch", "printStatement");
        match(Compiler808.Grammar.PRINT);
        match(Compiler808.Grammar.L_PARENTH);
        parseExpr();
        match(Compiler808.Grammar.R_PARENTH);
    }

    public static void parseExpr(){
        Tree.addNode("branch", "expr");
        if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.DIGIT) parseIntExpr();
        else if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.QUOTE) parseStringExpr();
        else if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.L_PARENTH | tokenStream.get(getIndex()).token_type == Compiler808.Grammar.BOOL) parseBooleanExpr();
        else if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.CHAR) parseId();
        else System.out.println("ERROR: parseExpr() FAILED"); //This should never happen //TODO: implement line number and char number error
    }

    public static void parseIntExpr(){
        Tree.addNode("branch", "intExpr");
        match(Compiler808.Grammar.DIGIT);
        if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.ADDITION_OP){
            parseIntop();
            parseExpr();
        }
    }

    public static void parseIntop(){
        Tree.addNode("branch", "intop");
        match(Compiler808.Grammar.ADDITION_OP);
    }

    public static void parseStringExpr(){
        match(Compiler808.Grammar.QUOTE);
        parseCharList();
        match(Compiler808.Grammar.QUOTE);
    }

    public static void parseCharList(){
        Tree.addNode("branch", "charList");
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

    public static void parseAssignmentStatement(){
        Tree.addNode("branch", "assignmentStatement");
        match(Compiler808.Grammar.ID);
        match(Compiler808.Grammar.ASSIGNMENT_OP);
        parseExpr();
    }

    public static void parseVarDecal(){
        Tree.addNode("branch", "varDecal");
        match(Compiler808.Grammar.VARIABLE_TYPE);
        match(Compiler808.Grammar.ID);
    }

    public static void parseWhileSatement(){
        Tree.addNode("branch", "whileStatement");
        match(Compiler808.Grammar.WHILE);
        parseBooleanExpr();
        parseBlock();
    }

    public static void parseIfStatement(){
        Tree.addNode("branch", "ifStatement");
        match(Compiler808.Grammar.IF);
        parseBooleanExpr();
        parseBlock();
    }

    public static void parseBooleanExpr(){
        Tree.addNode("branch", "booleanExpr");
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
            if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.BOOL)
        }
    }

    public static void parseId(){
        Tree.addNode("branch", "id");
    }
}
