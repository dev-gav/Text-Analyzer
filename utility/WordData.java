package utility;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

// TODO Pull methods out of WordData and delegate the 
// behavior to objects held by the AnalyzerThreads.
// If multiple threads are trying a single object
// with multiple synchronized blocks, all blocks
// must be free before the next thread can access.
// So this implementation is most likely slow.

// We probably instead just need to build everything in
// AnalyzerThreads and just assign everything to WordData
// in the constructor.

public class WordData {

    public static final int ARRAY_SIZE = 10;

    public int totalWordCount;
    public int onlyWordCount;
    public List<Word> words;
    public List<Word> mostCommonWords; 
    public List<Word> mostCommonCustomWords; 
    public List<Word> mostCommonUniqueWords;
    
    public WordData(int totalWordCount, int onlyWordCount, List<Word> words, ConcurrentHashMap<Word, AtomicInteger> mostCommonWords, ConcurrentHashMap<Word, AtomicInteger> mostCommonCustomWords, ConcurrentHashMap<Word, AtomicInteger> mostCommonUniqueWords) {
        this.totalWordCount = totalWordCount;
        this.onlyWordCount = onlyWordCount;
        this.words = words;

        // Creating array lists for word data
        this.mostCommonWords = new ArrayList<Word>();

        if(mostCommonCustomWords == null){
            this.mostCommonCustomWords = null;
        }
        else {
            this.mostCommonCustomWords = new ArrayList<Word>();
        }

        this.mostCommonUniqueWords = new ArrayList<Word>();
        
        // Setting array lists for word data
        for (Word w : mostCommonWords.keySet()) {
            this.mostCommonWords.add(w);
        }

        if (mostCommonCustomWords != null) {
            for (Word w : mostCommonCustomWords.keySet()) {
                this.mostCommonCustomWords.add(w);
            }
        }
        
        for (Word w : mostCommonUniqueWords.keySet()) {
            this.mostCommonUniqueWords.add(w);
        }
    }

    public void addToTotalCount(int amount) {
        this.totalWordCount += amount;
    }

    public void incrementOnlyWordCount() {
        this.onlyWordCount++;
    }

    public void addWord(Word word) {
        words.add(word);
    }

    public void checkCommonWords(Word word) {
        // If the arrays aren't full, add the word to both of them
        if (this.mostCommonWords.size() < ARRAY_SIZE) {
            this.mostCommonWords.add(word);
        }
        else {
            // Check most common words
            for (Word commonWord : this.mostCommonWords) {
                if (Word.descending.compare(word, commonWord) == -1) {
                    this.mostCommonWords.set(this.mostCommonWords.indexOf(commonWord), word);
                    return;
                }
            }
        }
    }

    public String fileOutput(boolean debug) {

        StringBuilder output = new StringBuilder();

        output.append("Word Count: " + this.totalWordCount + "\n");
        output.append("Unique words: " + this.words.size() + "\n");
        output.append("Number of words with a count of one: " + this.onlyWordCount + "\n");
        output.append("\n");

        Collections.sort(this.mostCommonWords, Word.descending);
        int mostCommonTotal = calculateMostCommonTotal();
        output.append("Combined total of the " + ARRAY_SIZE + " most common words: " + mostCommonTotal + "\n");
        float top10wordsPercentage = (float)mostCommonTotal / (float)this.totalWordCount;
        output.append(String.format("This is %.2f%% of all %d words\n", top10wordsPercentage, this.totalWordCount)); // use '%%' to print '%'
        output.append(ARRAY_SIZE + " most common words:\n");
        for (int i = 0; i < this.mostCommonWords.size(); i++) {
            if (i == WordData.ARRAY_SIZE) {
                break;
            }

            Word w = this.mostCommonWords.get(i);
            output.append(w.word + " " + w.count + "\n");
        }
        output.append("\n");


        if (this.mostCommonCustomWords != null){
            output.append(ARRAY_SIZE + " most common custom words:\n");
            Collections.sort(this.mostCommonCustomWords, Word.descending);
            for (int i = 0; i < this.mostCommonCustomWords.size(); i++) {
                if (i == WordData.ARRAY_SIZE) {
                    break;
                }

                Word w = this.mostCommonCustomWords.get(i);
                output.append(w.word + " " + w.count + "\n");
            }
            output.append("\n");
        }

        output.append(ARRAY_SIZE + " most common unique words:\n");
        Collections.sort(this.mostCommonUniqueWords, Word.descending);
        for (int i = 0; i < this.mostCommonUniqueWords.size(); i++) {
            if (i == WordData.ARRAY_SIZE) {
                break;
            }

            Word w = this.mostCommonUniqueWords.get(i);
            output.append(w.word + " " + w.count + "\n");
        }
        output.append("\n");

        if (debug)
            output.append(debug());

        return output.toString();
    }

    private int calculateMostCommonTotal() {
        int total = 0;

        Collections.sort(this.mostCommonWords, Word.descending);
        for (int i = 0; i < this.mostCommonWords.size(); i++) {
            if (i == WordData.ARRAY_SIZE) {
                break;
            }
            // System.out.println(i + " " + " total: " + total + " count: " + this.mostCommonWords.get(i).count);
            total += this.mostCommonWords.get(i).count;
        }

        return total;
    }

    private String debug() {

        StringBuilder debugStr = new StringBuilder();
        debugStr.append("\n\n(debug) all words:\n");

        Collections.sort(this.words, Word.descending);
        for (Word word : this.words) {
            debugStr.append(word.word + " " + word.count + "\n");
        }
        debugStr.append("\n");

        return debugStr.toString();
    }
}
