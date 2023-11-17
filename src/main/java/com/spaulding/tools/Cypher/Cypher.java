package com.spaulding.tools.Cypher;

import com.spaulding.tools.ASCII;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class Cypher {
    public static final int TYPE_ALPHA = 0;
    public static final int TYPE_ALPHA_NUMERIC = 1;
    public static final int TYPE_PRINTABLE = 2;
    private final String BANK;

    public Cypher() throws IllegalArgumentException {
        BANK = checkType(TYPE_PRINTABLE);
    }

    public Cypher(String bank) {
        BANK = bank;
    }

    public Cypher(int type) throws IllegalArgumentException {
        BANK = checkType(type);
    }

    private String checkType(int type) throws IllegalArgumentException {
        List<Integer> list = new ArrayList<>();
        switch (type) {
            case TYPE_ALPHA:
                list.addAll(IntStream.rangeClosed(65, 90).boxed().toList());
                list.addAll(IntStream.rangeClosed(97, 122).boxed().toList());
                return new String(list.stream().mapToInt(Integer::intValue).toArray(), 0, list.size());
            case TYPE_ALPHA_NUMERIC:
                list.addAll(IntStream.rangeClosed(48, 57).boxed().toList());
                list.addAll(IntStream.rangeClosed(65, 90).boxed().toList());
                list.addAll(IntStream.rangeClosed(97, 122).boxed().toList());
                return new String(list.stream().mapToInt(Integer::intValue).toArray(), 0, list.size());
            case TYPE_PRINTABLE:
                return new String(IntStream.rangeClosed(32, 126).toArray(), 0, 95);
            default:
                throw new IllegalArgumentException("Invalid Cypher Type!");
        }
    }

    public String createKey() {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < 64; i++) {
            result.append(ASCII.getRandomPrintable());
        }

        return result.toString();
    }

    public String encode(String value, String key) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return exec(value, key, true);
    }

    public String decode(String value, String key) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return exec(value, key, false);
    }

    private boolean isShiftable(String value) {
        for (char c : value.toCharArray()) {
            if (BANK.indexOf(c) == -1) {
                return false;
            }
        }
        return true;
    }

    private String exec(String value, String key, boolean toEncrypt) {
        if (!(ASCII.isPrintable(key) && ASCII.isPrintable(key))) {
            throw new IllegalArgumentException("Both value and key must contain printable ASCII characters.");
        }

        if (!isShiftable(value)) {
            throw new IllegalArgumentException("Value must contain ASCII characters of the provided shift bank.");
        }

        char[] cArray = value.toCharArray();
        int i = 0;
        for (; i < value.length(); i++) {
            int pos = BANK.indexOf(cArray[i]) + prepareShift(key.charAt(i % key.length()), (toEncrypt && i % 2 == 0) || (!toEncrypt && i % 2 != 0));
            pos += BANK.length();
            pos = pos % BANK.length();
            cArray[i] = BANK.charAt(pos);
        }

        for (; i < key.length(); i++) {
            int index = i % value.length();
            int pos = BANK.indexOf(cArray[index]) + prepareShift(key.charAt(i), (toEncrypt && i % 2 == 0) || (!toEncrypt && i % 2 != 0));
            pos += BANK.length();
            pos = pos % BANK.length();
            cArray[index] = BANK.charAt(pos);
        }

        return new String(cArray);
    }

    private int prepareShift(int shift, boolean flip) {
        shift = shift % BANK.length();
        return flip ? shift : -shift;
    }

}
