package com.spaulding.tools.Cypher;

import com.spaulding.tools.ASCII;
import com.spaulding.tools.Filer.Filer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class Cypher {
    public static final int TYPE_ALPHA = 0;
    public static final int TYPE_ALPHA_NUMERIC = 1;
    public static final int TYPE_PRINTABLE = 2;

    private final String BASIC_ENCRYPTION_KEY = "n5 tp:b]gb\"5y%HsQ<J<zo/ODd~~i6H-P'*4d/\"b=u6V3ifj/rn9*#9H~J2*B#Td";
    private String FILE_ENCRYPTION_KEY, BANK;

    public Cypher(String name) throws IllegalArgumentException {
        init(name, checkType(TYPE_PRINTABLE));
    }

    public Cypher(String name, String bank) {
        init(name, bank);
    }

    public Cypher(String name, int type) throws IllegalArgumentException {
        init(name, checkType(type));
    }

    private void init(String name, String bank) {
        this.BANK = bank;
        List<String> toWrite = new ArrayList<>();
        toWrite.add(standardEncode(createKey()));
        toWrite.add("DO NOT DELETE OR EDIT THIS FILE UNLESS YOU KNOW WHAT YOU ARE DOING");
        toWrite.add("This file is used as a reference for an encrypted key used for later");
        toWrite.add("decryption via the Cypher tool. This is typically not ever changed if");
        toWrite.add("there has been any data that has been encrypted and stored.");
        List<String> results = Filer.createFile(name.toUpperCase() + "-ENCRYPTION-KEY.txt", toWrite);
        if (results != null && !results.isEmpty()) {
            if (results.get(0).startsWith("Error:")) {
                throw new RuntimeException(String.join(". ", results));
            }
            else {
                FILE_ENCRYPTION_KEY = standardDecode(results.get(0));
                return;
            }
        }

        throw new RuntimeException("Unable to retrieve encryption key from file " + name + ".txt");
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

    public String standardEncode(String value) {
        return exec(value, BASIC_ENCRYPTION_KEY, true);
    }

    public String standardDecode(String value) {
        return exec(value, BASIC_ENCRYPTION_KEY, false);
    }

    public String encode(String value, String key) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        value = exec(value, key, true);
        value = exec(value, FILE_ENCRYPTION_KEY, true);
        return standardEncode(value);
    }

    public String decode(String value, String key) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        value = standardDecode(value);
        value = exec(value, FILE_ENCRYPTION_KEY, false);
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
