package cz.hudecekpetr.snowride.generalpurpose;

import java.util.Arrays;

/**
 * Byte array builder. Analogous to StringBuilder, but for bytes.
 */
@SuppressWarnings("ForLoopReplaceableByForEach") // I don't trust the compiler's performance. It's important here.
public class ByteArrayBuilder {
    private byte[] backingArray = new byte[1024];
    private int endExclusive = 0;

    /**
     * Adds the bytes to the end of this byte builder.
     */
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

    /**
     * Gets the number of bytes in this builder.
     */
    public int length() {
        return endExclusive;
    }

    /**
     * Gets the bytes at the beginning of this builder up to the given number, exclusive.
     * The returned array is a copy. Changing it won't affect the builder.
     */
    public byte[] getBeginning(int upToIndexExclusive) {
        return subArray(0, upToIndexExclusive);
    }

    /**
     * Gets the bytes at the beginning of this builder from a given index, inclusive, up to the given second index, exclusive.
     * The returned array is a copy. Changing it won't affect the builder.
     */
    public byte[] subArray(int startFromInclusive, int upToExclusive) {
        return Arrays.copyOfRange(backingArray, startFromInclusive, upToExclusive); // can be optimized
    }


    /**
     * Deletes the given number of bytes from the beginning of the builder and shifts the remaining bytes left.
     */
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
