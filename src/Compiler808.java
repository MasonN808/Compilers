import java.io.*;
import java.util.Scanner;

public class Compiler808 {

    public enum Grammar {
        EOP, L_BRACE, R_BRACE, VARIABLE_TYPE, IF, WHILE,
        PRINT, ASSIGNMENT_OP, ID, QUOTE, L_PARENTH,
        R_PARENTH, CHAR, DIGIT, EQUALITY_OP, INEQUALITY_OP,
        FALSE, TRUE, ADDITION_OP, SPACE
    }

    public static void main(String[] args) throws FileNotFoundException {

        File inFile = null;
        boolean is_verbose = true;
        if (0 < args.length) {
            inFile = new File(args[0]);
        }
        if (1 < args.length) {
            if (args[1].equals("notVerbose")){
                is_verbose = false;
            }
        }
        String content = new Scanner(inFile).useDelimiter("\\Z").next();
        Lexer lexer = new Lexer();
        lexer.get_token_stream(content, is_verbose);


//        boolean is_verbose = false;
//        File inFile = new File("Tests/opCodes");
//
//        String content = new Scanner(inFile).useDelimiter("\\Z").next();
//        Lexer lexer = new Lexer();
//        lexer.get_token_stream(content, is_verbose);


    }
}
