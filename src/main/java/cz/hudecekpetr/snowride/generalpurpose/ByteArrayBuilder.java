package cz.hudecekpetr.snowride.generalpurpose;

import java.util.Arrays;

@SuppressWarnings("ForLoopReplaceableByForEach") // I don't trust the compiler's performance. It's important here.
public class ByteArrayBuilder {
    private byte[] backingArray = new byte[1024];
    private int endExclusive = 0;

    public void append(byte[] appendThisToEnd) {
        int newEndExclusive = endExclusive + appendThisToEnd.length;
        if (newEndExclusive >  backingArray.length) {
            reallocate(newEndExclusive * 2);
        }
        int endOfArray = endExclusive;
        for (int i = 0; i < appendThisToEnd.length; i++) {
            backingArray[endOfArray] = appendThisToEnd[i];
            endOfArray++;
        }
        endExclusive = endOfArray;
    }

    private void reallocate(int newCapacity) {
        backingArray = Arrays.copyOf(backingArray, newCapacity);
    }

    public int length() {
        return endExclusive;
    }

    public byte[] getBeginning(int upToIndexExclusive) {
        return subArray(0, upToIndexExclusive);
    }

    public byte[] subArray(int startFromInclusive, int upToExclusive) {
        return Arrays.copyOfRange(backingArray, startFromInclusive, upToExclusive); // can be optimized
    }

    public void deleteFromStart(int count) {
        // Copy to the left:
        int whereTo = 0;
        for (int i = count; i < endExclusive; i++, whereTo++) {
            backingArray[whereTo] = backingArray[i];
        }
        // Decrease size:
        endExclusive -= count;
    }
}
