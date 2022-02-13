import java.util.ArrayList;
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
    public static boolean close_quote_found = true; // to check if closed quote is found
    public static int num_extra_parenth_found = 0;
    public static int num_extra_brace_found = 0;
    public static int num_extra_comment_found = 0;

    public boolean L_brace_found = false;
    public boolean L_parenth_found = false;

    public static int num_errors = 0;
    public static int num_warnings = 0;
    public static int program_num = 0;

    public static int current_index = 0; // Initialize index
    public static int printed_last_index = 0; // The index to be printed
    public static int printed_current_index = 0; // The index to be printed
    public static int last_index = 0; // keep track of index of last token found and verified using longest match and rule order
    public static int current_line = 1;
    public Token current_token;
    public boolean debug = false; // For debugging

    // List out all tokens from our predefined grammar https://www.labouseur.com/courses/compilers/grammar.pdf
    public enum Grammar {
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
            System.out.println("Lexer -------> " + token.token_type + " [ " + token.s + " ] at "
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
                ("false"), ("true"), ("if"), ("while"), ("print"), ("int"), ("string"), ("boolean")-> isToken = true;
            default -> { //since you can't use regex in switch{}, we put the many cases in default
                if (current_lexeme.matches("[a-z]") | current_lexeme.matches("[0-9]")) { // Check if CHAR OR ID OR DIGIT
                    isToken = true;
                }
            }
        } // end switch
        return isToken;
    }

    public static void lex_error(String not_lexeme, int line_number, int character_number){
        System.out.println("Lexer [ERROR] -------> unidentified token [" + not_lexeme + "] at "
                + line_number + ", " + character_number);
        num_errors += 1;
    }

    /**
     * This method uses our grammar to return the token of the current lexeme if it exists
     * @param current_lexeme The lexeme to be tested
     * @return boolean
     */
    public static Token get_token(String current_lexeme){
        Token token = null; // initialize the token
        switch (current_lexeme) {
            case ("$") -> token = new Token(Grammar.EOP, current_lexeme, current_line, printed_current_index); // END OF PROGRAM (EOP)
            case ("{") -> token = new Token(Grammar.L_BRACE, current_lexeme, current_line, printed_current_index); // BRACES
            case ("}") -> token = new Token(Grammar.R_BRACE, current_lexeme, current_line, printed_current_index);
            case ("\"") -> token = new Token(Grammar.QUOTE, current_lexeme, current_line, printed_current_index); // QUOTE
            case ("+") -> token = new Token(Grammar.ADDITION_OP, current_lexeme, current_line, printed_current_index); // OPERATORS
            case ("=") -> token = new Token(Grammar.ASSIGNMENT_OP, current_lexeme, current_line, printed_current_index);
            case ("==") -> token = new Token(Grammar.EQUALITY_OP, current_lexeme, current_line, printed_current_index);
            case ("!=") -> token = new Token(Grammar.INEQUALITY_OP, current_lexeme, current_line, printed_current_index);
            case ("(") -> token = new Token(Grammar.L_PARENTH, current_lexeme, current_line, printed_current_index); // PARENTHESIS
            case (")") -> token = new Token(Grammar.R_PARENTH, current_lexeme, current_line, printed_current_index);
            case ("false"), ("true") -> token = new Token(Grammar.BOOL, current_lexeme, current_line, printed_current_index); // BOOL
            case ("if") -> token = new Token(Grammar.IF, current_lexeme, current_line, printed_current_index); // IF
            case ("while") -> token = new Token(Grammar.WHILE, current_lexeme, current_line, printed_current_index); // WHILE
            case ("print") -> token = new Token(Grammar.PRINT, current_lexeme, current_line, printed_current_index); // PRINT
            case ("int"), ("string"), ("boolean") -> token = new Token(Grammar.VARIABLE_TYPE, current_lexeme, current_line, printed_current_index); // VARIABLE TYPES
            default -> { //since you can't use regex in switch{}, we put the many cases in default
                if (current_lexeme.matches("[a-z]") & !close_quote_found) { // It is a CHAR if in quotes, else ID
                    token = new Token(Grammar.CHAR, current_lexeme, current_line, printed_current_index); // CHAR
                }
                else if (current_lexeme.matches("[a-z]")) {
//                    System.out.println("quote_closed :" + close_quote_found); //DEBUGGING
                    token = new Token(Grammar.ID, current_lexeme, current_line, printed_current_index); // CHAR
                }
                else if (current_lexeme.matches("[0-9]")) {
                    token = new Token(Grammar.DIGIT, current_lexeme, current_line, printed_current_index); // CHAR
                } else {
                    System.out.println("ERROR: NO TOKEN FOUND FOR " + current_line + ", " + printed_current_index); //TODO: make this better if verbose==true
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
    public ArrayList<Token> get_token_stream(String s, boolean verbose) {
        ArrayList<Token> token_stream = new ArrayList<Token>(); // Initialize the token_stream which what will be given to the parser
        String current_string = "";

        Token prev_token = null;
        Lexer.Grammar t_type;

        System.out.println("------------------------------------------------------------");
        System.out.println("LEXING PROGRAM " + program_num);
        while (current_index < s.length() & !EOP_found) {
//            System.out.println(current_index);
            char current_char = s.charAt(current_index); // get the character from the current index of the string
            current_string += current_char; // append the current character to the lexeme for longest match
            String str_current_char = String.valueOf(current_char);
            if(debug) {
                System.out.println("Current_String: [" + str_current_char + "] at " + current_line + ", " + current_index);  // DEBUGGING
            }
//            System.out.println("Current_String: " + current_string + ", " + current_index);  // DEBUGGING
//            System.out.println("size of current string: " +  current_string.length());
            // account for spaces in quotes //TODO: make sure this works in unsmallerized txt file

            if (str_current_char.equals(" ") & !close_quote_found & current_string.length() == 1){
                Token token = new Token(Grammar.CHAR, " ", current_line, current_char); // create space character
                add_token(token_stream, token, verbose);
                printed_current_index += 1;
            }

            if (str_current_char.matches("[ ]") & current_string.length() == 1){

//                System.out.println("Found space:" + current_line + ", " + current_index);
//                current_index += 4; // since tab is 5 characters (maybe not)
                printed_current_index += 1;
                current_string = "";
            } else if (str_current_char.matches("[ ]") & current_string.length() > 1){
//                System.out.println("Found space");
                current_index = last_index; // reset index
                printed_current_index = printed_last_index;
                if (prev_token == null){
                    add_token(token_stream, current_token, verbose);
                }
                else{
                    add_token(token_stream, prev_token, verbose);
                }
                current_index += current_token.s.length() - 1;
                printed_current_index += current_token.s.length();
                current_string = "";
            }

            // case of current_char is line break


            if (str_current_char.matches("[\r\n]") & current_string.length() == 1 & !EOP_found) {
                current_line += 1;
                printed_current_index = 0;
                current_index += 1;
                current_string = "";
            }

            if (str_current_char.matches("[\r\n]") & current_string.length() == 1 & EOP_found) {
                break;
            }

            if (str_current_char.matches("[\\t]") & current_string.length() == 1){
//                System.out.println("Found tab:" + current_line + ", " + current_index);
                printed_current_index += 5; // since tab is 5 characters (maybe not)\
                current_string = "";
            } else if (str_current_char.matches("[\\t]") & current_string.length() > 1){
                current_index = last_index; // reset index
                printed_current_index = printed_last_index;
                if (prev_token == null){
                    add_token(token_stream, current_token, verbose);
                }
                else{
                    add_token(token_stream, prev_token, verbose);
                }
                current_index += current_token.s.length() - 1;
                printed_current_index += current_token.s.length();
                current_string = "";
            }

            //check for spaces, indents, and line breaks as boundaries //TODO: Might need to change this to somewhere else in the code
            //TODO: tab should count as 5 characters, space = 1 character, next line = character:= 0
//            else if (str_current_char.matches("[ \\t\\n]+") & s.length() > 1){
//                current_index = last_index; // reset index
//                printed_current_index = printed_last_index;
//                if (prev_token == null){
//                    add_token(token_stream, current_token, verbose);
//                }
//                else{
//                    add_token(token_stream, prev_token, verbose);
//                }
//                current_index += current_token.s.length() - 1;
//                printed_current_index += current_token.s.length();
//                current_string = "";
//            }

            // use comment delimiter "/" as a boundary
            if (str_current_char.equals("/") & current_string.length() > 1) {
                current_index = last_index; // reset index
                printed_current_index = printed_last_index;
                if (prev_token == null){
                    add_token(token_stream, current_token, verbose);
                }
                else{
                    add_token(token_stream, prev_token, verbose);
                }
                current_index += current_token.s.length() - 1;
                printed_current_index += current_token.s.length();
                current_string = "";
            }

            //check for comments
            if (str_current_char.equals("/") & current_string.length() == 1){
//                System.out.println("DEBUG");
                int temp_current_index = current_index;
                temp_current_index += 1;
                char temp_current_char = s.charAt(temp_current_index);
//                System.out.println("current_string " + current_string);
//                System.out.println("temp_current_char " + temp_current_char);

                String temp_current_string = current_string;
                temp_current_string += temp_current_char;
//                System.out.println(temp_current_string);
                if (temp_current_string.equals("/*")){
                    num_extra_comment_found += 1;
//                    System.out.println("DEBUG");
                    if(debug) {
                        System.out.println("Found begin comment: [ /* ] at " + current_line + ", " + current_index);
                    }
                    while(temp_current_index < s.length() & !String.valueOf(s.charAt(temp_current_index + 1)).equals("*")){ //check for end comment
                        temp_current_index += 1;
                        if(debug){
                            System.out.print(s.charAt(temp_current_index)); // DEBUG comment
                        }
                    }
                    if(debug) {
                        System.out.println(); // DEBUG
                    }
                    temp_current_string = "";
                    temp_current_string += String.valueOf(s.charAt(temp_current_index + 1)) + s.charAt(temp_current_index + 2);
                    if (temp_current_string.equals("*/")){
                        num_extra_comment_found -= 1;
                        printed_current_index += temp_current_index - current_index + 3;
                        current_index += temp_current_index - current_index + 2; // go to next character out of comment
                        if(debug) {
                            System.out.println("Found end comment: [ */ ]  at " + current_line + ", " + current_index);
                        }
                        current_string = "";
                    }

                    else{
                        // For unterminated comments
                        System.out.println("Lexer [WARNING]: -------> Unterminated comment");
                        num_warnings += 1;
                    }
                }
                else{
                    lex_error("/", current_line, printed_current_index); //TODO: implement line number and character number EVERYWHERE NOT JUST HERE
                }
            }

            // check if current_char is symbol
            if (is_token(str_current_char)){ // Check if current character is a token
//                System.out.println("Found Token" + "[ " + get_token(str_current_char).s + " ] at: " + current_line + ", " + current_index); // DEBUGGING
                t_type = get_token(str_current_char).token_type;
                if (t_type == Grammar.EOP & current_string.length() == 1){
                    add_token(token_stream, get_token(str_current_char), verbose);
                    printed_current_index += 1;
                    EOP_found = true;
                    current_string = "";
                }
                else if (t_type == Grammar.EOP & current_string.length() > 1){
                    current_index = last_index; // reset index
                    printed_current_index = printed_last_index;
                    if (prev_token == null){
                        add_token(token_stream, current_token, verbose);
                    }
                    else{
                        add_token(token_stream, prev_token, verbose);
                    }
                    current_index += current_token.s.length() - 1;
                    printed_current_index += current_token.s.length();
                    current_string = "";
                }
                else if (t_type == Grammar.ASSIGNMENT_OP & current_string.length() == 1){  // CASE: for when the current character is an = sign and checking if next character is a == operator
                    // Create temporary variables to check if its an assignment operator or an equality operator
                    int temp_current_index = current_index; //TODO: make this a method to smallerize code
                    temp_current_index += 1;
                    char temp_current_char = s.charAt(temp_current_index);
//                    System.out.println(temp_current_char); //DEBUG
                    String temp_current_string = current_string;
                    temp_current_string += temp_current_char;
//                    System.out.println(temp_current_string); //DEBUG
                    if (is_token(temp_current_string)){ // make sure not null
                        if (get_token(temp_current_string).token_type == Grammar.EQUALITY_OP){
                            add_token(token_stream, get_token(temp_current_string), verbose);
                            current_index += 1; // 1 because we temporarily went ahead one index
                            printed_current_index += 2;
                            current_string = "";
                        }
                    }else{// assumed that it is not inequality, thus we add = Token
                        add_token(token_stream, get_token(current_string), verbose);
                        printed_current_index += 1;
                        current_string = "";
                    }
                }
                else if (t_type == Grammar.ASSIGNMENT_OP & current_string.length() > 1){  // the boundary
                    current_index = last_index; // reset index
                    printed_current_index = printed_last_index;
                    if (prev_token == null){
                        add_token(token_stream, current_token, verbose);
                        current_index += current_token.s.length() - 1;
                        printed_current_index += current_token.s.length();
                        current_string = "";
                    }
                    else{
                        add_token(token_stream, prev_token, verbose);
                        current_index += current_token.s.length() - 1; // do this to account for length of token for updated index
                        printed_current_index += current_token.s.length();
                        current_string = "";
                    }
                }

//                 Does not register the current string is not of these types
                else if ((t_type == Grammar.QUOTE | t_type == Grammar.L_BRACE | t_type == Grammar.R_BRACE | t_type == Grammar.L_PARENTH | t_type == Grammar.R_PARENTH | t_type == Grammar.ADDITION_OP) & current_string.equals(str_current_char)){
                    if (t_type == Grammar.QUOTE) {
                        close_quote_found = !close_quote_found; // TODO: check for unterminated quote
                    }
                    // Case a: unequal number of L_BRACE and R_BRACE
                    // Case b: R_BRACE with no previous L_BRACE
                    if (t_type == Grammar.L_BRACE){
                        num_extra_brace_found += 1;
//                        L_brace_found = true;
                    }
                    else if (t_type == Grammar.R_BRACE){
                        num_extra_brace_found -= 1;
//                        if (!L_brace_found){
//                            System.out.println("Lexer [WARNING]: -------> Missing L_BRACE with R_BRACE at " + current_line + ", " + printed_current_index);
//                        }
                        // case b
                        if (num_extra_brace_found < 0){
                            System.out.println("Lexer [WARNING]: -------> Missing L_BRACE with R_BRACE at " + current_line + ", " + printed_current_index);
                            num_warnings += 1;
                        }

                    }

                    if (t_type == Grammar.L_PARENTH){
                        num_extra_parenth_found += 1;
//                        L_brace_found = true;
                    }
                    else if (t_type == Grammar.R_PARENTH){
                        num_extra_parenth_found -= 1;
//                        if (!L_brace_found){
//                            System.out.println("Lexer [WARNING]: -------> Missing L_BRACE with R_BRACE at " + current_line + ", " + printed_current_index);
//                        }
                        // case b
                        if (num_extra_parenth_found < 0){
                            System.out.println("Lexer [WARNING]: -------> Missing L_PARENTH with R_PARENTH at " + current_line + ", " + printed_current_index);
                            num_warnings += 1;
                        }

                    }

                    current_token = get_token(str_current_char);
                    add_token(token_stream, current_token, verbose);
                    printed_current_index += 1;
                    current_string = "";
                }

                // make these symbols boundaries for longest match algo
                else if ((t_type == Grammar.QUOTE | t_type == Grammar.L_BRACE | t_type == Grammar.R_BRACE | t_type == Grammar.L_PARENTH | t_type == Grammar.R_PARENTH | t_type == Grammar.ADDITION_OP) & current_string.length() > 1){
                    current_index = last_index; // reset index
                    printed_current_index = printed_last_index;
                    if (prev_token == null){
                        add_token(token_stream, current_token, verbose);
                        printed_current_index += 1;
                    }
                    else{
                        add_token(token_stream, prev_token, verbose);
                        printed_current_index += 1;
                    }
                    current_index += current_token.s.length() - 1;
                    printed_current_index += current_token.s.length();
                    current_string = "";
                }

            }
            else if (!str_current_char.equals("/") & !str_current_char.matches("[\\t]") & !str_current_char.matches("[\r\n]") & !str_current_char.matches("[ ]") & !str_current_char.matches("[!]")){
                lex_error(str_current_char, current_line, printed_current_index);
                String temp_char = "";
                temp_char += s.charAt(current_index);
                while(!temp_char.equals("$")){
                    temp_char = "";
                    current_index += 1;
                    temp_char += s.charAt(current_index);
                }
                current_string = "";
                EOP_found = true; // go to next program
            }


            // Check for inequality operator of current string
            if(current_string.equals("!")){
                int temp_current_index = current_index;
                temp_current_index += 1;
                char temp_current_char = s.charAt(temp_current_index);
                String temp_current_string = current_string;
                temp_current_string += temp_current_char;
                if (get_token(temp_current_string).token_type == Grammar.INEQUALITY_OP){
                    add_token(token_stream, get_token(temp_current_string), verbose);
                    current_index += 1; // 1 because we temporarily went ahead one index
                    printed_current_index += 2;
                    current_string = "";
                }
            }

            if (is_token(current_string)) {
                 // get token type
                current_token = get_token(current_string);
                t_type = get_token(current_string).token_type;

                if (t_type == Grammar.ID) {
                    last_index = current_index; // set pointer to ID since ID might be keyword
                    printed_last_index = printed_current_index;

//                } else if (t_type == Grammar.IF | t_type == Grammar.WHILE | t_type == Grammar.PRINT | t_type == Grammar.VARIABLE_TYPE | t_type == Grammar.BOOL | t_type == Grammar.EOP) {
//
//
//                } else if (t_type == Grammar.QUOTE | t_type == Grammar.L_BRACE | t_type == Grammar.R_BRACE | t_type == Grammar.L_PARENTH | t_type == Grammar.R_PARENTH | t_type == Grammar.INEQUALITY_OP | t_type == Grammar.ADDITION_OP | t_type == Grammar.EQUALITY_OP) {
//

                } else if (t_type == Grammar.DIGIT) {
                    add_token(token_stream, current_token, verbose);
                    printed_current_index += 1;
                    current_string = "";


                } else if (t_type == Grammar.CHAR) {
                    add_token(token_stream, current_token, verbose);
                    printed_current_index += 1;
                    current_string = "";

                }
//                else{
//                    System.out.println("ERROR: lexeme not recognized as a token"); // Should not occur
//                    System.out.println("LEXEME: " + current_string);
//                }

            }

            // If EOP found for program, go to next if it exists
            if (EOP_found & current_index <= s.length() - 1){ // This assumes that there are no characters after $
                // print errors and warnings
                if (verbose & num_errors == 0) {
                    if (num_extra_brace_found > 0) {
                        System.out.println("Lexer [WARNING]: -------> Missing R_BRACE");
                        num_warnings += 1;
                    }

                    if (num_extra_parenth_found > 0) {
                        System.out.println("Lexer [WARNING]: -------> Missing R_PARENTH");
                        num_warnings += 1;
                    }

                    if (!close_quote_found) {
                        System.out.println("Lexer [WARNING]: -------> Missing possible QUOTE");
                        num_warnings += 1;
                    }
                }

                if (num_errors > 0){
                    System.out.println("Lexer -------> Lex terminated with " + num_errors + " errors and " + num_warnings + " warnings");
                }
                else{
                    System.out.println("Lexer -------> Lex finished with " + num_errors + " errors and " + num_warnings + " warnings");
                }
                System.out.println("------------------------------------------------------------");

                program_num += 1;
                if (current_index < s.length() - 1){
                    System.out.println("LEXING PROGRAM " + program_num);
                }

                // reset pointers
                printed_current_index = 0;
                printed_last_index = 0;
                current_line = 0;
                EOP_found = false;
                num_errors = 0;
                num_warnings = 0;
                num_extra_brace_found = 0;
                num_extra_parenth_found = 0;
                L_brace_found = false;
                L_parenth_found = false;
            }

            // Break if final EOP is found
            else if (EOP_found){
                break;
            }

            // Check if end of EOP is in program
            else if (!EOP_found & current_index >= s.length() - 1){
                System.out.println("Lexer [WARNING]: -------> End of Program Token not found" );
            }

            current_index += 1;
        }
        return token_stream;
    }
}