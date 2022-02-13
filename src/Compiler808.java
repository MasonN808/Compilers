import java.io.*;
import java.util.Scanner;

public class Compiler808 {
    public static void main(String[] args) throws FileNotFoundException {

        File inFile = null;
        boolean is_verbose = false;
        if (0 < args.length) {
            inFile = new File(args[0]);
        }
        if (1 < args.length) {
            if (args[1].equals("verbose")){
                is_verbose = true;
            }
        }

//        is_verbose = true;
//        inFile = new File("src/test1");


//        assert inFile != null;
        String content = new Scanner(inFile).useDelimiter("\\Z").next();
//        System.out.println(content);
//        String smallerized_content = content.replaceAll("\\s+", ""); //remove line breaks, tabs, and spaces
        Lexer lexer = new Lexer();
        lexer.get_token_stream(content, is_verbose);
//        lexer.get_token_stream(smallerized_content, true);



    }
}
