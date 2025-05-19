package com.rits.integration.util;

import java.util.Base64;

public class EncodingUtils {

    // Method to encode a given string to Base64
    public static String encodeToBase64(String input) {
        if (input == null || input.isEmpty()) {
            return input; // If input is null or empty, return as-is
        }
        return Base64.getEncoder().encodeToString(input.getBytes());
    }

    // Method to decode a Base64 string back to its original value
    public static String decodeFromBase64(String encoded) {
        if (encoded == null || encoded.isEmpty()) {
            return encoded; // If encoded value is null or empty, return as-is
        }
        return new String(Base64.getDecoder().decode(encoded));
    }
}
