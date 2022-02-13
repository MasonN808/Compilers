import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;

public class Compiler808 {
    public static void main(String[] args) throws FileNotFoundException {
        String[] words = {};

        File inFile = null;
        boolean is_verbose = false;
//        if (0 < args.length) {
//            inFile = new File(args[0]);
//            if (args[1].equals("verbose")){
//                is_verbose = true;
//            }
//        }
//        is_verbose = true;
        inFile = new File("src/test1");


//        assert inFile != null;
        String content = new Scanner(inFile).useDelimiter("\\Z").next();
        System.out.println(content);
        String smallerized_content = content.replaceAll("\\s+", ""); //remove line breaks, tabs, and spaces
//        String smallerized_content = content.replaceAll("[\\n\\t ]", "");
//        System.out.println(smallerized_content);
//        String[] lines = Files.readAllLines(Paths.get(path), encoding);
        Lexer lexer = new Lexer();
//        System.out.println(Lexer.is_token("{"));
        lexer.get_token_stream(content, is_verbose, true);
//        lexer.get_token_stream(smallerized_content, true, true);



    }
}
