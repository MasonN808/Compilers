import java.lang.reflect.AnnotatedType;
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

    int current_index = 0; // Initialize index
    int last_index = 0; // keep track of index of last token found and verified using longest match and rule order

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

    public static void lex_error(String not_lexeme, int line_number, int character_number){
        System.out.println("Lexer [ERROR] -------> found unidentified token [" + not_lexeme + "] at "
                + line_number + ", " + character_number);
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
     * @return ArrayList
     */
    public ArrayList<Token> get_token_stream(String s, boolean verbose, boolean output) {
        ArrayList<Token> token_stream = new ArrayList<Token>(); // Initialize the token_stream which what will be given to the parser
        String current_string = "";

        // RULE ORDER STARTS HERE
        // initialize general token booleans
        boolean keyword = false;
        boolean id = false;
        boolean symbol = false;
        boolean digit = false;
        boolean character = false;
//        boolean[] rule_order = {keyword, id, symbol, digit, character}; //initialize rule order
//        boolean[] rule_order2 = {keyword, id, symbol, digit, character};
        int[] rule_order = new int[2];
        int k = 0;
//        boolean prevT_geq_cur = true; // boolean to see if previous token is greater than the current token based on rule order
        Token prev_token = null;
        Token current_token = null;
        Lexer.Grammar t_type;
        while (current_index < s.length() & !EOP_found) {
            char current_char = s.charAt(current_index); // get the character from the current index of the string
            current_string += current_char; // append the current character to the lexeme for longest match
            String str_current_char = String.valueOf(current_char);
            //check for spaces, indents, and line breaks as boundaries //TODO: Might need to change this to somewhere else in the code



            //check for comments
            if (str_current_char.equals("/")){
                int temp_current_index = current_index;
                temp_current_index += 1;
                char temp_current_char = s.charAt(temp_current_index);
                String temp_current_string = current_string;
                temp_current_string += temp_current_char;
                if (temp_current_string.equals("/*")){
                    while(temp_current_index < s.length() - current_index & !String.valueOf(s.charAt(temp_current_index)).equals("*")){ //check for end comment
                        temp_current_index += 1;
                    }
                    if (!String.valueOf(s.charAt(temp_current_index)).equals("*")){
                        System.out.println("WARNING: unterminated comment");
                    }
                    else{
                        current_index += temp_current_index + 2; // go to next character out of comment
                    }
                }
                else{
                    //TODO: LEX ERROR
                }
            }
            if (is_token(str_current_char)){ // Check if current character is a token
                t_type = get_token(str_current_char).token_type;
                if (t_type == Grammar.EOP){
                    EOP_found = true;
                }
                if (t_type == Grammar.ASSIGNMENT_OP & last_index-current_index < 2){  // CASE: for when the current character is an = sign and checking if next character is a == operator
                    // Create temporary variables to check if its an assignment operator or an equality operator
                    int temp_current_index = current_index;
                    temp_current_index += 1;
                    char temp_current_char = s.charAt(temp_current_index);
                    String temp_current_string = current_string;
                    temp_current_string += temp_current_char;
                    if (get_token(temp_current_string).token_type == Grammar.EQUALITY_OP){
                        add_token(token_stream, get_token(temp_current_string), verbose);
                        current_index = last_index; // reset index
                        current_index += 2; // 2 because we temporarily went ahead one index
                        prev_token = null; // just in case
                        current_token = null; // just in case
                        current_string = ""; // reset string
                    }
                }
                if (t_type == Grammar.ASSIGNMENT_OP & last_index-current_index > 2){  // the boundary
                    current_index = last_index; // reset index
                    current_index += 1;
                    if (prev_token == null){
                        add_token(token_stream, current_token, verbose);
                    }
                    else{
                        add_token(token_stream, prev_token, verbose);
                    }
                    prev_token = null; // just in case
                    current_token = null; // just in case
                    current_string = ""; // reset string
                }
            }


            if (is_token(current_string)) {
                 // get token type
                current_token = get_token(current_string);
                t_type = get_token(current_string).token_type;

                if (t_type == Grammar.ID) {
                    rule_order[k] = 1;
                    k += 1;
                } else if (t_type == Grammar.IF | t_type == Grammar.WHILE | t_type == Grammar.PRINT | t_type == Grammar.VARIABLE_TYPE | t_type == Grammar.BOOL) {
                    rule_order[k] = 0;
                    k += 1;
//                    add_token(token_stream, get_token(current_string), verbose); // we can add the token since they are uniquely registered in our grammar and are not subsets of any other lexemes in our grammar
//                    current_index = last_index;

                } else if (t_type == Grammar.QUOTE | t_type == Grammar.L_BRACE | t_type == Grammar.R_BRACE | t_type == Grammar.L_PARENTH | t_type == Grammar.R_PARENTH | t_type == Grammar.INEQUALITY_OP | t_type == Grammar.ADDITION_OP | t_type == Grammar.EQUALITY_OP) {
                    rule_order[k] = 2;
                    k += 1;
//                    add_token(token_stream, get_token(current_string), verbose); // we can add the token since there are uniquely registered in our grammar
//                    current_index = last_index;

                } else if (t_type == Grammar.DIGIT) {
                    rule_order[k] = 3;
                    k += 1;
//                    add_token(token_stream, get_token(current_string), verbose); // we can add the token since there are no digits in other tokens registered in our grammar
//                    current_index = last_index;

                } else if (t_type == Grammar.CHAR) {
                    rule_order[k] = 4;// TODO: make sure character is registered instead of ID. That is, characters are in quotes.
                    k += 1;
                }
                else System.out.println("ERROR: lexeme not recognized as a token"); // Should not occur
//                for (int i = 0; i < rule_order.length; i++){
//                    if(rule_order[i]){
//                        int first_element = i;
//                    }
//                }
                if (rule_order[0] > rule_order[1]) {
                    rule_order[0] = rule_order[1];
                    prev_token = current_token; // replace token pointer
                }
                rule_order[1] = -1;


//                if (id & keyword){
//                    last_index = current_index;// TODO: incorporate line number and character number in add_token() for log
//                    add_token(token_stream, get_token(current_string), verbose);
//                }

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
            current_index += 1;
        } // end while
        return token_stream;
    } // end get_token_stream
} // end class

//Copy of Grammar
//public static enum Grammar {
//    , CHAR, DIGIT,
//
//}