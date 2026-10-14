package pharmacy.util;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public final class InputHelper {

    private InputHelper() { } // utility class — no instances

    public static int readInt(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String text = sc.nextLine().trim();
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException ex) {
                System.out.println("Please enter a whole number.");
            }
        }
    }

    public static double readDouble(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String text = sc.nextLine().trim();
            try {
                double value = Double.parseDouble(text);
                if (Double.isFinite(value)) return value;
            } catch (NumberFormatException ignored) { }
            System.out.println("Please enter a valid number.");
        }
    }

    public static String readLine(Scanner sc, String prompt) {
        System.out.print(prompt);
        return sc.nextLine();
    }

    public static String readNonEmpty(Scanner sc, String prompt) {
        while (true) {
            String value = readLine(sc, prompt).trim();
            if (!value.isEmpty()) return value;
            System.out.println("This field cannot be empty.");
        }
    }

    public static boolean validExpiry(String value) {
        try {
            YearMonth.parse(value);
            return value.matches("\\d{4}-\\d{2}");
        } catch (DateTimeParseException ex) {
            return false;
        }
    }
}