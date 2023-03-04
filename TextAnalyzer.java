// COP4520 Term Project
import java.io.*;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import threads.AnalyzerThread;
import threads.ConverterThread;
import threads.ParserThread;
import utility.Counter;
import utility.Word;
import utility.WordData;

import java.util.ArrayList;
import java.util.List;

// For an overview of what we are doing that's much easier to grasp,
// check the LazyTextAnalyzer.java file for a single-threaded implementation

// Current bugs:
// The single threaded implementation is faster at both parsing and analyzing :(
// WordData's methods need to be put in AnalyzerThread's run method to hopefully improve analyzing speed, check WordData.java

class TextAnalyzer {

    // prints out all words if true
    public static final boolean DEBUG = true;

    // TODO maybe switch from using inputFile and NUM_THREADS to args[x] at some point?
    public static final String inputFile = "text.txt";
    private static final int NUM_THREADS = 8;

    private static float PARSE_TIME;
    private static float ANALYZE_TIME;

    public static void main(String[] args) throws IOException {

        // Open file
        File file = new File(inputFile);
        Scanner input = new Scanner(file); 
        List<String> text = new ArrayList<String>();

        while (input.hasNext())
            text.add(input.next());
        input.close();

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
        long endTime   = System.nanoTime();
        PARSE_TIME = (float)(endTime - startTime) / 1_000_000_000;

        return wordCounts;
    }

    // Given a list of Words, update a WordData object
    // with valuable information about the Word list.
    private static WordData analyzeWords(List<Word> words) {

            // Create threads
            AnalyzerThread[] analyzers = new AnalyzerThread[NUM_THREADS];
            Counter analyzerCounter = new Counter();
            WordData data = new WordData();

            // Start timer
            long startTime = System.nanoTime();
    
            for(int i = 0; i < NUM_THREADS; i++)
                analyzers[i] = new AnalyzerThread(analyzerCounter, words, data);
    
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

            return data;
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