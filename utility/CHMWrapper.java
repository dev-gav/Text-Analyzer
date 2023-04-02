package utility;

import java.util.concurrent.ConcurrentHashMap;

public class CHMWrapper extends ConcurrentHashMap<String, Integer> {
    
    public CHMWrapper(int numThreads) {
        super(16, 0.75f, numThreads);

        // Fill the HashMap with dummy fields.
        // UUIDs so the hashes are different.
        for(int i = 0; i < WordData.ARRAY_SIZE; i++) {
            this.put(java.util.UUID.randomUUID().toString(), 0);
        }
    }

    public synchronized void check(Entry<String, Integer> newEntry) {
        Entry<String, Integer> temp = newEntry;
        for (Entry<String, Integer> currentEntry : this.entrySet()) {
            if (temp.getValue() > currentEntry.getValue()) {
                temp = currentEntry;
            }
        }

        // We couldn't find anything with a lower count
        if (temp.getKey().equals(newEntry.getKey())) {
            return;
        }

        this.remove(temp.getKey());
        this.put(newEntry.getKey(), newEntry.getValue());
    }
}
