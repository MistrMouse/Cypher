package com.spaulding.tools.Cypher.objects;

import com.spaulding.tools.Cypher.Cypher;
import com.spaulding.tools.Filer.Filer;

import java.util.ArrayList;
import java.util.List;

public class FileEncryptionKey {

    public final String value;
    public FileEncryptionKey(String fileName) {
        List<String> results = new ArrayList<>();
        Cypher cypher = new Cypher();
        results.add(cypher.createKey());
        results.add("DO NOT DELETE OR EDIT THIS FILE UNLESS YOU KNOW WHAT YOU ARE DOING");
        results.add("This file is used as a reference for an encrypted key used for later");
        results.add("decryption via the Cypher tool. This is typically not ever changed if");
        results.add("there has been any data that has been encrypted and stored.");
        results = Filer.createFile(fileName, results);

        if (results != null && !results.isEmpty()) {
            if (results.get(0).startsWith("Error:")) {
                throw new RuntimeException(String.join(". ", results));
            }
            else {
                value = results.get(0);
                return;
            }
        }

        throw new RuntimeException("Unable to retrieve encryption key from file " + fileName + ".txt");
    }

}
