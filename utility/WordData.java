package utility;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class WordData {

    public static final int ARRAY_SIZE = 10;

    public int totalWordCount;
    public int onlyWordCount;
    public List<Entry<String, AtomicInteger>> wordCounts;
    public List<Entry<String, Integer>> mostCommonWords; 
    public List<Entry<String, Integer>> mostCommonUniqueWords;
    public List<Entry<String, Integer>> customWordsData; 
    
    public WordData(int totalWordCount, 
                    int onlyWordCount, 
                    List<Entry<String, AtomicInteger>> wordCounts, 
                    HashSet<String> customWords,
                    ConcurrentHashMap<String, Integer> customWordsData,
                    CHMWrapper mostCommonWords,  
                    CHMWrapper mostCommonUniqueWords) {

        this.totalWordCount = totalWordCount;
        this.onlyWordCount = onlyWordCount;
        this.wordCounts = wordCounts;

        // Creating array lists for word data
        this.mostCommonWords = new ArrayList<Entry<String, Integer>>();
        for (Entry<String, Integer> w : mostCommonWords.entrySet()) {
            this.mostCommonWords.add(w);
        }

        this.mostCommonUniqueWords = new ArrayList<Entry<String, Integer>>();        
        for (Entry<String, Integer> w : mostCommonUniqueWords.entrySet()) {
            this.mostCommonUniqueWords.add(w);
        }

        if(customWords == null){
            this.customWordsData = null;
            return;
        }

        this.customWordsData = new ArrayList<Entry<String, Integer>>();
        for (String customWord : customWords) {
            if (!customWordsData.containsKey(customWord)) {
                this.customWordsData.add(Map.entry(customWord, 0));
                continue;
            }

            Entry<String, Integer> wordWithCount = Map.entry(customWord, customWordsData.get(customWord));
            this.customWordsData.add(wordWithCount);
        }
    }

    public String fileOutput(boolean debug) {

        StringBuilder output = new StringBuilder();

        output.append("Word Count: " + this.totalWordCount + "\n");
        output.append("Unique words: " + this.wordCounts.size() + "\n");
        output.append("Number of words with a count of one: " + this.onlyWordCount + "\n");
        output.append("\n");

        if (this.customWordsData != null){
            output.append("User searched terms:\n");
            this.customWordsData.sort(descending);
            for (Entry<String, Integer> word : this.customWordsData) {
                output.append(word.getKey() + " " + word.getValue() + "\n");
            }
            output.append("\n");
        }

        this.mostCommonWords.sort(descending);
        int mostCommonTotal = calculateMostCommonTotal();
        output.append("Combined total of the " + ARRAY_SIZE + " most common words: " + mostCommonTotal + "\n");
        float top10wordsPercentage = (float)mostCommonTotal / (float)this.totalWordCount;
        output.append(String.format("This is %.2f%% of all %d words\n", top10wordsPercentage, this.totalWordCount)); // use '%%' to print '%'
        output.append(ARRAY_SIZE + " most common words:\n");
        for (Entry<String, Integer> word : this.mostCommonWords) {
            output.append(word.getKey() + " " + word.getValue() + "\n");
        }
        output.append("\n");

        output.append(ARRAY_SIZE + " most common unique words:\n");
        this.mostCommonUniqueWords.sort(descending);
        for (Entry<String, Integer> word : this.mostCommonUniqueWords) {
            output.append(word.getKey() + " " + word.getValue() + "\n");
        }

        if (debug)
            output.append(debug());

        return output.toString();
    }

    private int calculateMostCommonTotal() {
        int total = 0;

        for (Entry<String, Integer> word : this.mostCommonWords) {
            total += word.getValue();
        }

        return total;
    }

    private String debug() {

        StringBuilder debugStr = new StringBuilder();
        debugStr.append("\n\n(debug) all words:\n");

        this.wordCounts.sort(descendingAtomic);
        for (Entry<String, AtomicInteger> word : this.wordCounts) {
            debugStr.append(word.getKey() + " " + word.getValue() + "\n");
        }

        return debugStr.toString();
    }

    private Comparator<Entry<String, Integer>> descending = new Comparator<>() {
        @Override
        public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
            int countCompare = o2.getValue() - o1.getValue();
            return (countCompare != 0) ? countCompare : o2.getKey().compareTo(o1.getKey());
        }
    };

    private Comparator<Entry<String, AtomicInteger>> descendingAtomic = new Comparator<>() {
        @Override
        public int compare(Entry<String, AtomicInteger> o1, Entry<String, AtomicInteger> o2) {
            int countCompare = o2.getValue().get() - o1.getValue().get();
            return (countCompare != 0) ? countCompare : o2.getKey().compareTo(o1.getKey());
        }
    };
}
