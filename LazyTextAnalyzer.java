import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import utility.CHMWrapper;
import utility.WordData;

// This is the single threaded form of TextAnalyzer,
// which we can use to check if we're getting the right
// output from TextAnalyzer.

public class LazyTextAnalyzer {

    private static float PARSE_TIME;
    private static float ANALYZE_TIME;

    public static void lazyMain(List<String> text) throws IOException {

        ConcurrentHashMap<String, AtomicInteger> wordCounts = LazyTextAnalyzer.lazyParse(text); 
        WordData data = LazyTextAnalyzer.lazyAnalyze(wordCounts.entrySet());
        LazyTextAnalyzer.lazyOutput(data);
    }

    public static ConcurrentHashMap<String, AtomicInteger> lazyParse(List<String> words) {
        
        ConcurrentHashMap<String, AtomicInteger> wordCounts = new ConcurrentHashMap<String, AtomicInteger>();

        // Regices
        // Match words https://regexr.com/796mo
        // Click the settings icon in the top left
        // and read description for info.
        String wordRegex = "\\w((('|-)(?=\\w))|\\w)*";
        Pattern wordPattern = Pattern.compile(wordRegex);
        Matcher wordMatch = null;

        // Match numbers https://regexr.com/796nj
        String numRegex = "\\d(((\\.|\\/)(?=\\d))|\\d)*";
        Pattern numPattern = Pattern.compile(numRegex);
        Matcher numMatch = null;

        // Match any letter
        String letterRegex = "[A-z]";
        Pattern letterPattern = Pattern.compile(letterRegex);

        String parse = "";

        long startTime = System.nanoTime();
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

    public static WordData lazyAnalyze(Set<Entry<String, AtomicInteger>> wordCounts) throws FileNotFoundException {
        
        List<Entry<String, AtomicInteger>> wordCountsList = new ArrayList<>();
        wordCountsList.addAll(wordCounts);

        // Reads common English words and puts them in a hash map
        HashSet<String> mostCommonEnglishWords = TextAnalyzer.readWords(TextAnalyzer.mostCommonWordsFile); 

        // Reads custom words from list and puts them in a hash map
        HashSet<String> customWords = null;
        ConcurrentHashMap<String, Integer> customWordsData = null;
        if(!TextAnalyzer.customWordsFile.isEmpty()) {
            customWords = TextAnalyzer.readWords(TextAnalyzer.customWordsFile); 
            customWordsData = new ConcurrentHashMap<String, Integer>(16, 0.75f, TextAnalyzer.NUM_THREADS);
        }

        int totalWordCount = 0;
        int onlyWordCount = 0;
        CHMWrapper mostCommonWords = new CHMWrapper(TextAnalyzer.NUM_THREADS);
        CHMWrapper mostCommonUniqueWords = new CHMWrapper(TextAnalyzer.NUM_THREADS);

        long startTime = System.nanoTime();
        for (Entry<String, AtomicInteger> word : wordCountsList) {
            // Increment total count of words (addToTotalCount())
            totalWordCount += word.getValue().get();

            // Increment only word count (incrementOnlyWordCount())
            if (word.getValue().get() == 1)
                onlyWordCount++;

            // Check for common english words
            if (mostCommonEnglishWords.contains(word.getKey())) {
                mostCommonWords.check(Map.entry(word.getKey(), word.getValue().get()));
            }
            // Checks for common words that are not common in the English language
            else {
                mostCommonUniqueWords.check(Map.entry(word.getKey(), word.getValue().get()));
            }

            if ((customWords != null) && (customWords.contains(word.getKey()))) {
                customWordsData.put(word.getKey(), word.getValue().get()); 
            }
        }

        long endTime   = System.nanoTime();
        ANALYZE_TIME = (float)(endTime - startTime) / 1_000_000_000;

        // Create word data constructor using analyzed information
        WordData data = new WordData(totalWordCount, onlyWordCount, wordCountsList, customWords, customWordsData, mostCommonWords, mostCommonUniqueWords);

        return data;
    }

    public static void lazyOutput(WordData data) throws IOException {

        File out = new File("lazyOutput.txt");
        if(!out.exists())
            out.createNewFile();

        PrintWriter pw = new PrintWriter(out);

        pw.printf("File: %s\n", TextAnalyzer.inputFile);
        pw.printf("Parse Runtime: %.4f seconds\n", PARSE_TIME);
        pw.printf("Analyze Runtime: %.4f seconds\n", ANALYZE_TIME);
        pw.printf("Total Runtime: %.4f seconds\n", (PARSE_TIME + ANALYZE_TIME));
        pw.printf("\n");
        pw.printf("%s", data.fileOutput(TextAnalyzer.DEBUG));            
        pw.close();
    }  
}
