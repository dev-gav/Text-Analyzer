import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
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

    public static final String inputFile = "text.txt";
    public static final boolean DEBUG = true;

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

    public static WordData lazyAnalyze(List<Word> words) throws FileNotFoundException {

        long startTime = System.nanoTime();
        
        int totalWordCount = 0;
        int onlyWordCount = 0;
        ConcurrentHashMap<Word, AtomicInteger> mostCommonWords = new ConcurrentHashMap<Word, AtomicInteger>(16, 0.75f, TextAnalyzer.NUM_THREADS);;
        ConcurrentHashMap<Word, AtomicInteger> mostCommonUniqueWords = new ConcurrentHashMap<Word, AtomicInteger>(16, 0.75f, TextAnalyzer.NUM_THREADS);;

        // Gets common english words
        ConcurrentHashMap<Word, AtomicInteger> commonEnglishWords = TextAnalyzer.readWords(TextAnalyzer.mostCommonWordsFile);

        for (Word word : words) {
            // Increment total count of words (addToTotalCount())
            totalWordCount += word.count;

            // Increment only word count (incrementOnlyWordCount())
            if (word.count == 1)
                onlyWordCount++;

            // Check for common english words
            if (commonEnglishWords.containsKey(word)) {
                if (mostCommonWords.size() < WordData.ARRAY_SIZE) {
                    mostCommonWords.put(word, new AtomicInteger(0));
                }
                else {
                    for (Word commonWord : mostCommonWords.keySet()) {
                        if (Word.descending.compare(word, commonWord) == -1) {
                            mostCommonWords.remove(commonWord);
                            mostCommonWords.put(word, new AtomicInteger(0));
                            break;
                        }
                    }
                }
            }
            else {
                // Checks for common words that are not common in the English language
                if (mostCommonUniqueWords.size() < WordData.ARRAY_SIZE) {
                    mostCommonUniqueWords.put(word, new AtomicInteger(0));
                }
                else {
                    for (Word commonUniqueWord : mostCommonUniqueWords.keySet()) {
                        if (Word.descending.compare(word, commonUniqueWord) == -1) {
                            mostCommonUniqueWords.remove(commonUniqueWord);
                            mostCommonUniqueWords.put(word, new AtomicInteger(0));
                            break;
                        }
                    }   
                }
            }
        }

        // Create word data constructor using analyzed information
        WordData data = new WordData(totalWordCount, onlyWordCount, words, mostCommonWords, mostCommonUniqueWords);

        long endTime   = System.nanoTime();
        ANALYZE_TIME = (float)(endTime - startTime) / 1_000_000_000;

        return data;
    }

    public static void lazyOutput(WordData data) throws IOException {

        File out = new File("lazyOutput.txt");
        if(!out.exists())
            out.createNewFile();

        PrintWriter pw = new PrintWriter(out);

        pw.printf("File: %s\n", inputFile);
        pw.printf("Parse Runtime: %.4f seconds\n", PARSE_TIME);
        pw.printf("Analyze Runtime: %.4f seconds\n", ANALYZE_TIME);
        pw.printf("Total Runtime: %.4f seconds\n", (PARSE_TIME + ANALYZE_TIME));
        pw.printf("\n");
        pw.printf("%s", data.fileOutput(DEBUG));            
        pw.close();
    }  
}
