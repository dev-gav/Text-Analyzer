package threads;
import java.util.ArrayList;
import java.util.List;

import utility.Counter;
import utility.Word;
import utility.WordData;

public class AnalyzerThread extends Thread {

    private Counter counter;
    private List<Word> wordCounts;
    private WordData data;

    // Values taken from WordData.java
    private int totalWordCount;
    private int onlyWordCount;
    private List<Word> words;
    private List<Word> mostCommonWords; 

    public AnalyzerThread(Counter counter, List<Word> wordCounts, WordData data) {
        this.counter = counter;
        this.wordCounts = wordCounts;
        this.data = data;

        // Setting local variables to those in WordData
        this.totalWordCount = data.totalWordCount;
        this.onlyWordCount = data.onlyWordCount;
        this.words = data.words;
        this.mostCommonWords = data.mostCommonWords;
    }

    // Methods taken from WordData
    public synchronized void addToTotalCount(int amount) {
        this.totalWordCount += amount;
    }

    public synchronized void incrementOnlyWordCount() {
        this.onlyWordCount++;
    }

    public synchronized void addWord(Word word) {
        words.add(word);
    }

    public synchronized void checkCommonWords(Word word) {
        // If the arrays aren't full, add the word to both of them
        if (this.mostCommonWords.size() < WordData.ARRAY_SIZE) {
            // System.out.println("mostCommonWords.size(): " +  mostCommonWords.size());
            this.mostCommonWords.add(word);
        }
        else {
            // Check most common words
            for (Word commonWord : this.mostCommonWords) {
                if (Word.descending.compare(word, commonWord) == -1) {
                    // System.out.println("common word index: " + mostCommonWords.indexOf(commonWord));
                    this.mostCommonWords.set(this.mostCommonWords.indexOf(commonWord), word);
                    return;
                }
            }
            
        }
    }

    @Override
    public void run() {
        int num = counter.getAndIncrement();
        while (num < wordCounts.size()) {

            Word word = wordCounts.get(num);

            // Methods being accessed within AnalyzerThread
            addToTotalCount(word.count);
            addWord(word);
            checkCommonWords(word);

            num = counter.getAndIncrement();
        }

        // Create WordData here (?) passing in all components 
        // data = new WordData(totalWordCount, onlyWordCount, words, mostCommonWords);

        // Setting WordData variables to that of local variables
        data.totalWordCount = this.totalWordCount;
        data.onlyWordCount = this.onlyWordCount;
        data.words = this.words;
        data.mostCommonWords = this.mostCommonWords;
    }
}
