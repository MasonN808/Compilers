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
//    private static final Set<Lexer.Grammar> statementListValues = new HashSet<Lexer.Grammar>(Arrays.asList(
//            Lexer.Grammar.PRINT, Lexer.Grammar.ASSIGNMENT_OP, Lexer.Grammar.VARIABLE_TYPE/*CASE: VarDecal*/,
//            Lexer.Grammar.WHILE, Lexer.Grammar.IF, Lexer.Grammar.L_BRACE/*CASE: Block*/));
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
//    public static void match(Lexer.Grammar expected){
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
//        match(Lexer.Grammar.EOP);
//    }
//
//    public static void parseBlock(){
//        Tree.addNode("branch", "block");
//        match(Lexer.Grammar.L_BRACE);
//        parseStatementList();
//        match(Lexer.Grammar.R_BRACE);
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
//            case (Lexer.Grammar.PRINT) -> parsePrintStatement();
//            case (Lexer.Grammar.ASSIGNMENT_OP) -> parseAssignmentStatement();
//            case (Lexer.Grammar.VARIABLE_TYPE) -> parseVarDecal();
//            case (Lexer.Grammar.WHILE) -> parseWhileSatement();
//            case (Lexer.Grammar.IF) -> parseIfStatement();
//            case (Lexer.Grammar.L_BRACE) -> parseBlock();
//            default -> System.out.println("ERROR: parseStatement() FAILED"); //This should never happen
//        }
//    }
//
//    public static void parsePrintStatement(){
//        Tree.addNode("branch", "printStatement");
//        match(Lexer.Grammar.PRINT);
//        match(Lexer.Grammar.L_PARENTH);
//        parseExpr();
//        match(Lexer.Grammar.R_PARENTH);
//    }
//
//    public static void parseExpr(){
//        Tree.addNode("branch", "expr");
//        switch (tokenStream.get(getIndex()).token_type){
//            case (Lexer.Grammar.DIGIT) -> parseIntExpr();
//            case (Lexer.Grammar.QUOTE) -> parseStringExpr();
//            case (Lexer.Grammar.L_PARENTH),(Lexer.Grammar.BOOL)  -> parseBooleanExpr();
//            case (Lexer.Grammar.CHAR) -> parseId();
//            default -> System.out.println("ERROR: parseExpr() FAILED"); //This should never happen //TODO: implement line number and char number error
//        }
//    }
//
//    public static void parseIntExpr(){
//        Tree.addNode("branch", "intExpr");
//        match(Lexer.Grammar.DIGIT);
//        if (tokenStream.get(getIndex()).token_type == Lexer.Grammar.ADDITION_OP){
//            parseIntop();
//            parseExpr();
//        }
//    }
//
//    public static void parseIntop(){
//        Tree.addNode("branch", "intop");
//        match(Lexer.Grammar.ADDITION_OP);
//    }
//
//    public static void parseStringExpr(){
//        match(Lexer.Grammar.QUOTE);
//        parseCharList();
//        match(Lexer.Grammar.QUOTE);
//    }
//
//    public static void parsesCharList(){
//
//    }
//
//}
