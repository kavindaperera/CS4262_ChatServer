package com.codewizards.utils;

import lombok.NonNull;

public class Utils {

    public static boolean validateIdentifier(@NonNull String id) {
        return regexCheck(id);
    }

    private static boolean regexCheck(String str){
        return str.matches("^[a-zA-Z][a-zA-Z0-9]{3,16}");
    }

    private static boolean isAlphaNumeric(String str) {
        return str.matches("[a-zA-Z0-9]+");
    }

}
