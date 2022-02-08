public class Lexer {
    // List out all tokens from our predefined grammar https://www.labouseur.com/courses/compilers/grammar.pdf
    public static enum Grammar{
        EOP, L_BRACE, R_BRACE, VARIABLE_TYPE, IF, WHILE,
        PRINT, ASSIGNMENT_OP, ID, L_QUOTE, R_QUOTE, L_PARENTH,
        R_PARENTH, CHAR, DIGIT, EQUALITY_OP, INEQUALITY_OP,
        BOOL, ADDITION_OP
    }

    public Token[] get_token_stream(String s){
        Token[] token_stream = {}; // Initialize the token_stream which what will be given to the parser
        int i = 0; // Initialize index
        while(i < s.length()){
            current_char = s.charAt(i);
        }
    }
}
