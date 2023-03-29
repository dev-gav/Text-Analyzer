package utility;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    // private static final int ARRAY_SIZE = 10;

    // private int totalWordCount;
    // private int onlyWordCount;
    // private List<Word> words;
    // private List<Word> mostCommonWords;

    public static final int ARRAY_SIZE = 10;

    public int totalWordCount;
    public int onlyWordCount;
    public List<Word> words;
    public List<Word> mostCommonWords; 
    
    public WordData() {
        this.totalWordCount = 0;
        this.onlyWordCount = 0;
        this.words = new ArrayList<Word>();
        this.mostCommonWords = new ArrayList<Word>();
    }

    public WordData(int totalWordCount, int onlyWordCount, List<Word> words, List<Word> mostCommonWords) {
        this.totalWordCount = totalWordCount;
        this.onlyWordCount = onlyWordCount;
        this.words = words;
        this.mostCommonWords = mostCommonWords;
    }

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

        int mostCommonTotal = calculateMostCommonTotal();
        output.append("Combined total of the " + ARRAY_SIZE + " most common words: " + mostCommonTotal + "\n");
        float top10wordsPercentage = (float)mostCommonTotal / (float)this.totalWordCount;
        output.append(String.format("This is %.2f%% of all %d words\n", top10wordsPercentage, this.totalWordCount)); // use '%%' to print '%'
        output.append(ARRAY_SIZE + " most common words:\n");
        Collections.sort(this.mostCommonWords, Word.descending);
        for (Word word : this.mostCommonWords) {
            output.append(word.word + " " + word.count + "\n");
        }
        output.append("\n");

        if (debug)
            output.append(debug());

        return output.toString();
    }

    private int calculateMostCommonTotal() {
        int total = 0;

        for (Word word : this.mostCommonWords) {
            total += word.count;
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
