public class Counter {
    private int value;

    public int getAndIncrement() {
        int temp;
        synchronized(this){
            temp = value;
            value = temp + 1;
        }
        return temp;
    }
}
