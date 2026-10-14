package pharmacy.service;

import pharmacy.model.Customer;
import pharmacy.model.Drug;

import java.io.*;
import java.util.*;

public final class PersistenceService {
    private static final String DATA_FILE = "pharmacy-data.ser";

    private PersistenceService() { }

    public static void save(List<Drug> inventory,
                            List<Customer> customers,
                            double totalSales,
                            Stack<String> actionLog) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            out.writeObject(inventory);
            out.writeObject(customers);
            out.writeDouble(totalSales);
            out.writeObject(actionLog);
        } catch (IOException ex) {
            System.out.println("Could not save data: " + ex.getMessage());
        }
    }

    /**
     * Loads saved data. Returns an AppState object.
     * On failure, returns null so the caller can seed sample data.
     */
    @SuppressWarnings("unchecked")
    public static AppState load() {
        File file = new File(DATA_FILE);
        if (!file.exists()) return null;

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            List<Drug> inv = (List<Drug>) in.readObject();
            List<Customer> cust = (List<Customer>) in.readObject();
            double sales = in.readDouble();
            Stack<String> log = (Stack<String>) in.readObject();
            System.out.println("Saved data loaded successfully.");
            return new AppState(inv, cust, sales, log);
        } catch (IOException | ClassNotFoundException | ClassCastException ex) {
            System.out.println("Could not load saved data: " + ex.getMessage());
            System.out.println("Starting with sample data. The existing data file was not overwritten automatically.");
            return null;
        }
    }

    /** Simple holder for the loaded state. */
    public static class AppState {
        public final List<Drug> inventory;
        public final List<Customer> customers;
        public final double totalSales;
        public final Stack<String> actionLog;

        public AppState(List<Drug> inventory, List<Customer> customers,
                        double totalSales, Stack<String> actionLog) {
            this.inventory = inventory;
            this.customers = customers;
            this.totalSales = totalSales;
            this.actionLog = actionLog;
        }
    }
}