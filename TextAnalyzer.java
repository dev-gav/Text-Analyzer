// COP4520 Term Project
import java.io.*;
import java.util.Scanner;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import threads.AnalyzerThread;
import threads.ParserThread;
import utility.CHMWrapper;
import utility.Counter;
import utility.WordData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

// A WAY TO EXTEND THIS PROJECT
// also parse the user input so that someone
// could compare two input files

// For an overview of what we are doing that's much easier to grasp,
// check the LazyTextAnalyzer.java file for a single-threaded implementation

// Current bugs:
// The single threaded implementation is faster at both parsing and analyzing :(
// WordData's methods need to be put in AnalyzerThread's run method to hopefully improve analyzing speed, check WordData.java

class TextAnalyzer {

    // Stops threads
    public static boolean endProgram = false;

    // prints out all words if true
    public static final boolean DEBUG = false;

    public static String inputFile = "";
    public static String customWordsFile = "";
    public static final String mostCommonWordsFile = "mostCommonWords.txt";
    public static final int NUM_THREADS = 8;

    private static float PARSE_TIME;
    private static float ANALYZE_TIME;

    public static void main(String[] args) throws IOException {

        boolean invalidInput = false;
        Scanner scanner = new Scanner(System.in);
        List<String> text = new ArrayList<String>();

        // keep asking for a text file to analyze while the input given in invalid
        do {
            System.out.println("What text would you like to analyze?");
            String textName = scanner.nextLine();
            inputFile = textName;

            try {
                File file = new File(inputFile);
                Scanner input = new Scanner(file); 

                while (input.hasNext())
                    text.add(input.next());
                input.close();

                // Asks for input text of words to analyze
                System.out.println("Would you like to use a custom list of words to analyze, as well as those common in the English dictionary? y/n");
                if (scanner.nextLine().equalsIgnoreCase("y")) {
                    
                    System.out.println("What is the name of your text file containing your list of words?");
                    customWordsFile = scanner.nextLine();
                }
                else {
                    customWordsFile = "";
                }

            } catch (Exception e) {
                System.out.println("The text file give to analyze does not exist.");
                invalidInput = true;
                return;
            } 
            finally {
                scanner.close();
            }

        } while (invalidInput);
        
        // Count number of times each word appears
        ConcurrentHashMap<String, AtomicInteger> wordCounts = countWords(text); 
        
        // Get useful data from the word counts
        WordData data = analyzeWords(wordCounts.entrySet());
        
        outputToFile(data);

        // Run single-threaded version (prints to lazyOutput.txt)
        LazyTextAnalyzer.lazyMain(text);
    }

    // Read the file using ParserThreads and return a hashmap
    // containing each word and their count.
    private static ConcurrentHashMap<String, AtomicInteger> countWords(List<String> words) {

        // Create threads
        ParserThread parsers[] = new ParserThread[NUM_THREADS]; 
        Counter parserCounter = new Counter();

        ConcurrentHashMap<String, AtomicInteger> wordCounts = new ConcurrentHashMap<String, AtomicInteger>(16, 0.75f, NUM_THREADS);
        
        for(int i = 0; i < NUM_THREADS; i++)
            parsers[i] = new ParserThread(parserCounter, words, wordCounts);

        // Start timer 
        long startTime = System.nanoTime();

        // Start threads
        for (int i = 0; i < NUM_THREADS; i++)
            parsers[i].start();

        // Wait for threads to finish
        for(int i = 0; i < NUM_THREADS; i++){
            try {
                parsers[i].join();
            } catch (InterruptedException e) {
            }
        }

        // Stop timer
        long endTime = System.nanoTime();
        PARSE_TIME = (float)(endTime - startTime) / 1_000_000_000;

        return wordCounts;
    }

    // Given a list of Words, update a WordData object
    // with valuable information about the Word list.
    private static WordData analyzeWords(Set<Entry<String, AtomicInteger>> wordCounts) throws FileNotFoundException {
        
        List<Entry<String, AtomicInteger>> wordCountsList = new ArrayList<>();
        wordCountsList.addAll(wordCounts);

        // Reads common English words and puts them in a hash map
        HashSet<String> mostCommonEnglishWords = readWords(mostCommonWordsFile); 

        // Reads custom words from list and puts them in a hash map
        HashSet<String> customWords = null;
        ConcurrentHashMap<String, Integer> customWordsData = null;
        if(!customWordsFile.isEmpty()) {
            customWords = readWords(customWordsFile); 
            customWordsData = new ConcurrentHashMap<String, Integer>(16, 0.75f, NUM_THREADS);
        }

        AtomicInteger totalWordCount = new AtomicInteger(0);
        AtomicInteger onlyWordCount = new AtomicInteger(0);
        CHMWrapper mostCommonWords = new CHMWrapper(NUM_THREADS);
        CHMWrapper mostCommonUniqueWords = new CHMWrapper(NUM_THREADS);

        // Create threads
        AnalyzerThread[] analyzers = new AnalyzerThread[NUM_THREADS];
        Counter analyzerCounter = new Counter();

        for(int i = 0; i < NUM_THREADS; i++)
            analyzers[i] = new AnalyzerThread(analyzerCounter, mostCommonEnglishWords, customWords, wordCountsList, totalWordCount, onlyWordCount, customWordsData, mostCommonWords, mostCommonUniqueWords);
            
        // Start timer
        long startTime = System.nanoTime();

        // Start threads
        for (int i = 0; i < NUM_THREADS; i++)
            analyzers[i].start();

        // Wait for threads to finish
        for(int i = 0; i < NUM_THREADS; i++){
            try {
                analyzers[i].join();
            } catch (InterruptedException e) {
            }
        }

        // Stop timer
        long endTime   = System.nanoTime();
        ANALYZE_TIME = (float)(endTime - startTime) / 1_000_000_000;

        return new WordData(totalWordCount.get(), onlyWordCount.get(), wordCountsList, customWords, customWordsData, mostCommonWords, mostCommonUniqueWords);
    }

    public static HashSet<String> readWords(String filename) throws FileNotFoundException {
        // Read in most common words
        File file = new File(filename);
        Scanner input = new Scanner(file); 
        
        HashSet<String> set = new HashSet<String>(); 
        while (input.hasNext()) {
            set.add(input.next());
        }
        
        input.close();
        return set;
    }
    
    private static void outputToFile(WordData data) {
        try {

            File out = new File("output.txt");
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

        } catch (IOException e){
            e.printStackTrace();
        }
    }  
}