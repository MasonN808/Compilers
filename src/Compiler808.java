import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;

public class Compiler808 {
    public static void main(String[] args) throws FileNotFoundException {
        String[] words = {};

        File inFile = null;
//        if (0 < args.length) {
//            inFile = new File(args[0]);
//        }

        inFile = new File("src/test1");


//        assert inFile != null;
        String content = new Scanner(inFile).useDelimiter("\\Z").next();
        System.out.println(content);
        String smallerized_content = content.replaceAll("\\s+", ""); //remove line breaks, tabs, and spaces
//        String smallerized_content = content.replaceAll("[\\n\\t ]", "");
        System.out.println(smallerized_content); //TODO: move this to lexer to keep track of line number
        //TODO: get an array of lines of the code and run each line in the lexer CONTINUE HERE 2/11/2022
//        String[] lines = Files.readAllLines(Paths.get(path), encoding);
        Lexer lexer = new Lexer();
//        System.out.println(Lexer.is_token("{"));
        lexer.get_token_stream(content, true, true);
//        lexer.get_token_stream(content, true, true);



    }
}
