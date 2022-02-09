import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
/**
 * <h1>Lexical Analysis (Part 1)</h1>
 * We design a lexical analyzer ( or scanner, lexer, ...) to produce a stream
 * of tokens to be fed to the parser (Part 2)
 * <p>
 * <b>Note:</b> Still in progress
 *
 * @author  Mason Nakamura
 * @since   02-08-2022
 */
public class Lexer {
    // List out all tokens from our predefined grammar https://www.labouseur.com/courses/compilers/grammar.pdf
    public static enum Grammar {
        EOP, L_BRACE, R_BRACE, VARIABLE_TYPE, IF, WHILE,
        PRINT, ASSIGNMENT_OP, ID, QUOTE, L_PARENTH,
        R_PARENTH, CHAR, DIGIT, EQUALITY_OP, INEQUALITY_OP,
        BOOL, ADDITION_OP
    }

    /**
     * This method prints out various tokens depending on the token the lexer identifies
     * @param token_stream The stream of tokens the lexer already identified
     * @param token  The token the lexer identifies after longest match and rule order
     * @param verbose A condition to allow the user to print more information on the lexer
     */
    public static void add_token(ArrayList<Token> token_stream, Token token, boolean verbose) {
        if (verbose) {
            System.out.println("Lexer -------> found " + token.token_type + " [" + token.s + "] at "
                    + token.line_number + ", " + token.character_number);
        }
        token_stream.add(token);
    }

    /**
     * This method uses our grammar to determine whether a string is identified as a token
     * @param current_lexeme The lexeme to be tested
     * @return boolean
     */
    public static boolean is_token(String current_lexeme){
        boolean isToken = false;
        switch (current_lexeme) {
            // USE REGULAR EXPRESSIONS HERE FROM import java.util.regex.Matcher;
            //import java.util.regex.Pattern;
            // Checking easiest tokens to identify without
            case ("$"), ("{") , ("}"), ("\""), ("+"), ("="), ("=="), ("!="), ("("), (")"), // Check for a lot of different tokens
                ("false"), ("true"), ("if"), ("while"), ("print"), ("int"), ("string"), ("boolean")-> {
                isToken = true;
            }
            default -> { //since you can't use regex in switch{}, we put the many cases in default
                if (current_lexeme.matches("[a-z]") | current_lexeme.matches("[0-9]")) { // Check if CHAR OR ID OR DIGIT
                    isToken = true;
                }
            }
        } // end switch
        return isToken;
    }

    public ArrayList<Token> get_token_stream(String s, boolean verbose) {
        ArrayList<Token> token_stream = new ArrayList<Token>(); // Initialize the token_stream which what will be given to the parser
        int current_index = 0; // Initialize index
        int last_index;
        String current_string = "";
        boolean EOP_found = false; // TODO: move these variables out of scope/block
        boolean close_quote_found = true; // to check if closed quote is found TODO: check this case """" -> is it two quotes or one quote?
        boolean L_parenth_found = false; // to check if left parenthesis is found
        boolean R_parenth_found = false; // to check if right parenthesis is found
        while (current_index < s.length() | EOP_found) {
            char current_char = s.charAt(current_index);
            current_string = current_string + current_char; // append the current character to the string we use to find a token
            Token token; // initialize the token
            switch (current_string) {
                // USE REGULAR EXPRESSIONS HERE FROM import java.util.regex.Matcher;
                //import java.util.regex.Pattern;
                // Checking easiest tokens to identify without
                case ("$") -> { // TODO: maybe change the String s in Token() to current_string to be more general
                    token = new Token(Grammar.EOP, "$"); // END OF PROGRAM (EOP)
                    add_token(token_stream, token, verbose); //Add token to the token stream
                    EOP_found = true;  // Found EOP so exit while
                }
                case ("{") -> {
                    token = new Token(Grammar.L_BRACE, "{"); // BRACES
                    add_token(token_stream, token, verbose);
                    last_index = current_index;
                }
                case ("}") -> {
                    token = new Token(Grammar.R_BRACE, "}");
                    add_token(token_stream, token, verbose);
                }
                case ("\"") -> {
                    token = new Token(Grammar.QUOTE, "\""); // QUOTE
                    add_token(token_stream, token, verbose);
                    close_quote_found = !close_quote_found;  // set close_quote_found to false
                }
                case ("+") -> {
                    token = new Token(Grammar.ADDITION_OP, "+"); // OPERATORS
                    add_token(token_stream, token, verbose);
                }
                case ("=") -> {
                    token = new Token(Grammar.ASSIGNMENT_OP, "=");
                    add_token(token_stream, token, verbose);
                }
                case ("==") -> {
                    token = new Token(Grammar.EQUALITY_OP, "==");
                    add_token(token_stream, token, verbose);
                }
                case ("!=") -> {
                    token = new Token(Grammar.INEQUALITY_OP, "!=");
                    add_token(token_stream, token, verbose);
                }
                case ("(") -> {
                    token = new Token(Grammar.L_PARENTH, "("); // PARENTHESIS
                    L_parenth_found = true;
                    add_token(token_stream, token, verbose);
                }
                case (")") -> {
                    token = new Token(Grammar.R_PARENTH, ")");
                    R_parenth_found = true;
                    add_token(token_stream, token, verbose);
                }
                case ("false") -> {
                    token = new Token(Grammar.BOOL, "false"); // BOOL
                    add_token(token_stream, token, verbose);
                }
                case ("true") -> {
                    token = new Token(Grammar.BOOL, "true");
                    add_token(token_stream, token, verbose);
                }
                case ("if") -> {
                    token = new Token(Grammar.IF, "if"); // IF
                    add_token(token_stream, token, verbose);
                }
                case ("while") -> {
                    token = new Token(Grammar.WHILE, "while"); // WHILE
                    add_token(token_stream, token, verbose);
                }
                case ("print") -> {
                    token = new Token(Grammar.PRINT, "print"); // PRINT
                    add_token(token_stream, token, verbose);
                }
                case ("int") -> {
                    token = new Token(Grammar.VARIABLE_TYPE, "int"); // VARIABLE TYPES
                    add_token(token_stream, token, verbose);
                }
                case ("string") -> {
                    token = new Token(Grammar.VARIABLE_TYPE, "string");
                    add_token(token_stream, token, verbose);
                }
                case ("boolean") -> {
                    token = new Token(Grammar.VARIABLE_TYPE, "boolean");
                    add_token(token_stream, token, verbose);
                }
                default -> { //since you can't use regex in switch{}, we put the many cases in default
                    if (current_string.matches("[a-z]") & !close_quote_found) { // It is a CHAR if in quotes, else ID
                        token = new Token(Grammar.CHAR, current_string); // CHAR
                        add_token(token_stream, token, verbose);
                    }
                    if (current_string.matches("[a-z]")) {
                        token = new Token(Grammar.ID, current_string); // CHAR
                        add_token(token_stream, token, verbose);
                    }
                    if (current_string.matches("[0-9]")) {
                        token = new Token(Grammar.DIGIT, current_string); // CHAR
                        add_token(token_stream, token, verbose);
                    }
                    else{
                        System.out.println("ERROR: NO TOKEN FOUND FOR"); //TODO: make this better if verbose==true
                    }

                }
            } // end switch
        } // end while
        return null;
    } // end get_token_stream
} // end class
