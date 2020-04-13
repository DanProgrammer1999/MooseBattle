package com.DanielDv99;

import java.io.BufferedWriter;
import java.io.FileWriter;

class Logger {
    private static BufferedWriter fileWriter = null;

    public static void log(String text) {
        if (Parameters.LOG_ENABLED) {
            System.out.println(text);
        }

        if (Parameters.FILE_LOGGING) {
            // Add another separator, as println adds it implicitly
            Logger.logToFile(text + System.lineSeparator());
        }
    }

    public static void playerLog(String playerName, String text) {
        log("\tPlayer " + playerName + text);
    }

    public static String asTable(String[] headers, String[] values) {
        return asTable(new String[][] {headers, values});
    }

    public static String asTable(String[][] rows) {
        if (rows.length == 0) {
            return "";
        }

        // Assume that ALL rows are of equal length
        var widths = new int[rows[0].length];

        for (int row = 0; row < rows.length; row++) {
            for (int col = 0; col < rows[row].length; col++) {
                widths[col] = Math.max(widths[col], rows[row][col].length());
            }
        }

        var res = new StringBuilder();

        for (int i = 0; i < rows.length; i++) {
            res.append("|");
            for (int j = 0; j < rows[i].length; j++) {
                res.append(String.format(" %-" + widths[j] + "s", rows[i][j])).append(" |");
            }
            res.append("\n");
        }

        // delete last '\n'
        res.deleteCharAt(res.length() - 1);
        return res.toString();
    }

    public static void printSeparator() {
        if (Parameters.LOG_ENABLED) {
            log(System.lineSeparator() + "-".repeat(50));
        }
    }

    public static String moveToString(int move) {
        switch (move) {
            case 1:
                return "A";
            case 2:
                return "B";
            case 3:
                return "C";
            default:
                return "ERROR";
        }
    }

    public static void cleanUp() {
        if (fileWriter == null) {
            return;
        }

        try {
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        fileWriter = null;
    }



    private static void logToFile(String message) {
        if (Logger.fileWriter == null) {
            try {
                fileWriter = new BufferedWriter(new FileWriter(Parameters.LOG_FILE_PATH));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            fileWriter.write(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
