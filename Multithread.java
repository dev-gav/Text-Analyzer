import java.util.List;

public class Multithread extends Thread {
    private Counter counter;
    private List<String> words;
    private int n;

    public Multithread(Counter counter, List<String> words, int n){
        this.counter = counter;
        this.words = words;
        this.n = n;
    }

    @Override
    public void run() {
        int num = counter.getAndIncrement();
        while(words.size() > num){
            // System.out.println("Thread " + n + ": " + words.get(num));
            num = counter.getAndIncrement();
        }
    }
}
