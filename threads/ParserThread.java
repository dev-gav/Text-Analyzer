package threads;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import utility.Counter;

// The job of the ParserThread is to read the file
// and add each word to a hashmap as the key.
// The value in the hashmap is that word's count.

public class ParserThread extends Thread {

    private Counter counter;
    private List<String> words;
    private ConcurrentHashMap<String, AtomicInteger> wordCounts;

    // Regices
    // Match words https://regexr.com/796mo
    // Click the settings icon in the top left
    // and read description for info.
    private static final String wordRegex = "\\w((('|-)(?=\\w))|\\w)*";
    private static final Pattern wordPattern = Pattern.compile(wordRegex);

    // Match numbers https://regexr.com/796nj
    private static final String numRegex = "\\d(((\\.|\\/)(?=\\d))|\\d)*";
    private static final Pattern numPattern = Pattern.compile(numRegex);

    // Match any letter
    // We use this to avoid any overlap between the two regices above.
    // The wordRegex can match numbers and the numRegex would just pull
    // any number present in the string, leaving letters behind. 
    // So neither one should go first. For flow, check run().
    private static final String letterRegex = "[A-z]";
    private static final Pattern letterPattern = Pattern.compile(letterRegex);

    public ParserThread(Counter counter, List<String> words, ConcurrentHashMap<String, AtomicInteger> wordCounts){
        this.counter = counter;
        this.words = words;
        this.wordCounts = wordCounts;
    }

    @Override
    public void run() {

        Matcher numMatch = null;
        Matcher wordMatch = null;

        String word = "";
        String parse = "";
        int num = this.counter.getAndIncrement();

        while (num < this.words.size()) {

            word = this.words.get(num);

            // If the string has any letters, try the word regex.
            // Otherwise, try the number regex.
            if (letterPattern.matcher(word).find()) {
                wordMatch = wordPattern.matcher(word);

                if (wordMatch.find()) {

                    parse = word.substring(wordMatch.start(), wordMatch.end());
                    parse = parse.toLowerCase();

                    this.wordCounts.putIfAbsent(parse, new AtomicInteger(0));
                    this.wordCounts.get(parse).incrementAndGet();
                }
            }
            else {
                numMatch = numPattern.matcher(word);

                if (numMatch.find()) {
                    parse = word.substring(numMatch.start(), numMatch.end());

                    this.wordCounts.putIfAbsent(parse, new AtomicInteger(0));
                    this.wordCounts.get(parse).incrementAndGet();

                }
            }

            num = counter.getAndIncrement();
        }
    }
}
