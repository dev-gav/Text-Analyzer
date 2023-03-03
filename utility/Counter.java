package utility;
public class Counter {
    private int value;

    public Counter() {
        this.value = 0;
    }

    public int getAndIncrement() {
        int temp;
        synchronized(this){
            temp = value;
            value = temp + 1;
        }
        return temp;
    }
}
