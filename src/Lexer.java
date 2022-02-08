import java.util.ArrayList;
import java.util.List;

public class Lexer {
    // List out all tokens from our predefined grammar https://www.labouseur.com/courses/compilers/grammar.pdf
    public static enum Grammar{
        EOP, L_BRACE, R_BRACE, VARIABLE_TYPE, IF, WHILE,
        PRINT, ASSIGNMENT_OP, ID, QUOTE, L_PARENTH,
        R_PARENTH, CHAR, DIGIT, EQUALITY_OP, INEQUALITY_OP,
        BOOL, ADDITION_OP
    }

    public static void add_token(ArrayList<Token> token_stream, Token token, boolean verbose){
        if (verbose){
            System.out.println("Lexer -------> found " + token.token_type + " [" + token.s + "] at "
                    + token.line_number + ", "+ token.character_number);
        }
        token_stream.add(token);
    }

    public ArrayList<Token> get_token_stream(String s, boolean verbose){
        ArrayList<Token> token_stream = new ArrayList<Token>(); // Initialize the token_stream which what will be given to the parser
        int current_index = 0; // Initialize index
        int last_index;
        StringBuilder current_string = "";
        boolean EOP_found = false;
        boolean close_quote_found = true; // to check if closed quote is found TODO: check this case """" -> is it two quotes or one quote?
        boolean L_parenth_found = false; // to check if left parenthesis is found
        boolean R_parenth_found = false; // to check if right parenthesis is found
        while(current_index < s.length() | EOP_found){
            char current_char = s.charAt(current_index);
            current_string.append(current_char); // append the current character to the string we use to find a token
            Token token; // initialize the token
            switch (current_string.toString()) {
                // USE REGULAR EXPRESSIONS HERE FROM import java.util.regex.Matcher;
                //import java.util.regex.Pattern;
                // Checking easiest tokens to identify without
                case ("$") -> {
                    token = new Token(Grammar.EOP, "$");
                    add_token(token_stream, token, verbose); //Add token to the token stream
                    EOP_found = true;  // Found EOP so exit while
                }
                case ("{") -> {
                    token = new Token(Grammar.L_BRACE, "{");
                }
                case ("}") -> {
                    token = new Token(Grammar.R_BRACE, "}");
                    add_token(token_stream, token, verbose);
                }
                case ("\"") -> {
                    token = new Token(Grammar.QUOTE, "\"");
                    add_token(token_stream, token, verbose);
                    close_quote_found = !close_quote_found;  // set close_quote_found to false
                }
                case ("+") -> {
                    token = new Token(Grammar.ADDITION_OP, "+");
                    add_token(token_stream, token, verbose);
                }
                case ("=") -> {
                    token = new Token(Grammar.ASSIGNMENT_OP, "=");
                    add_token(token_stream, token, verbose);
                }
                case ("(") -> {
                    token = new Token(Grammar.L_PARENTH, "(");
                    L_parenth_found = true;
                    add_token(token_stream, token, verbose);
                }
                case (")") -> {
                    token = new Token(Grammar.R_PARENTH, ")");
                    R_parenth_found = true;
                    add_token(token_stream, token, verbose);
                }
            }
            add_token(token_stream, token, verbose);
        }
        return null;
    }
}
