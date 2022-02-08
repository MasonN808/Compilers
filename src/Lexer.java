import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Lexer {
    // List out all tokens from our predefined grammar https://www.labouseur.com/courses/compilers/grammar.pdf
    public static enum Grammar{
        EOP, L_BRACE, R_BRACE, VARIABLE_TYPE, IF, WHILE,
        PRINT, ASSIGNMENT_OP, ID, L_QUOTE, R_QUOTE, L_PARENTH,
        R_PARENTH, CHAR, DIGIT, EQUALITY_OP, INEQUALITY_OP,
        BOOL, ADDITION_OP
    }

    public ArrayList<Token> get_token_stream(String s){
        ArrayList<Token> token_stream = new ArrayList<Token>(); // Initialize the token_stream which what will be given to the parser
        int i = 0; // Initialize index
        while(i < s.length()){
            char current_char = s.charAt(i);
            switch (current_char){
                case ('$'): Token token = new Token(Grammar.EOP, "$");
                            token_stream = Arrays.copyOf(token_stream, token_stream.length + 1); //extends memory
                            token_stream.
                            break;
            }
        }
    }
}
