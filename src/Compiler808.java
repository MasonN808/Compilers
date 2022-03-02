import java.io.*;
import java.util.Scanner;

public class Compiler808 {
    public static void main(String[] args) throws FileNotFoundException {

//        File inFile = null;
//        boolean is_verbose = false;
//        if (0 < args.length) {
//            inFile = new File(args[0]);
//        }
//        if (1 < args.length) {
//            if (args[1].equals("verbose")){
//                is_verbose = true;
//            }
//        }
//        String content = new Scanner(inFile).useDelimiter("\\Z").next();
//        Lexer lexer = new Lexer();
//        lexer.get_token_stream(content, is_verbose);


        boolean is_verbose = true;
        File inFile = new File("src/test2");

        String content = new Scanner(inFile).useDelimiter("\\Z").next();
        Lexer lexer = new Lexer();
        lexer.get_token_stream(content, is_verbose);
        System.out.println(System.lineSeparator());


    }
}
