import java.io.*;
import java.util.Arrays;
import java.util.Scanner;

public class Compiler808 {
    public static void main(String[] args) throws FileNotFoundException {
        String[] words = {};

        File inFile = null;
        if (0 < args.length) {
            inFile = new File(args[0]);
        }

        assert inFile != null;
        String content = new Scanner(inFile).useDelimiter("\\Z").next();
        System.out.println(content);
        String smallerized_content = content.replaceAll("\\s+", ""); //remove line breaks, tabs, and spaces
        System.out.println(smallerized_content);

        Lexer lexer = new Lexer();
        lexer.get_token_stream(content, true, true);


    }
}
