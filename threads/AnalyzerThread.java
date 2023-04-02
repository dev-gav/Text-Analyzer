package threads;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import utility.CHMWrapper;
import utility.Counter;

public class AnalyzerThread extends Thread {

    private Counter counter;
    private HashSet<String> commonEnglishWords;
    private HashSet<String> customWords;

    private List<Entry<String, AtomicInteger>> wordCounts;
    private AtomicInteger totalWordCount;
    private AtomicInteger onlyWordCount;
    private ConcurrentHashMap<String, Integer> customWordsData;
    private CHMWrapper mostCommonWords;
    private CHMWrapper mostCommonUniqueWords;

    public AnalyzerThread(Counter counter, 
                          HashSet<String> commonEnglishWords, 
                          HashSet<String> customWords, 
                          List<Entry<String, AtomicInteger>> wordCounts, 
                          AtomicInteger totalWordCount, 
                          AtomicInteger onlyWordCount, 
                          ConcurrentHashMap<String, Integer> customWordsData,  
                          CHMWrapper mostCommonWords,  
                          CHMWrapper mostCommonUniqueWords) {
        
    
        this.counter = counter;

        this.commonEnglishWords = commonEnglishWords;
        this.customWords = customWords;

        this.wordCounts = wordCounts;
        this.totalWordCount = totalWordCount;
        this.onlyWordCount =  onlyWordCount;

        this.customWordsData = customWordsData;
        this.mostCommonWords = mostCommonWords;
        this.mostCommonUniqueWords = mostCommonUniqueWords;
    }

    @Override
    public void run() {
        int num = this.counter.getAndIncrement();
        while (num < this.wordCounts.size()) {

            Entry<String, AtomicInteger> word = this.wordCounts.get(num);

            // Increment total count of words (addToTotalCount())
            this.totalWordCount.addAndGet(word.getValue().get());

            // Increment only word count (incrementOnlyWordCount())
            if (word.getValue().get() == 1)
                this.onlyWordCount.incrementAndGet();

            // Check against other common english words
            if (this.commonEnglishWords.contains(word.getKey())) {
                this.mostCommonWords.check(Map.entry(word.getKey(), word.getValue().get()));
            }
            // Checks against other common words that are not common in the English language
            else {
                this.mostCommonUniqueWords.check(Map.entry(word.getKey(), word.getValue().get()));
            }

            if ((this.customWords != null) && (this.customWords.contains(word.getKey()))) {
                this.customWordsData.put(word.getKey(), word.getValue().get()); 
            }

            num = this.counter.getAndIncrement();
        }
    }
}
