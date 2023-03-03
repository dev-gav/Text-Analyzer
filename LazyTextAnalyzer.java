import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import utility.Word;
import utility.WordData;

// This is the single threaded form of TextAnalyzer,
// which we can use to check if we're getting the right
// output from TextAnalyzer.

public class LazyTextAnalyzer {

    private static final String fileName = "lazyOutput.txt";

    private static float PARSE_TIME;
    private static float ANALYZE_TIME;

    public static void lazyMain(List<String> text) throws IOException {

        ConcurrentHashMap<String, AtomicInteger> wordCounts = LazyTextAnalyzer.lazyParse(text); 

        List<Word> words = new ArrayList<Word>();
        for (String word : wordCounts.keySet()) {
            words.add(new Word(word, wordCounts.get(word).get()));
        }
        
        WordData data = LazyTextAnalyzer.lazyAnalyze(words);

        LazyTextAnalyzer.lazyOutput(data);
    }

    public static ConcurrentHashMap<String, AtomicInteger> lazyParse(List<String> words) {
        
        ConcurrentHashMap<String, AtomicInteger> wordCounts = new ConcurrentHashMap<String, AtomicInteger>();

        // Match numbers https://regexr.com/796nj
        // Click the settings icon in the top left and read
        // description for clarification.
        String numRegex = "\\d(((\\.|\\/)(?=\\d))|\\d)*";
        Pattern numPattern = Pattern.compile(numRegex);
        Matcher numMatch = null;

        // Match words https://regexr.com/796mo
        String wordRegex = "\\w((('|-)(?=\\w))|\\w)*";
        Pattern wordPattern = Pattern.compile(wordRegex);
        Matcher wordMatch = null;

        // Match any letter
        String letterRegex = "[A-z]";
        Pattern letterPattern = Pattern.compile(letterRegex);

        long startTime = System.nanoTime();

        String parse = "";
        for (String word : words) {
            // If the string has any letters, try the word regex.
            // Otherwise, try the number regex.
            if (letterPattern.matcher(word).find()) {
                wordMatch = wordPattern.matcher(word);

                if (wordMatch.find()) {
                    parse = word.substring(wordMatch.start(), wordMatch.end());
                    parse = parse.toLowerCase();

                    wordCounts.putIfAbsent(parse, new AtomicInteger(0));
                    wordCounts.get(parse).incrementAndGet();
                }
            }
            else {
                numMatch = numPattern.matcher(word);

                if (numMatch.find()) {
                    parse = word.substring(numMatch.start(), numMatch.end());

                    wordCounts.putIfAbsent(parse, new AtomicInteger(0));
                    wordCounts.get(parse).incrementAndGet();
                }
                else {
                    // no letter and number match
                    // all the junk goes here 
                    // System.out.println("number: could not parse \'" + word + "\'");
                }
            }
        }

        long endTime   = System.nanoTime();
        PARSE_TIME = (float)(endTime - startTime) / 1_000_000_000;

        return wordCounts;
    }

    public static WordData lazyAnalyze(List<Word> words) {

        WordData data = new WordData();

        long startTime = System.nanoTime();
        
        for (Word word : words) {
            data.addToTotalCount(word.count);
            data.addWord(word);
            data.checkCommonWords(word);
        }

        long endTime   = System.nanoTime();
        ANALYZE_TIME = (float)(endTime - startTime) / 1_000_000_000;

        return data;
    }

    public static void lazyOutput(WordData data) throws IOException {

        File out = new File(fileName);
        if(!out.exists())
            out.createNewFile();

        PrintWriter pw = new PrintWriter(out);

        pw.printf("File: %s\n", fileName);
        pw.printf("Parse Runtime: %.4f seconds\n", PARSE_TIME);
        pw.printf("Analyze Runtime: %.4f seconds\n", ANALYZE_TIME);
        pw.printf("Total Runtime: %.4f seconds\n", (PARSE_TIME + ANALYZE_TIME));
        pw.printf("\n");
        pw.printf("%s", data.fileOutput(TextAnalyzer.DEBUG));            
        pw.close();
    }  
}
