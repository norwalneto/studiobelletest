package com.nwltecnologia.studiobelle.util;

import java.text.Normalizer;

public class StringUtils {

    public static String sanitize(String input) {

        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);

        return normalized
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
    }
}
