// COP4520 Term Project
import java.io.*;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import threads.AnalyzerThread;
import threads.ParserThread;
import utility.Counter;
import utility.Word;
import utility.WordData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// For an overview of what we are doing that's much easier to grasp,
// check the LazyTextAnalyzer.java file for a single-threaded implementation

// Current bugs:
// The single threaded implementation is faster at both parsing and analyzing :(
// WordData's methods need to be put in AnalyzerThread's run method to hopefully improve analyzing speed, check WordData.java

class TextAnalyzer {

    // Stops threads
    public static boolean endProgram = false;

    // prints out all words if true
    public static final boolean DEBUG = true;

    // TODO maybe switch from using inputFile and NUM_THREADS to args[x] at some point?
    public static String inputFile = "text.txt";
    public static final String mostCommonWordsFile = "mostCommonWords.txt";
    private static final int NUM_THREADS = 8;

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
            } catch (Exception e) {
                System.out.println("The text file give to analyze does not exist.");
                invalidInput = true;
                return;
            } finally {
                scanner.close();
            }

        } while (invalidInput);
        
        // System.out.println("Would you like to use a custom list of words to analyze, as well as those in the English dictionary? y/n");
        // if (scanner.nextLine().equalsIgnoreCase("y"))
        // {
        //     System.out.println("What is the name of your text file containing your list of words?");
        //     String customWordFileName = scanner.nextLine();
        // }

        // Count number of times each word appears
        ConcurrentHashMap<String, AtomicInteger> wordCounts = countWords(text); 

        // Turn hashmap into list of Word objects
        // We could potentially parallelize this too
        List<Word> words = new ArrayList<Word>();
        for (String word : wordCounts.keySet()) {
            words.add(new Word(word, wordCounts.get(word).get()));
        }

        WordData data = analyzeWords(words);

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
            parsers[i] = new ParserThread(i, parserCounter, words, wordCounts);

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
    private static WordData analyzeWords(List<Word> words) throws FileNotFoundException {
        
        // Reads common English words and puts them in a hash map
        ConcurrentHashMap<String, AtomicInteger> mcwMap = readWords(mostCommonWordsFile) ; 

        // Create threads
        AnalyzerThread[] analyzers = new AnalyzerThread[NUM_THREADS];
        Counter analyzerCounter = new Counter();

        // Creating variables to pass to analyzer threads
        AtomicInteger totalWordCount = new AtomicInteger(0);
        AtomicInteger onlyWordCount = new AtomicInteger(0);
        List<Word> mostCommonWords = Collections.synchronizedList(new ArrayList<Word>()); 
        List<Word> mostCommonUniqueWords = Collections.synchronizedList(new ArrayList<Word>()); 

        for(int i = 0; i < NUM_THREADS; i++)
            analyzers[i] = new AnalyzerThread(analyzerCounter, mcwMap, words, totalWordCount, onlyWordCount, mostCommonWords, mostCommonUniqueWords);
            
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

        return new WordData(totalWordCount.get(), onlyWordCount.get(), words, mostCommonWords, mostCommonUniqueWords);
    }

    public static ConcurrentHashMap<String, AtomicInteger> readWords(String filename) throws FileNotFoundException {
        // Read in most common words
        File file = new File(mostCommonWordsFile);
        Scanner input = new Scanner(file); 
        
        ConcurrentHashMap<String, AtomicInteger> map = new ConcurrentHashMap<>(); 
        while (input.hasNext()) {
            map.put(input.next(), new AtomicInteger(1));
        }
        
        input.close();

        return map;
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