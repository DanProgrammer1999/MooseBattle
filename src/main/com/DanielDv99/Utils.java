package com.DanielDv99;

public class Utils {
    public static double f(int x) {
        double exponent = Math.exp(x);
        return 10 * exponent / (1 + exponent);
    }

    public static double calcPlayerScore(int fieldScore) {
        return f(fieldScore) - f(0);
    }
}

