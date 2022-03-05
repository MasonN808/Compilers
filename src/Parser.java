import java.lang.reflect.Array;
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
    public static int index = 0;
    public static ArrayList<Token> token_stream;
    public static Tree cst;
    
    // Constructor to set new token_stream for parsing
    public Parser(ArrayList<Token> token_stream){
        Parser.token_stream = token_stream;
        Parser.cst = new Tree();
    }
    public static void recursive_descent(ArrayList<Token> token_stream){
    }
    public static void match(Token expected){
        Token given = token_stream.get(index);
        // Check the expected token_type is the same as the one given
         if (expected.token_type == given.token_type){
             Tree.add_node("leaf", given); //TODO: make sure it's referencing right instance of tree (don't think it is)
         }
         else{ //Error if unexpected token
             System.out.println("PARSE ERROR (Unexpected Token) --------> expected [ " + expected.token_type + "], got [" + given.token_type + "]");
         }
    }
    public static void parse_program(){
        parse_block();
        match(EOP);//TODO: Transfer the Grammar
    }
}
