package dialight.easynms.mapping.utils;

import java.util.Iterator;

public class CharIterator implements Iterator<Character> {

    private final char[] chars;
    private int index;

    public CharIterator(String str) {
        chars = str.toCharArray();
        index = 0;
    }

    @Override public boolean hasNext() {
        return index < chars.length;
    }

    @Override public Character next() {
        return chars[index++];
    }

    @Override
    public String toString() {
        final var sb = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            if(i == index) sb.append('^');
            sb.append(chars[i]);
        }
        if(chars.length == index) sb.append('^');
        return sb.toString();
    }
}
