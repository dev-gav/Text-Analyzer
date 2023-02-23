// COP4520 Term Project
import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

class TextAnalyzer{
    public static void main(String[] args) throws IOException{
        int numThreads = 8;
        Multithread threads[] = new Multithread[numThreads]; 
        Counter counter = new Counter();
        List<String> words = new ArrayList<>();

        //Start timer
        long startTime = System.nanoTime();

        // Open file
        File text = new File("text.txt");
        Scanner input = new Scanner(text); 

        // Read text and add each word to list
        while (input.hasNext()) {
            String word  = input.next();
            words.add(word);
        }
        input.close();

        // Start all threads
        for(int i = 0; i < numThreads; i++){
            threads[i] = new Multithread(counter, words, i);
            threads[i].start();
        }

        //Wait for all threads to finish
        for(int i = 0; i < numThreads; i++){
            try {
                threads[i].join();
            } catch (InterruptedException e) {
            }
        }

        try{
            File out = new File("output.txt");

            if(!out.exists()){
                out.createNewFile();
            }

            PrintWriter pw = new PrintWriter(out);

            //Stop timer
            long endTime   = System.nanoTime();
            long totalTime = endTime - startTime;
            float totalTimeInSecond = (float)totalTime / 1_000_000_000;

            pw.printf("Runtime: %.2f seconds", totalTimeInSecond);            
            pw.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}