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
    public static boolean EOP_found = false; // to check if EOP is found
    public static boolean close_quote_found = true; // to check if closed quote is found TODO: check this case """" -> is it two quotes or one quote?
    public static boolean L_parenth_found = false; // to check if left parenthesis is found
    public static boolean R_parenth_found = false; // to check if right parenthesis is found
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
     * @param verbose A condition to allow the user to print extra information
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

    /**
     * This method uses our grammar to return the token of the current lexeme if it exists
     * @param current_lexeme The lexeme to be tested
     * @return boolean
     */
    public static Token get_token(String current_lexeme){
        Token token = null; // initialize the token
        switch (current_lexeme) {
            case ("$") -> {
                token = new Token(Grammar.EOP, current_lexeme); // END OF PROGRAM (EOP)
            }
            case ("{") -> {
                token = new Token(Grammar.L_BRACE, current_lexeme); // BRACES
            }
            case ("}") -> {
                token = new Token(Grammar.R_BRACE, current_lexeme);
            }
            case ("\"") -> {
                token = new Token(Grammar.QUOTE, current_lexeme); // QUOTE
            }
            case ("+") -> {
                token = new Token(Grammar.ADDITION_OP, current_lexeme); // OPERATORS
            }
            case ("=") -> {
                token = new Token(Grammar.ASSIGNMENT_OP, current_lexeme);
            }
            case ("==") -> {
                token = new Token(Grammar.EQUALITY_OP, current_lexeme);
            }
            case ("!=") -> {
                token = new Token(Grammar.INEQUALITY_OP, current_lexeme);
            }
            case ("(") -> {
                token = new Token(Grammar.L_PARENTH, current_lexeme); // PARENTHESIS
            }
            case (")") -> {
                token = new Token(Grammar.R_PARENTH, current_lexeme);
            }
            case ("false"), ("true") -> {
                token = new Token(Grammar.BOOL, current_lexeme); // BOOL
            }
            case ("if") -> {
                token = new Token(Grammar.IF, current_lexeme); // IF
            }
            case ("while") -> {
                token = new Token(Grammar.WHILE, current_lexeme); // WHILE
            }
            case ("print") -> {
                token = new Token(Grammar.PRINT, current_lexeme); // PRINT
            }
            case ("int"), ("string"), ("boolean") -> {
                token = new Token(Grammar.VARIABLE_TYPE, current_lexeme); // VARIABLE TYPES
            }
            default -> { //since you can't use regex in switch{}, we put the many cases in default
                if (current_lexeme.matches("[a-z]") & !close_quote_found) { // It is a CHAR if in quotes, else ID
                    token = new Token(Grammar.CHAR, current_lexeme); // CHAR
                }
                if (current_lexeme.matches("[a-z]")) {
                    token = new Token(Grammar.ID, current_lexeme); // CHAR
                }
                if (current_lexeme.matches("[0-9]")) {
                    token = new Token(Grammar.DIGIT, current_lexeme); // CHAR
                } else {
                    System.out.println("ERROR: NO TOKEN FOUND FOR"); //TODO: make this better if verbose==true
                }
            }
        } // end switch
        return token;
    }


    /**
     * This method returns the token stream in the form of an ArrayList<Token>
     * @param verbose A condition to allow the user to print extra information
     * @param s A string to test from the source code
     * @return boolean
     */
    public ArrayList<Token> get_token_stream(String s, boolean verbose) {
        ArrayList<Token> token_stream = new ArrayList<Token>(); // Initialize the token_stream which what will be given to the parser
        int current_index = 0; // Initialize index
        int last_index; // keep track of index of last token found and verified using longest match and rule order
        String current_string = "";

        // RULE ORDER STARTS HERE
        // initialize general token booleans
        boolean keyword;
        boolean id;
        boolean symbol;
        boolean digit;
        boolean character;
        boolean prevt_geq_cur = true; // boolean to see if previous token is greater than the current token based on rule order
        while (current_index < s.length() & !EOP_found) {
            char current_char = s.charAt(current_index); // get the character from the current index of the string
            current_string = current_string + current_char; // append the current character to the lexeme for longest match
            Token token; // initialize the token
            if (is_token(current_string)) {
                    last_index = current_index; // if there is a //TODO: CONTINUE HERE WORKING ON LONGEST MATCH AND RULE ORDER (2/8/2022)
                    Lexer.Grammar t_type = get_token(current_string).token_type;
                    if (t_type == Grammar.IF | t_type == Grammar.WHILE | t_type == Grammar.PRINT | t_type == Grammar.VARIABLE_TYPE | t_type == Grammar.BOOL) {
                        keyword = true;
                    } else if (t_type == Grammar.ID) {
                        id = true;
                    } else if (t_type == Grammar.QUOTE | t_type == Grammar.L_BRACE | t_type == Grammar.R_BRACE | t_type == Grammar.L_PARENTH | t_type == Grammar.R_PARENTH | t_type == Grammar.INEQUALITY_OP | t_type == Grammar.ADDITION_OP | t_type == Grammar.EQUALITY_OP | t_type == Grammar.EOP) {
                        symbol = true;
                        add_token(token_stream, get_token(current_string), verbose); // we can add the token since there are uniquely registered in our grammar
                        break;
                    } else if (t_type == Grammar.DIGIT) {
                        digit = true;
                        add_token(token_stream, get_token(current_string), verbose); // we can add the token since there are no digits in other tokens registered in our grammar
                        break;
                    } else if (t_type == Grammar.CHAR) {
                        character = true;// TODO: make sure character is registered instead of ID. That is, characters are in quotes.
                    }
                    if (true){

                    }
//                switch (current_string) {
//                    case ("$") -> {
//                        token = new Token(Grammar.EOP, current_string); // END OF PROGRAM (EOP)
//                        add_token(token_stream, token, verbose); //Add token to the token stream
//                        EOP_found = true;  // Found EOP so exit while
//                    }
//                    case ("{") -> {
//                        token = new Token(Grammar.L_BRACE, current_string); // BRACES
//                        add_token(token_stream, token, verbose);
//                        last_index = current_index;
//                    }
//                    case ("}") -> {
//                        token = new Token(Grammar.R_BRACE, current_string);
//                        add_token(token_stream, token, verbose);
//                    }
//                    case ("\"") -> {
//                        token = new Token(Grammar.QUOTE, current_string); // QUOTE
//                        add_token(token_stream, token, verbose);
//                        close_quote_found = !close_quote_found;  // set close_quote_found to false
//                    }
//                    case ("+") -> {
//                        token = new Token(Grammar.ADDITION_OP, current_string); // OPERATORS
//                        add_token(token_stream, token, verbose);
//                    }
//                    case ("=") -> {
//                        token = new Token(Grammar.ASSIGNMENT_OP, current_string);
//                        add_token(token_stream, token, verbose);
//                    }
//                    case ("==") -> {
//                        token = new Token(Grammar.EQUALITY_OP, current_string);
//                        add_token(token_stream, token, verbose);
//                    }
//                    case ("!=") -> {
//                        token = new Token(Grammar.INEQUALITY_OP, current_string);
//                        add_token(token_stream, token, verbose);
//                    }
//                    case ("(") -> {
//                        token = new Token(Grammar.L_PARENTH, current_string); // PARENTHESIS
//                        L_parenth_found = true;
//                        add_token(token_stream, token, verbose);
//                    }
//                    case (")") -> {
//                        token = new Token(Grammar.R_PARENTH, current_string);
//                        R_parenth_found = true;
//                        add_token(token_stream, token, verbose);
//                    }
//                    case ("false"), ("true") -> {
//                        token = new Token(Grammar.BOOL, current_string); // BOOL
//                        add_token(token_stream, token, verbose);
//                    }
//                    case ("if") -> {
//                        token = new Token(Grammar.IF, current_string); // IF
//                        add_token(token_stream, token, verbose);
//                    }
//                    case ("while") -> {
//                        token = new Token(Grammar.WHILE, current_string); // WHILE
//                        add_token(token_stream, token, verbose);
//                    }
//                    case ("print") -> {
//                        token = new Token(Grammar.PRINT, current_string); // PRINT
//                        add_token(token_stream, token, verbose);
//                    }
//                    case ("int"), ("string"), ("boolean") -> {
//                        token = new Token(Grammar.VARIABLE_TYPE, current_string); // VARIABLE TYPES
//                        add_token(token_stream, token, verbose);
//                    }
//                    default -> { //since you can't use regex in switch{}, we put the many cases in default
//                        if (current_string.matches("[a-z]") & !close_quote_found) { // It is a CHAR if in quotes, else ID
//                            token = new Token(Grammar.CHAR, current_string); // CHAR
//                            add_token(token_stream, token, verbose);
//                        }
//                        if (current_string.matches("[a-z]")) {
//                            token = new Token(Grammar.ID, current_string); // CHAR
//                            add_token(token_stream, token, verbose);
//                        }
//                        if (current_string.matches("[0-9]")) {
//                            token = new Token(Grammar.DIGIT, current_string); // CHAR
//                            add_token(token_stream, token, verbose);
//                        } else {
//                            System.out.println("ERROR: NO TOKEN FOUND FOR"); //TODO: make this better if verbose==true
//                        }
//                    }
//                } // end switch
            } // end if
        } // end while
        return token_stream;
    } // end get_token_stream
} // end class

//Copy of Grammar
//public static enum Grammar {
//    , CHAR, DIGIT,
//
//}