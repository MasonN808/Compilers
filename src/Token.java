public class Token {
    public Lexer.Grammar token_type;
    public String s;
    public int line_number; // For error log
    public int character_number; // For error log

    // Constructor for Token Class
    public Token(Lexer.Grammar token_type, String s){
        this.token_type = token_type;
        this.s = s;
    }
}
