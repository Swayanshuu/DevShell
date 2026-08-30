package com.devcli.ui;

public class AnsiStyle {
    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String DIM = "\u001B[2m";
    public static final String ITALIC = "\u001B[3m";
    public static final String UNDERLINE = "\u001B[4m";

    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";
    public static final String GRAY = "\u001B[90m";

    public static final String BRIGHT_RED = "\u001B[91m";
    public static final String BRIGHT_GREEN = "\u001B[92m";
    public static final String BRIGHT_YELLOW = "\u001B[93m";
    public static final String BRIGHT_BLUE = "\u001B[94m";
    public static final String BRIGHT_MAGENTA = "\u001B[95m";
    public static final String BRIGHT_CYAN = "\u001B[96m";
    public static final String BRIGHT_WHITE = "\u001B[97m";

    public static final String BG_BLUE = "\u001B[44m";
    public static final String BG_CYAN = "\u001B[46m";
    public static final String BG_MAGENTA = "\u001B[45m";

    private static boolean colorEnabled = true;

    public static void setColorEnabled(boolean enabled) {
        colorEnabled = enabled;
    }

    public static boolean isColorEnabled() {
        return colorEnabled;
    }

    private static String apply(String code, String text) {
        if (!colorEnabled || text == null) return text != null ? text : "";
        return code + text + RESET;
    }

    public static String bold(String text) { return apply(BOLD, text); }
    public static String dim(String text) { return apply(DIM, text); }
    public static String gray(String text) { return apply(GRAY, text); }
    public static String green(String text) { return apply(GREEN, text); }
    public static String brightGreen(String text) { return apply(BRIGHT_GREEN, text); }
    public static String red(String text) { return apply(RED, text); }
    public static String brightRed(String text) { return apply(BRIGHT_RED, text); }
    public static String yellow(String text) { return apply(YELLOW, text); }
    public static String brightYellow(String text) { return apply(BRIGHT_YELLOW, text); }
    public static String cyan(String text) { return apply(CYAN, text); }
    public static String brightCyan(String text) { return apply(BRIGHT_CYAN, text); }
    public static String blue(String text) { return apply(BLUE, text); }
    public static String brightBlue(String text) { return apply(BRIGHT_BLUE, text); }
    public static String magenta(String text) { return apply(MAGENTA, text); }
    public static String brightMagenta(String text) { return apply(BRIGHT_MAGENTA, text); }

    public static String boldCyan(String text) { return apply(BOLD + BRIGHT_CYAN, text); }
    public static String boldGreen(String text) { return apply(BOLD + BRIGHT_GREEN, text); }
    public static String boldMagenta(String text) { return apply(BOLD + BRIGHT_MAGENTA, text); }
    public static String boldYellow(String text) { return apply(BOLD + BRIGHT_YELLOW, text); }
    public static String boldRed(String text) { return apply(BOLD + BRIGHT_RED, text); }
    public static String boldWhite(String text) { return apply(BOLD + BRIGHT_WHITE, text); }
    public static String brightWhite(String text) { return apply(BRIGHT_WHITE, text); }

    public static String hyperlink(String text, String url) {
        if (!colorEnabled || text == null) return text != null ? text : "";
        return "\u001B]8;;" + url + "\u001B\\" + text + "\u001B]8;;\u001B\\";
    }

    public static String badge(String text, String colorCode) {
        if (!colorEnabled) return "[" + text + "]";
        return colorCode + " " + text + " " + RESET;
    }
}
