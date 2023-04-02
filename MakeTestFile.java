import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class MakeTestFile {
    public static void main(String[] args) {
        Scanner scanner = null;
        PrintWriter pw = null;
        String[] words = {"word ", "this ", "it ", "it's ", "and ", "bees ", "barry ", "i ", "i'm ", "won't "};
        File out;
        try {

            scanner = new Scanner(System.in);
            System.out.println("How many words?");
            int wordCount = scanner.nextInt();

            ArrayList<String> printThese = new ArrayList<String>();
            for (int i = 0; i < wordCount * words.length; i++) {
                printThese.add(words[i % words.length]);
            }
            Collections.shuffle(printThese);

            out = new File("test.txt");
            if(!out.exists())
                out.createNewFile();

            pw = new PrintWriter(out);

            for (String word : printThese) {
                pw.print(word);
            }

        } catch (IOException e){
            e.printStackTrace();
        } finally {
            scanner.close();
            pw.close();
        }
    }  
}
