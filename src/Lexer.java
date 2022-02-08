import java.util.ArrayList;
import java.util.Arrays;
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
        int i = 0; // Initialize index
        while(i < s.length()){
            char current_char = s.charAt(i);
            Token token;
            switch (current_char){
                case ('$'): token = new Token(Grammar.EOP, "$");
                            add_token(token_stream, token, verbose); //Add token to the token stream
                            break;
                case ('{'): token = new Token(Grammar.L_BRACE, "{");
                            add_token(token_stream, token, verbose);
                            break;
                case ('}'): token = new Token(Grammar.R_BRACE, "}");
                            add_token(token_stream, token, verbose);
                            break;
                case ('"'): token = new Token(Grammar.QUOTE, "\"");
                            add_token(token_stream, token, verbose);
                            break;
            }
        }
    }
}
