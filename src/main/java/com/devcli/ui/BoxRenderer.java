package com.devcli.ui;

import java.util.ArrayList;
import java.util.List;

public class BoxRenderer {

    public static String stripAnsi(String text) {
        if (text == null) return "";
        String cleaned = text.replaceAll("\u001B\\[[;\\d]*m", "");
        cleaned = cleaned.replaceAll("\u001B\\]8;;[^\u001B]*\u001B\\\\", "");
        return cleaned;
    }

    public static int visibleLength(String text) {
        return stripAnsi(text).length();
    }

    public static void printAsciiBanner() {
        String swynxLink = AnsiStyle.hyperlink("SWYNX", "https://swynx.dev");
        System.out.println(AnsiStyle.boldCyan("╭─────────────────────────────────────────────────────────────╮"));
        System.out.println(AnsiStyle.boldCyan("│                                                             │"));
        System.out.println(AnsiStyle.boldCyan("│   ██████╗ ███████╗██╗   ██╗███████╗██╗  ██╗███████╗██╗      │"));
        System.out.println(AnsiStyle.boldCyan("│   ██╔══██╗██╔════╝██║   ██║██╔════╝██║  ██║██╔════╝██║      │"));
        System.out.println(AnsiStyle.boldCyan("│   ██║  ██║█████╗  ██║   ██║███████╗███████║█████╗  ██║      │"));
        System.out.println(AnsiStyle.boldCyan("│   ██║  ██║██╔══╝  ╚██╗ ██╔╝╚════██║██╔══██║██╔══╝  ██║      │"));
        System.out.println(AnsiStyle.boldCyan("│   ██████╔╝███████╗ ╚████╔╝ ███████║██║  ██║███████╗███████╗ │"));
        System.out.println(AnsiStyle.boldCyan("│   ╚═════╝ ╚══════╝  ╚═══╝  ╚══════╝╚═╝  ╚═╝╚══════╝╚══════╝ │"));
        System.out.println(AnsiStyle.boldCyan("│                                                             │"));
        System.out.println(AnsiStyle.boldCyan("│") + AnsiStyle.gray("      Your Personal Developer Command Center • ") + AnsiStyle.boldCyan(swynxLink) + AnsiStyle.gray("         ") + AnsiStyle.boldCyan("│"));
        System.out.println(AnsiStyle.boldCyan("│                                                             │"));
        System.out.println(AnsiStyle.boldCyan("╰─────────────────────────────────────────────────────────────╯"));
        System.out.println();
    }

    public static void printBanner(String title, String subtitle) {
        int width = 50;
        String borderStyle = AnsiStyle.CYAN;
        
        System.out.println(borderStyle + "╭" + "─".repeat(width - 2) + "╮" + AnsiStyle.RESET);
        System.out.println(borderStyle + "│" + AnsiStyle.RESET + padCenter("", width - 2) + borderStyle + "│" + AnsiStyle.RESET);
        System.out.println(borderStyle + "│" + AnsiStyle.RESET + padCenter(AnsiStyle.boldCyan(title), width - 2) + borderStyle + "│" + AnsiStyle.RESET);
        System.out.println(borderStyle + "│" + AnsiStyle.RESET + padCenter("", width - 2) + borderStyle + "│" + AnsiStyle.RESET);
        if (subtitle != null && !subtitle.isEmpty()) {
            System.out.println(borderStyle + "│" + AnsiStyle.RESET + padCenter(AnsiStyle.gray(subtitle), width - 2) + borderStyle + "│" + AnsiStyle.RESET);
            System.out.println(borderStyle + "│" + AnsiStyle.RESET + padCenter("", width - 2) + borderStyle + "│" + AnsiStyle.RESET);
        }
        System.out.println(borderStyle + "╰" + "─".repeat(width - 2) + "╯" + AnsiStyle.RESET);
        System.out.println();
    }

    public static void printSectionHeader(String header) {
        System.out.println();
        System.out.println(AnsiStyle.boldCyan(header.toUpperCase()));
        System.out.println(AnsiStyle.gray("─".repeat(Math.max(30, visibleLength(header) + 4))));
    }

    public static void printSubHeader(String header) {
        System.out.println();
        System.out.println(AnsiStyle.boldYellow("▸ " + header));
    }

    public static void printDivider() {
        System.out.println(AnsiStyle.dim("─".repeat(50)));
    }

    public static void renderBox(String title, List<String> lines, String borderColor) {
        int maxLineLen = title != null ? visibleLength(title) + 4 : 0;
        for (String line : lines) {
            maxLineLen = Math.max(maxLineLen, visibleLength(line));
        }
        int innerWidth = Math.max(maxLineLen + 4, 45);

        String color = borderColor != null ? borderColor : AnsiStyle.CYAN;

        System.out.println(color + "╭" + "─".repeat(innerWidth) + "╮" + AnsiStyle.RESET);
        if (title != null && !title.isEmpty()) {
            System.out.println(color + "│ " + AnsiStyle.RESET + AnsiStyle.bold(title) + " ".repeat(innerWidth - visibleLength(title) - 1) + color + "│" + AnsiStyle.RESET);
            System.out.println(color + "├" + "─".repeat(innerWidth) + "┤" + AnsiStyle.RESET);
        }
        for (String line : lines) {
            int padding = innerWidth - visibleLength(line) - 2;
            padding = Math.max(0, padding);
            System.out.println(color + "│ " + AnsiStyle.RESET + line + " ".repeat(padding) + color + "│" + AnsiStyle.RESET);
        }
        System.out.println(color + "╰" + "─".repeat(innerWidth) + "╯" + AnsiStyle.RESET);
    }

    private static String padCenter(String text, int width) {
        int visLen = visibleLength(text);
        if (visLen >= width) return text;
        int left = (width - visLen) / 2;
        int right = width - visLen - left;
        return " ".repeat(left) + text + " ".repeat(right);
    }
}
