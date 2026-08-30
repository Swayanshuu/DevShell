package com.devcli.ui;

public class ProgressRenderer {

    public static void printStep(String taskName, boolean completed) {
        String paddedTask = String.format("%-32s", taskName);
        if (completed) {
            System.out.println("  " + paddedTask + " " + AnsiStyle.boldGreen("✓"));
        } else {
            System.out.println("  " + paddedTask + " " + AnsiStyle.yellow("⏳"));
        }
    }

    public static String buildProgressBar(double percentage, int width) {
        int filled = (int) Math.round((percentage / 100.0) * width);
        filled = Math.max(0, Math.min(width, filled));
        int empty = width - filled;
        
        String filledChar = "█";
        String emptyChar = "░";
        
        return AnsiStyle.cyan(filledChar.repeat(filled)) + AnsiStyle.dim(emptyChar.repeat(empty)) + 
               String.format(" %5.1f%%", percentage);
    }
}
