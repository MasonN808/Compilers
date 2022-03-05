import java.util.ArrayList;

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
    private static int index;
    private static ArrayList<Token> tokenStream;
    public static Tree cst = null;
    
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

    public static void recursiveDescent(ArrayList<Token> tokenStream){
    }

    public static void match(Lexer.Grammar expected){
        Token given = token_stream.get(getIndex());
        // Check the expected token_type is the same as the one given
         if (expected == given.token_type){
             Tree.addNode("leaf", given); //TODO: make sure it's referencing right instance of tree (don't think it is)
         }
         else{ //Error if unexpected token
             System.out.println("PARSE ERROR (Unexpected Token) --------> expected [ " + expected + "], got [" + given.token_type + "]");
         }
         addIndex();
    }

    public static void parseProgram(){
        parse_block();
        match(Lexer.Grammar.EOP);//TODO: Transfer the Grammar
    }

    public static void parseBlock(){

    }
}
