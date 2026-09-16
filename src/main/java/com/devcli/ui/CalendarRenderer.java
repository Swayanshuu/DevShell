package com.devcli.ui;

import com.devcli.model.Commit;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

public class CalendarRenderer {

    public static void renderContributionCalendar(List<Commit> commits) {
        System.out.println("  " + AnsiStyle.boldWhite("CONTRIBUTION CALENDAR"));
        System.out.println("  " + AnsiStyle.dim("───────────────────────────────────────────────────"));
        System.out.println("  " + AnsiStyle.boldCyan("Mon  Tue  Wed  Thu  Fri  Sat  Sun"));
        System.out.println();

        Map<LocalDate, Integer> dailyCounts = new HashMap<>();
        if (commits != null) {
            for (Commit c : commits) {
                if (c.getDate() != null) {
                    LocalDate d = c.getDate().toLocalDate();
                    dailyCounts.put(d, dailyCounts.getOrDefault(d, 0) + 1);
                }
            }
        }

        LocalDate today = LocalDate.now();
        // Start 4 weeks ago on Monday
        LocalDate start = today.minusWeeks(4).with(DayOfWeek.MONDAY);

        LocalDate current = start;
        int weekCount = 0;
        StringBuilder row = new StringBuilder("  ");

        while (!current.isAfter(today) || current.getDayOfWeek() != DayOfWeek.MONDAY) {
            int count = dailyCounts.getOrDefault(current, 0);
            String symbol;

            if (count == 0) {
                symbol = AnsiStyle.dim("░");
            } else if (count <= 2) {
                symbol = AnsiStyle.cyan("▒");
            } else if (count <= 5) {
                symbol = AnsiStyle.boldYellow("▓");
            } else {
                symbol = AnsiStyle.boldGreen("█");
            }

            row.append(" ").append(symbol).append("   ");

            if (current.getDayOfWeek() == DayOfWeek.SUNDAY) {
                System.out.println(row.toString());
                row = new StringBuilder("  ");
                weekCount++;
            }

            current = current.plusDays(1);
            if (weekCount >= 5 && current.getDayOfWeek() == DayOfWeek.MONDAY) break;
        }

        if (row.length() > 2) {
            System.out.println(row.toString());
        }

        System.out.println();
        System.out.println("  " + AnsiStyle.dim("Legend: ") +
                AnsiStyle.dim("░ 0") + "  " +
                AnsiStyle.cyan("▒ 1-2") + "  " +
                AnsiStyle.boldYellow("▓ 3-5") + "  " +
                AnsiStyle.boldGreen("█ 6+"));
        System.out.println();
    }
}
