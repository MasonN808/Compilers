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
//        try {
//            FileReader fr = new FileReader(inFile);
//            BufferedReader br = new BufferedReader(fr);
//            String line;
//            while ((line = br.readLine()) != null) {
//                // add strings from txt file line by line into array words
//                words = Arrays.copyOf(words, words.length + 1); //extends memory
//                words[words.length - 1] = line; //adds word to extra memory
//            }
//        } catch (FileNotFoundException e) {
//            System.out.println("An error occurred.");
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
        String content = new Scanner(inFile).useDelimiter("\\Z").next();
        System.out.println(content);

//        String[] source_file = words.clone();

        Lexer lexer = new Lexer();
//        lexer.get_token_stream(content, );

    }
}
