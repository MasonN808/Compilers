import java.util.ArrayList;

/**
 * <h1>Lexical Analysis (Part 1)</h1>
 * We design a lexical analyzer ( or scanner, lexer, ...) to produce a stream
 * of tokens to be fed to the parser (Part 2)
 * <p>
 * <b>Note:</b> Pretty much finished (Needs refactoring)
 *
 * @author  Mason Nakamura
 * @since   03-01-2022
 */
public class Lexer {
//    Logger logger
//            = Logger.getLogger(
//            Lexer2.class.getName());


    public static boolean EOP_found = false; // to check if EOP is found
    public static boolean close_quote_found = true; // to check if closed quote is found
    public static int num_extra_parenth_found = 0;
    public static int num_extra_brace_found = 0;
    public static int num_extra_comment_found = 0;
    public static boolean first_thing_found = false; //To check for the first non-empty line in program -> for line number transformation

    public boolean L_brace_found = false;
    public boolean L_parenth_found = false;

    public static int num_errors = 0;
    public static int num_warnings = 0;
    public static int program_num = 0;

    public static boolean WINDOWS = false;

    public static int current_index = 0; // Initialize index
    public static int printed_last_index = 0; // The index to be printed
    public static int printed_current_index = 0; // The index to be printed
    public static int last_index = 0; // keep track of index of last token found and verified using longest match and rule order
    public static int current_line = 1;
    public Token current_token;
    public boolean debug = false; // For debugging


    // List out all tokens from our predefined Compiler808.Grammar https://www.labouseur.com/courses/compilers/Compiler808.Grammar.pdf


    /**
     * This method prints out various tokens depending on the token the lexer identifies
     * @param tokenStream The stream of tokens the lexer already identified
     * @param token  The token the lexer identifies after longest match and rule order
     * @param verbose A condition to allow the user to print extra information
     */
    public static void add_token(ArrayList<Token> tokenStream, Token token, boolean verbose) {
        if (verbose) {
            System.out.println("Lexer -------> " + token.token_type + " [ " + token.s + " ] at "
                    + token.line_number + ", " + token.character_number);
        }
        tokenStream.add(token);
    }

    /**
     * This method uses our Compiler808.Grammar to determine whether a string is identified as a token
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
     * This method uses our Compiler808.Grammar to return the token of the current lexeme if it exists
     * @param current_lexeme The lexeme to be tested
     * @return boolean
     */
    public static Token get_token(String current_lexeme){
        Token token = null; // initialize the token
        switch (current_lexeme) {
            case ("$") -> token = new Token(Compiler808.Grammar.EOP, current_lexeme, current_line, printed_current_index); // END OF PROGRAM (EOP)
            case ("{") -> token = new Token(Compiler808.Grammar.L_BRACE, current_lexeme, current_line, printed_current_index); // BRACES
            case ("}") -> token = new Token(Compiler808.Grammar.R_BRACE, current_lexeme, current_line, printed_current_index);
            case ("\"") -> token = new Token(Compiler808.Grammar.QUOTE, current_lexeme, current_line, printed_current_index); // QUOTE
            case ("+") -> token = new Token(Compiler808.Grammar.ADDITION_OP, current_lexeme, current_line, printed_current_index); // OPERATORS
            case ("=") -> token = new Token(Compiler808.Grammar.ASSIGNMENT_OP, current_lexeme, current_line, printed_current_index);
            case ("==") -> token = new Token(Compiler808.Grammar.EQUALITY_OP, current_lexeme, current_line, printed_current_index);
            case ("!=") -> token = new Token(Compiler808.Grammar.INEQUALITY_OP, current_lexeme, current_line, printed_current_index);
            case ("(") -> token = new Token(Compiler808.Grammar.L_PARENTH, current_lexeme, current_line, printed_current_index); // PARENTHESIS
            case (")") -> token = new Token(Compiler808.Grammar.R_PARENTH, current_lexeme, current_line, printed_current_index);
            case ("false") -> token = new Token(Compiler808.Grammar.FALSE, current_lexeme, current_line, printed_current_index); // BOOL
            case ("true") -> token = new Token(Compiler808.Grammar.TRUE, current_lexeme, current_line, printed_current_index); // BOOL
            case ("if") -> token = new Token(Compiler808.Grammar.IF, current_lexeme, current_line, printed_current_index); // IF
            case ("while") -> token = new Token(Compiler808.Grammar.WHILE, current_lexeme, current_line, printed_current_index); // WHILE
            case ("print") -> token = new Token(Compiler808.Grammar.PRINT, current_lexeme, current_line, printed_current_index); // PRINT
            case ("int"), ("string"), ("boolean") -> token = new Token(Compiler808.Grammar.VARIABLE_TYPE, current_lexeme, current_line, printed_current_index); // VARIABLE TYPES
            default -> { //since you can't use regex in switch{}, we put the many cases in default
                if (current_lexeme.matches("[a-z]") & !close_quote_found) { // It is a CHAR if in quotes, else ID
                    token = new Token(Compiler808.Grammar.CHAR, current_lexeme, current_line, printed_current_index); // CHAR
                }
//                else if (current_lexeme.matches(" ") & !close_quote_found){
//                    token = new Token(Compiler808.Grammar.SPACE, current_lexeme, current_line, printed_current_index); // SPACE
//                }
                else if (current_lexeme.matches("[a-z]")) {
//                    System.out.println("quote_closed :" + close_quote_found); //DEBUGGING
                    token = new Token(Compiler808.Grammar.ID, current_lexeme, current_line, printed_current_index); // CHAR
                }
                else if (current_lexeme.matches("[0-9]")) {
                    token = new Token(Compiler808.Grammar.DIGIT, current_lexeme, current_line, printed_current_index); // CHAR
                } else {
                    lex_error(current_lexeme, current_line, printed_current_index);
                    printed_current_index += 1;
//                    System.out.println("ERROR: NO TOKEN FOUND FOR " + current_line + ", " + printed_current_index);
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

        //Check for line break character count
        if (System.lineSeparator().length()==2){//Windows
            WINDOWS = true;
        }

        ArrayList<Token> tokenStreamAggregate = new ArrayList<Token>(); // Initialize the tokenStream that will output ALL tokenStreams
        ArrayList<Token> tokenStream = new ArrayList<Token>(); // Initialize the tokenStream that will be given to the parser
        String current_string = "";

        Token prev_token = null;
        Compiler808.Grammar t_type;


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

            // Sets the starting line to be the first instance of a non empty line in the program (advised by Alan 2/15/2022)
            if (!current_string.matches("[ ]") & !current_string.matches("[\\t]") & current_char!=(System.lineSeparator().charAt(0))  & !first_thing_found){
                first_thing_found = true;
                current_line = 1;
            }

            if (str_current_char.equals(" ") & !close_quote_found & current_string.length() == 1){
                Token token = new Token(Compiler808.Grammar.SPACE, " ", current_line, current_char); // create space character
                add_token(tokenStream, token, verbose);
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
                    add_token(tokenStream, current_token, verbose);
                }
                else{
                    add_token(tokenStream, prev_token, verbose);
                }
                current_index += current_token.s.length() - 1;
                printed_current_index += current_token.s.length();
                current_string = "";
            }

            // case of current_char is line break
            if (current_char==(System.lineSeparator().charAt(0)) & current_string.length() == 1 & !EOP_found) {
//                if (WINDOWS){ // We do this sine lineSeparator is two char long
//                    current_index += 1;
//                }
                current_line += 1;
                printed_current_index = 0;
                current_index += 1;
                current_string = "";
            }

            if (current_char==(System.lineSeparator().charAt(0)) & current_string.length() == 1 & EOP_found) {
                if (WINDOWS){
                    current_index += 1;
                }
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
                    add_token(tokenStream, current_token, verbose);
                }
                else{
                    add_token(tokenStream, prev_token, verbose);
                }
                current_index += current_token.s.length() - 1;
                printed_current_index += current_token.s.length();
                current_string = "";
            }

            // use comment delimiter "/" as a boundary
            if (str_current_char.equals("/") & current_string.length() > 1) {
                current_index = last_index; // reset index
                printed_current_index = printed_last_index;
                if (prev_token == null){
                    add_token(tokenStream, current_token, verbose);
                }
                else{
                    add_token(tokenStream, prev_token, verbose);
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
                if (temp_current_string.equals("/*")){ //TODO: Work on embedded comments
                    num_extra_comment_found += 1;
//                    System.out.println("DEBUG");
                    if(debug) {
                        System.out.println("Found begin comment: [ /* ] at " + current_line + ", " + current_index);
                    }
                    boolean found_end = false;
                    while(temp_current_index < s.length() & !found_end){ //check for end comment
                        temp_current_index += 1;
                        if (String.valueOf(s.charAt(temp_current_index + 1)).equals("*") & String.valueOf(s.charAt(temp_current_index + 2)).equals("/")){
                            found_end = true;
                        }
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
                    lex_error("/", current_line, printed_current_index);
                    printed_current_index += 1;
                    current_string = ""; // do this to continue in program
                }
            }

            // check if current_char is symbol
            if (is_token(str_current_char)){ // Check if current character is a token
//                System.out.println("Found Token" + "[ " + get_token(str_current_char).s + " ] at: " + current_line + ", " + current_index); // DEBUGGING
                t_type = get_token(str_current_char).token_type;
                if (t_type == Compiler808.Grammar.EOP & current_string.length() == 1){
                    add_token(tokenStream, get_token(str_current_char), verbose);
                    printed_current_index += 1;
                    EOP_found = true;
                    current_string = "";
                }
                else if (t_type == Compiler808.Grammar.EOP & current_string.length() > 1){
                    current_index = last_index; // reset index
                    printed_current_index = printed_last_index;
                    if (prev_token == null){
                        add_token(tokenStream, current_token, verbose);
                    }
                    else{
                        add_token(tokenStream, prev_token, verbose);
                    }
                    current_index += current_token.s.length() - 1;
                    printed_current_index += current_token.s.length();
                    current_string = "";
                }
                else if (t_type == Compiler808.Grammar.ASSIGNMENT_OP & current_string.length() == 1){  // CASE: for when the current character is an = sign and checking if next character is a == operator
                    // Create temporary variables to check if its an assignment operator or an equality operator
                    int temp_current_index = current_index; //TODO: make this a method to smallerize code
                    temp_current_index += 1;
                    char temp_current_char = s.charAt(temp_current_index);
//                    System.out.println(temp_current_char); //DEBUG
                    String temp_current_string = current_string;
                    temp_current_string += temp_current_char;
//                    System.out.println(temp_current_string); //DEBUG
                    if (is_token(temp_current_string)){ // make sure not null
                        if (get_token(temp_current_string).token_type == Compiler808.Grammar.EQUALITY_OP){
                            add_token(tokenStream, get_token(temp_current_string), verbose);
                            current_index += 1; // 1 because we temporarily went ahead one index
                            printed_current_index += 2;
                            current_string = "";
                        }
                    }else{// assumed that it is not inequality, thus we add = Token
                        add_token(tokenStream, get_token(current_string), verbose);
                        printed_current_index += 1;
                        current_string = "";
                    }
                }
                else if (t_type == Compiler808.Grammar.ASSIGNMENT_OP & current_string.length() > 1){  // the boundary
                    current_index = last_index; // reset index
                    printed_current_index = printed_last_index;
                    if (prev_token == null){
                        add_token(tokenStream, current_token, verbose);
                        current_index += current_token.s.length() - 1;
                        printed_current_index += current_token.s.length();
                        current_string = "";
                    }
                    else{
                        add_token(tokenStream, prev_token, verbose);
                        current_index += current_token.s.length() - 1; // do this to account for length of token for updated index
                        printed_current_index += current_token.s.length();
                        current_string = "";
                    }
                }

//                 Does not register the current string is not of these types
                else if ((t_type == Compiler808.Grammar.QUOTE | t_type == Compiler808.Grammar.L_BRACE | t_type == Compiler808.Grammar.R_BRACE | t_type == Compiler808.Grammar.L_PARENTH | t_type == Compiler808.Grammar.R_PARENTH | t_type == Compiler808.Grammar.ADDITION_OP) & current_string.equals(str_current_char)){
                    if (t_type == Compiler808.Grammar.QUOTE) {
                        close_quote_found = !close_quote_found; // TODO: check for unterminated quote
                    }
                    // Case a: unequal number of L_BRACE and R_BRACE
                    // Case b: R_BRACE with no previous L_BRACE
                    if (t_type == Compiler808.Grammar.L_BRACE){
                        num_extra_brace_found += 1;
//                        L_brace_found = true;
                    }
                    else if (t_type == Compiler808.Grammar.R_BRACE){
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

                    if (t_type == Compiler808.Grammar.L_PARENTH){
                        num_extra_parenth_found += 1;
//                        L_brace_found = true;
                    }
                    else if (t_type == Compiler808.Grammar.R_PARENTH){
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
                    add_token(tokenStream, current_token, verbose);
                    printed_current_index += 1;
                    current_string = "";
                }

                // make these symbols boundaries for longest match algo
                else if ((t_type == Compiler808.Grammar.QUOTE | t_type == Compiler808.Grammar.L_BRACE | t_type == Compiler808.Grammar.R_BRACE | t_type == Compiler808.Grammar.L_PARENTH | t_type == Compiler808.Grammar.R_PARENTH | t_type == Compiler808.Grammar.ADDITION_OP) & current_string.length() > 1){
                    current_index = last_index; // reset index
                    printed_current_index = printed_last_index;
                    if (prev_token == null){
                        add_token(tokenStream, current_token, verbose);
                        printed_current_index += 1;
                    }
                    else{
                        add_token(tokenStream, prev_token, verbose);
                        printed_current_index += 1;
                    }
                    current_index += current_token.s.length() - 1;
                    printed_current_index += current_token.s.length();
                    current_string = "";
                }

            }
            else if (!str_current_char.equals("/") & !str_current_char.matches("[\\t]") & !str_current_char.matches("[\\r]?[\\n]?") & !str_current_char.matches("[ ]") & !str_current_char.matches("[!]")){
//                System.out.println("\r".equals(str_current_char));
                lex_error(str_current_char, current_line, printed_current_index);
                printed_current_index += 1;
                // This commented out portion allows to find end of program to exit program if lex error encountered
//                String temp_char = "";
//                temp_char += s.charAt(current_index);
//                while(!temp_char.equals("$")){
//                    temp_char = "";
//                    current_index += 1;
//                    temp_char += s.charAt(current_index);
//                }
                current_string = "";
//                EOP_found = true; // go to next program
            }


            // Check for inequality operator of current string
            if(current_string.equals("!")){
                int temp_current_index = current_index;
                temp_current_index += 1;
                char temp_current_char = s.charAt(temp_current_index);
                String temp_current_string = current_string;
                temp_current_string += temp_current_char;
                if (is_token(temp_current_string)){
                    if (get_token(temp_current_string).token_type == Compiler808.Grammar.INEQUALITY_OP) {
                        add_token(tokenStream, get_token(temp_current_string), verbose);
                        current_index += 1; // 1 because we temporarily went ahead one index
                        printed_current_index += 2;
                        current_string = "";
                    }
                }
                else{
                    lex_error(current_string, current_line, printed_current_index);
                    current_string = "";
                    printed_current_index += 1;

                }
            }

            if (is_token(current_string)) {
                 // get token type
                current_token = get_token(current_string);
                t_type = get_token(current_string).token_type;

                if (t_type == Compiler808.Grammar.ID) {
                    last_index = current_index; // set pointer to ID since ID might be keyword
                    printed_last_index = printed_current_index;

//                } else if (t_type == Compiler808.Grammar.IF | t_type == Compiler808.Grammar.WHILE | t_type == Compiler808.Grammar.PRINT | t_type == Compiler808.Grammar.VARIABLE_TYPE | t_type == Compiler808.Grammar.BOOL | t_type == Compiler808.Grammar.EOP) {
//
//
//                } else if (t_type == Compiler808.Grammar.QUOTE | t_type == Compiler808.Grammar.L_BRACE | t_type == Compiler808.Grammar.R_BRACE | t_type == Compiler808.Grammar.L_PARENTH | t_type == Compiler808.Grammar.R_PARENTH | t_type == Compiler808.Grammar.INEQUALITY_OP | t_type == Compiler808.Grammar.ADDITION_OP | t_type == Compiler808.Grammar.EQUALITY_OP) {
//

                } else if (t_type == Compiler808.Grammar.DIGIT) {
                    add_token(tokenStream, current_token, verbose);
                    printed_current_index += 1;
                    current_string = "";


                } else if (t_type == Compiler808.Grammar.CHAR) {
                    add_token(tokenStream, current_token, verbose);
                    printed_current_index += 1;
                    current_string = "";

                }

            }

            // If EOP found for program, go to next if it exists
            if (EOP_found & current_index <= s.length() - 1){ // This assumes that there are no characters after $
                // print errors and warnings
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

                if (num_errors > 0){
                    System.out.println("Lexer -------> Lex terminated with " + num_errors + " errors and " + num_warnings + " warnings");
                }
                else{
                    System.out.println("Lexer -------> Lex finished with " + num_errors + " errors and " + num_warnings + " warnings");
                }

                //DEBUGGING
//                for (Token token: tokenStream){
//                    System.out.print(" " + token.s);
//                }

                if (num_errors == 0){
                    // putting parser here:
                    System.out.println("------------------------------------------------------------");
                    System.out.println("PARSING Program " + program_num);
                    Parser parser = new Parser(tokenStream, verbose);
                    parser.parseProgram();
                    if (!Parser.foundError) {
                        System.out.println("Parser -------> Parse finished SUCCESSFULLY");

                        // For CST
                        System.out.println("------------------------------------------------------------");
                        System.out.println("CST for Program " + program_num);
                        System.out.println(parser.cst.traverse(parser.cst.root, 0, ""));

                        AbstractSyntaxTree abstractST = new AbstractSyntaxTree(tokenStream, verbose);
                        abstractST.parseProgram();
                        abstractST.rearrangeTree(abstractST.ast.root); //Rearrange tree for boolOp rearrangement
                        // Assume parser made CST successfully, so don't have to check for errors
                        // For AST
                        System.out.println("------------------------------------------------------------");
                        System.out.println("AST for Program " + program_num);
                        System.out.println(abstractST.ast.traverse(abstractST.ast.root, 0, ""));

                        // For Symbol Table
                        TreeST treeST = new TreeST(abstractST.ast);
                        System.out.println("------------------------------------------------------------");
                        System.out.println("SYMBOL TABLE for Program " + program_num);
                        treeST.buildSymbolTable(); // build table from ast.root

                        if (treeST.numErrors > 0){
                            System.out.println("SYMBOL TABLE -------> SYMBOL TABLE terminated UNSUCCESSFULLY");
                            System.out.println("------------------------------------------------------------");
                            System.out.println("SKIP CODE GEN for program  " + program_num + " since error(s) in Symbol Table");
                            System.out.println("------------------------------------------------------------");
                        }
                        else {
                            System.out.println("SYMBOL TABLE -------> SYMBOL TABLE finished SUCCESSFULLY");
                            boolean[] discovered = new boolean[treeST.scopeNum]; //make sure all values are false
                            treeST.BFS(treeST, treeST.root, discovered);
                        }
                    }
                    else {
                        System.out.println("Parser -------> Parse terminated UNSUCCESSFULLY");
                        System.out.println("------------------------------------------------------------");
                        System.out.println("SKIP CST for program " + program_num + " since error(s) in Parse");
                        System.out.println("------------------------------------------------------------");
                    }

                }
                else { // if lex had errors
                    System.out.println("------------------------------------------------------------");
                    System.out.println("SKIP Parsing program " + program_num + " since error(s) in Lex");
                    System.out.println("------------------------------------------------------------");
                }

                System.out.println();
                System.out.println();

                program_num += 1;

                // Check if this was the last program
                if (current_index < s.length()-1){
                    //search for any char besides space, tabs, and newlines
                    int temp_index = current_index + 1;
                    boolean found = false;
                    while (temp_index < s.length() & !found){
                        char temp_char = s.charAt(temp_index);
                        if (!String.valueOf(temp_char).matches("[ ]") & !String.valueOf(temp_char).matches("[\\t]") & temp_char!=(System.lineSeparator().charAt(0))){
                            if (WINDOWS){
                                if (temp_char != (System.lineSeparator().charAt(1)) & temp_index < s.length()-1){
                                    found = true;
                                }
                            }

                        }
                        temp_index += 1;
                    }
                    if (found){
                        System.out.println("------------------------------------------------------------");
                        System.out.println("LEXING PROGRAM " + program_num);
                    }

                }

                // reset pointers
                printed_current_index = 0;
                printed_last_index = 0;
                current_line = 1;
                EOP_found = false;
                num_errors = 0;
                num_warnings = 0;
                num_extra_brace_found = 0;
                num_extra_parenth_found = 0;
                L_brace_found = false;
                L_parenth_found = false;
                close_quote_found = true;
                first_thing_found = false;

                tokenStreamAggregate.addAll(tokenStream); //Add current tokenstream to aggregate


                tokenStream = new ArrayList<Token>(); // make TokenStream empty for next program
            }

            // Break if final EOP is found
            else if (EOP_found){
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
                break;
            }

            // Check if end of EOP is in program, specifically if error is found (A parser problem, not a lexer problem)(Commented out)
//            else if (!EOP_found & current_index >= s.length() - 1){
//                System.out.println("Lexer [WARNING]: -------> End of Program Token not found" );
//            }

            current_index += 1;
        }
        return tokenStream;
    }
}