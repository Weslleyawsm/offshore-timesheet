package com.leley.timesheet.cli;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Scanner;

public final class ConsoleIO {
    private final Scanner sc = new Scanner(System.in);

    public void println(String s) {
        System.out.println(s);
    }

    public String readLine(String label) {
        System.out.print(label + ": ");
        return sc.nextLine();
    }

    public long readLong(String label) {
        while (true) {
            String s = readLine(label).trim();
            try {
                return Long.parseLong(s);
            } catch (Exception e) {
                println("Valor inválido.");
            }
        }
    }

    public BigDecimal readBigDecimal(String label) {
        while (true) {
            String s = readLine(label).trim().replace(",", ".");
            try {
                return new BigDecimal(s);
            } catch (Exception e) {
                println("Valor inválido.");
            }
        }
    }

    public LocalDate readDate(String label) {
        while (true) {
            String s = readLine(label).trim();
            try {
                return LocalDate.parse(s);
            } catch (Exception e) {
                println("Data inválida. Use YYYY-MM-DD.");
            }
        }
    }

    public boolean confirm(String label) {
        String s = readLine(label + " (s/n)").trim().toLowerCase();
        return s.equals("s") || s.equals("sim");
    }
}
