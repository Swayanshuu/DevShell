package com.devcli.ui;

import java.util.ArrayList;
import java.util.List;

public class TableRenderer {

    public static void printTable(List<String> headers, List<List<String>> rows) {
        if (headers == null || headers.isEmpty()) return;

        int numCols = headers.size();
        int[] colWidths = new int[numCols];

        for (int i = 0; i < numCols; i++) {
            colWidths[i] = BoxRenderer.visibleLength(headers.get(i));
        }

        for (List<String> row : rows) {
            for (int i = 0; i < Math.min(numCols, row.size()); i++) {
                colWidths[i] = Math.max(colWidths[i], BoxRenderer.visibleLength(row.get(i)));
            }
        }

        // Top Border
        StringBuilder topBorder = new StringBuilder("┌");
        for (int i = 0; i < numCols; i++) {
            topBorder.append("─".repeat(colWidths[i] + 2));
            if (i < numCols - 1) topBorder.append("┬");
        }
        topBorder.append("┐");
        System.out.println(AnsiStyle.dim(topBorder.toString()));

        // Header Row
        StringBuilder headerLine = new StringBuilder("│");
        for (int i = 0; i < numCols; i++) {
            String h = headers.get(i);
            int padding = colWidths[i] - BoxRenderer.visibleLength(h);
            headerLine.append(" ").append(AnsiStyle.boldCyan(h)).append(" ".repeat(padding)).append(" │");
        }
        System.out.println(headerLine.toString());

        // Header Divider
        StringBuilder midBorder = new StringBuilder("├");
        for (int i = 0; i < numCols; i++) {
            midBorder.append("─".repeat(colWidths[i] + 2));
            if (i < numCols - 1) midBorder.append("┼");
        }
        midBorder.append("┤");
        System.out.println(AnsiStyle.dim(midBorder.toString()));

        // Data Rows
        for (List<String> row : rows) {
            StringBuilder rowLine = new StringBuilder("│");
            for (int i = 0; i < numCols; i++) {
                String val = i < row.size() ? row.get(i) : "";
                int padding = colWidths[i] - BoxRenderer.visibleLength(val);
                rowLine.append(" ").append(val).append(" ".repeat(padding)).append(" │");
            }
            System.out.println(rowLine.toString());
        }

        // Bottom Border
        StringBuilder botBorder = new StringBuilder("└");
        for (int i = 0; i < numCols; i++) {
            botBorder.append("─".repeat(colWidths[i] + 2));
            if (i < numCols - 1) botBorder.append("┴");
        }
        botBorder.append("┘");
        System.out.println(AnsiStyle.dim(botBorder.toString()));
    }
}
