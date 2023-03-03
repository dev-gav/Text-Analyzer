package threads;
import java.util.List;

import utility.Counter;
import utility.Word;
import utility.WordData;

public class AnalyzerThread extends Thread {

    private Counter counter;
    private List<Word> wordCounts;
    private WordData data;

    public AnalyzerThread(Counter counter, List<Word> wordCounts, WordData data) {
        this.counter = counter;
        this.wordCounts = wordCounts;
        this.data = data;
    }

    @Override
    public void run() {
        int num = counter.getAndIncrement();
        while (num < wordCounts.size()) {

            Word word = wordCounts.get(num);

            // this is slow
            data.addToTotalCount(word.count);
            data.addWord(word);
            data.checkCommonWords(word);

            num = counter.getAndIncrement();
        }
    }
}
