package utility;
import java.util.Comparator;

public class Word {
    
    public String word;
    public int count;

    public Word(String word, int count) {
        this.word = word;
        this.count = count;
    }

    @Override 
    public int hashCode() {
        return word.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != this.getClass()) {
            return false;
        }

        Word other = (Word)obj;

        if (other.word.equals(this.word))
            return true;

        return false;
    }

    // Comparers
    public static final Comparator<Word> ascending = new Comparator<Word>() {
        @Override
        // Checks if Word x is greater than Word y
        public int compare(Word x, Word y) {
            // Ascending count and word length check
            int countCompare = ((x.count > y.count) ? 1 : ((x.count < y.count) ? -1 : 0));
            if (countCompare != 0)
                return countCompare;

            // If they have the same count, take the longest word?
            // We can probably make this more meaningful somehow.
            int wordLengthCompare = ((x.word.length() > y.word.length()) ? -1 : ((x.word.length() < y.word.length()) ? 1 : 0));
            return wordLengthCompare;
        }
    };

    public static final Comparator<Word> descending = new Comparator<Word>() {
        @Override
        // Checks if Word x is has a lower count than Word y,
        // but is a longer word.
        public int compare(Word x, Word y) {
            // Descending count check, ascending word length check
            int countCompare = ((x.count > y.count) ? -1 : ((x.count < y.count) ? 1 : 0));
            if (countCompare != 0)
                return countCompare;

            // Still taking the longest word here.
            int wordLengthCompare = ((x.word.length() > y.word.length()) ? -1 : ((x.word.length() < y.word.length()) ? 1 : 0));
            return wordLengthCompare;
        }
    };
}
