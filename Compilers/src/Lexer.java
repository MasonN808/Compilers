public class Lexer {
    // List out all tokens from our predefined grammar https://www.labouseur.com/courses/compilers/grammar.pdf
    public static enum Grammar{
        EOP, LBRACE, RBRACE, PRINT_STATEMENT,  ASSIGNMENT_OP, VARIABLE_TYPE,
        WHILE_STATEMENT, IF_STATEMENT, ID, EQUALITY_0P, INEQUALITY_OP, ADDITION_OP,
        BOOL, DIGIT, WHILE, SPACE, LPARENTH, RPARENTH, LQUOTE, RQUOTE
    }
    public void get_token(){

    }
}
