package threads;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import utility.Counter;
import utility.Word;
import utility.WordData;

public class AnalyzerThread extends Thread {

    private Counter counter;
    private ConcurrentHashMap<Word, AtomicInteger> commonEnglishWords;
    private List<Word> wordCounts;
    private AtomicInteger totalWordCount;
    private AtomicInteger onlyWordCount;
    private ConcurrentHashMap<Word, AtomicInteger> mostCommonWords;
    private ConcurrentHashMap<Word, AtomicInteger> mostCommonUniqueWords;

    public AnalyzerThread(Counter counter, ConcurrentHashMap<Word, AtomicInteger> commonEnglishWords, List<Word> wordCounts, AtomicInteger totalWordCount, AtomicInteger onlyWordCount, ConcurrentHashMap<Word, AtomicInteger> mostCommonWords,  ConcurrentHashMap<Word, AtomicInteger> mostCommonUniqueWords) {
        this.counter = counter;
        this.commonEnglishWords = commonEnglishWords;
        this.wordCounts = wordCounts;
        this.totalWordCount = totalWordCount;
        this.onlyWordCount =  onlyWordCount;
        this.mostCommonWords = mostCommonWords;
        this.mostCommonUniqueWords = mostCommonUniqueWords;
    }

    @Override
    public void run() {
        int num = counter.getAndIncrement();
        while (num < wordCounts.size()) {

            Word word = wordCounts.get(num);

            // Increment total count of words (addToTotalCount())
            this.totalWordCount.addAndGet(word.count);

            // Increment only word count (incrementOnlyWordCount())
            if (word.count == 1)
                this.onlyWordCount.addAndGet(1);

            // Check for common english words
            if (this.commonEnglishWords.containsKey(word)) {
                if (this.mostCommonWords.size() < WordData.ARRAY_SIZE) {
                    this.mostCommonWords.put(word, new AtomicInteger(0));
                }
                else {
                    for (Word commonWord : this.mostCommonWords.keySet()) {
                        if (Word.descending.compare(word, commonWord) == -1) {
                            this.mostCommonWords.remove(commonWord);
                            this.mostCommonWords.put(word, new AtomicInteger(0));
                            break;
                        }
                    }
                }
            }
            else {
                // Checks for common words that are not common in the English language
                if (this.mostCommonUniqueWords.size() < WordData.ARRAY_SIZE) {
                    this.mostCommonUniqueWords.put(word, new AtomicInteger(0));
                }
                else {
                    for (Word commonUniqueWord : this.mostCommonUniqueWords.keySet()) {
                        if (Word.descending.compare(word, commonUniqueWord) == -1) {
                            this.mostCommonUniqueWords.remove(commonUniqueWord);
                            this.mostCommonUniqueWords.put(word, new AtomicInteger(0));
                            break;
                        }
                    }   
                }
            }

            num = counter.getAndIncrement();
        }
    }
}
