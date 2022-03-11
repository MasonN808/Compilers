//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashSet;
//import java.util.Set;
//
///**
// * <h1>Parser (Part 2)</h1>
// * We design a Parser to produce a Concrete Syntax Tree (CST)
// * using recursive descent parsing
// * <p>
// * <b>Note:</b> Still in progress
// *
// * @author  Mason Nakamura
// * @since   03-01-2022
// */
//public class Parser {
//    private static int index;
//    private static ArrayList<Token> tokenStream;
//    public static Tree cst = null;
//    //From https://stackoverflow.com/questions/7604814/best-way-to-format-multiple-or-conditions-in-an-if-statement
//    //Makes the lengthy boolean statement more efficient
//    private static final Set<Compiler808.Grammar> statementListValues = new HashSet<Compiler808.Grammar>(Arrays.asList(
//            Compiler808.Grammar.PRINT, Compiler808.Grammar.ASSIGNMENT_OP, Compiler808.Grammar.VARIABLE_TYPE/*CASE: VarDecal*/,
//            Compiler808.Grammar.WHILE, Compiler808.Grammar.IF, Compiler808.Grammar.L_BRACE/*CASE: Block*/));
//
//
//
//    // Constructor to set new token_stream for parsing
//    public Parser(ArrayList<Token> tokenStream){
//        Parser.tokenStream = tokenStream;
//        cst = new Tree();
//    }
//
//    private static int getIndex(){
//        return index;
//    }
//
//    private static void addIndex(){
//        index = index + 1;
//    }
//
//    public static void match(Compiler808.Grammar expected){
//        Token given = tokenStream.get(getIndex());
//        // Check the expected token_type is the same as the one given
//         if (expected == given.token_type){
//             Tree.addNode("leaf", given.token_type.toString());
//         }
//         else{ //Error if unexpected token
//             System.out.println("PARSE ERROR (Unexpected Token) --------> expected [ " + expected + "], got [" + given.token_type + "]"); //TODO: maybe use logger.log() instead
//         }
//         addIndex();
//    }
//
//    public static void parseProgram(){
//        Tree.addNode("root", "goal");
//        parseBlock();
//        match(Compiler808.Grammar.EOP);
//    }
//
//    public static void parseBlock(){
//        Tree.addNode("branch", "block");
//        match(Compiler808.Grammar.L_BRACE);
//        parseStatementList();
//        match(Compiler808.Grammar.R_BRACE);
//    }
//
//    public static void parseStatementList(){
//        Tree.addNode("branch", "statementList");
//        if(statementListValues.contains(tokenStream.get(getIndex()).token_type)){
//            parseStatement();
//            parseStatementList();
//        }
//        else{
//            //epsilon production (nothing happens; skips)
//        }
//    }
//
//    public static void parseStatement(){
//        Tree.addNode("branch", "statement");
//        switch(tokenStream.get(getIndex()).token_type){
//            case (PRINT) -> parsePrintStatement();
//            case (Compiler808.Grammar.ASSIGNMENT_OP) -> parseAssignmentStatement();
//            case (Compiler808.Grammar.VARIABLE_TYPE) -> parseVarDecal();
//            case (Compiler808.Grammar.WHILE) -> parseWhileSatement();
//            case (Compiler808.Grammar.IF) -> parseIfStatement();
//            case (Compiler808.Grammar.L_BRACE) -> parseBlock();
//            default -> System.out.println("ERROR: parseStatement() FAILED"); //This should never happen
//        }
//    }
//
//    public static void parsePrintStatement(){
//        Tree.addNode("branch", "printStatement");
//        match(Compiler808.Grammar.PRINT);
//        match(Compiler808.Grammar.L_PARENTH);
//        parseExpr();
//        match(Compiler808.Grammar.R_PARENTH);
//    }
//
//    public static void parseExpr(){
//        Tree.addNode("branch", "expr");
//        switch (tokenStream.get(getIndex()).token_type){
//            case (Compiler808.Grammar.DIGIT) -> parseIntExpr();
//            case (Compiler808.Grammar.QUOTE) -> parseStringExpr();
//            case (Compiler808.Grammar.L_PARENTH),(Compiler808.Grammar.BOOL)  -> parseBooleanExpr();
//            case (Compiler808.Grammar.CHAR) -> parseId();
//            default -> System.out.println("ERROR: parseExpr() FAILED"); //This should never happen //TODO: implement line number and char number error
//        }
//    }
//
//    public static void parseIntExpr(){
//        Tree.addNode("branch", "intExpr");
//        match(Compiler808.Grammar.DIGIT);
//        if (tokenStream.get(getIndex()).token_type == Compiler808.Grammar.ADDITION_OP){
//            parseIntop();
//            parseExpr();
//        }
//    }
//
//    public static void parseIntop(){
//        Tree.addNode("branch", "intop");
//        match(Compiler808.Grammar.ADDITION_OP);
//    }
//
//    public static void parseStringExpr(){
//        match(Compiler808.Grammar.QUOTE);
//        parseCharList();
//        match(Compiler808.Grammar.QUOTE);
//    }
//
//    public static void parseCharList(){
//
//    }
//
//    public static void parseAssignmentStatement(){
//
//    }
//
//    public static void parseVarDecal(){
//
//    }
//
//    public static void parseWhileSatement(){
//
//    }
//    public static void parseIfStatement(){
//
//    }
//    public static void parseBooleanExpr(){
//
//    }
//    public static void parseId(){
//
//    }
//}
