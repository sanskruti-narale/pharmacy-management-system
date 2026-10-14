import java.io.*;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Pharmacy Management System
 * Java-only console application with repeated submenus and file persistence.
 *
 * Compile: javac PharmacySystem.java
 * Run:     java PharmacySystem
 *
 * Data is saved to pharmacy-data.ser in the program's working directory.
 */
public class PharmacySystem {
    private static final String DATA_FILE = "pharmacy-data.ser";

    private static List<Drug> inventory = new ArrayList<>();
    private static List<Customer> customers = new ArrayList<>();
    private static Stack<String> actionLog = new Stack<>();
    private static double totalSales = 0.0;

    public static void main(String[] args) {
        loadData();
        actionLog.push("System started.");
        Scanner sc = new Scanner(System.in);

        boolean running = true;
        while (running) {
            System.out.println("\n========== PHARMACY MANAGEMENT SYSTEM ==========\n");
            System.out.println("1. Drug Management");
            System.out.println("2. Customer Management");
            System.out.println("3. Cart / POS Checkout");
            System.out.println("4. Search Drugs");
            System.out.println("5. Expiry and Low-Stock Alerts");
            System.out.println("6. View Action Log");
            System.out.println("7. Sales Report");
            System.out.println("8. Save and Exit\n");

            int choice = readInt(sc, "Choose an option: ");
            switch (choice) {
                case 1: manageDrugs(sc); break;
                case 2: manageCustomers(sc); break;
                case 3: manageCart(sc); break;
                case 4: searchDrugs(sc); break;
                case 5: showAlerts(); break;
                case 6: showActionLog(); break;
                case 7: showSalesReport(); break;
                case 8:
                    saveData();
                    actionLog.push("System closed.");
                    saveData();
                    running = false;
                    System.out.println("Data saved. Goodbye!");
                    break;
                default: System.out.println("Invalid option. Please choose 1-8.");
            }
        }
        sc.close();
    }

    // -------------------- DRUG MANAGEMENT --------------------

    private static void manageDrugs(Scanner sc) {
        while (true) {
            System.out.println("\n------ DRUG MANAGEMENT ------\n");
            System.out.println("1. List All Drugs");
            System.out.println("2. Add New Drug");
            System.out.println("3. Update Stock");
            System.out.println("4. Edit Drug Details");
            System.out.println("5. Delete Drug");
            System.out.println("6. Back to Main Menu\n");

            int choice = readInt(sc, "Choose an option: ");
            switch (choice) {
                case 1:
                    listDrugs();
                    actionLog.push("Admin viewed inventory.");
                    break;
                case 2:
                    addDrug(sc);
                    break;
                case 3:
                    updateStock(sc);
                    break;
                case 4:
                    editDrug(sc);
                    break;
                case 5:
                    deleteDrug(sc);
                    break;
                case 6:
                    return;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }

    private static void listDrugs() {
        System.out.println("\n------ CURRENT INVENTORY ------\n");
        if (inventory.isEmpty()) {
            System.out.println("No drugs in inventory.");
            return;
        }
        for (Drug drug : inventory) System.out.println(drug);
    }

    private static void addDrug(Scanner sc) {
        System.out.println("\n------ ADD NEW DRUG ------\n");
        String id = readNonEmpty(sc, "Enter Drug ID: ");
        if (findDrugById(id) != null) {
            System.out.println("A drug with that ID already exists.");
            return;
        }
        String name = readNonEmpty(sc, "Enter Name: ");
        double price = readDouble(sc, "Enter Price (Rs.): ");
        while (price < 0) price = readDouble(sc, "Price cannot be negative. Enter Price: ");
        int qty = readInt(sc, "Enter Quantity: ");
        while (qty < 0) qty = readInt(sc, "Quantity cannot be negative. Enter Quantity: ");
        String expiry = readNonEmpty(sc, "Enter Expiry Date (YYYY-MM): ");
        if (!validExpiry(expiry)) {
            System.out.println("Invalid expiry date. Use YYYY-MM (example: 2027-05).");
            return;
        }
        String description = readNonEmpty(sc, "Enter Description: ");
        String category = readNonEmpty(sc, "Enter Category: ");

        inventory.add(new Drug(id, name, price, qty, expiry, description, category));
        actionLog.push("Added drug: " + name + " (ID " + id + ").");
        saveData();
        System.out.println("Drug added successfully.");
    }

    private static void updateStock(Scanner sc) {
        String id = readNonEmpty(sc, "Enter Drug ID: ");
        Drug drug = findDrugById(id);
        if (drug == null) {
            System.out.println("Drug not found.");
            return;
        }
        System.out.println("Current stock: " + drug.getQuantity());
        int quantity = readInt(sc, "Enter new quantity: ");
        if (quantity < 0) {
            System.out.println("Quantity cannot be negative.");
            return;
        }
        drug.setQuantity(quantity);
        actionLog.push("Updated stock for " + drug.getName() + " to " + quantity + ".");
        saveData();
        System.out.println("Stock updated successfully.");
    }

    private static void editDrug(Scanner sc) {
        String id = readNonEmpty(sc, "Enter Drug ID to edit: ");
        Drug drug = findDrugById(id);
        if (drug == null) {
            System.out.println("Drug not found.");
            return;
        }
        System.out.println("Press Enter to keep the current value.");
        System.out.print("Name [" + drug.getName() + "]: ");
        String name = sc.nextLine().trim();
        if (!name.isEmpty()) drug.setName(name);

        String priceText = readLine(sc, "Price [" + drug.getPrice() + "] (Enter to keep): ");
        if (!priceText.trim().isEmpty()) {
            try {
                double price = Double.parseDouble(priceText.trim());
                if (price < 0) throw new NumberFormatException();
                drug.setPrice(price);
            } catch (NumberFormatException ex) {
                System.out.println("Invalid price; keeping old price.");
            }
        }

        String expiry = readLine(sc, "Expiry YYYY-MM [" + drug.getExpiryDate() + "] (Enter to keep): ");
        if (!expiry.trim().isEmpty()) {
            if (validExpiry(expiry.trim())) drug.setExpiryDate(expiry.trim());
            else System.out.println("Invalid expiry; keeping old expiry.");
        }

        String description = readLine(sc, "Description [" + drug.getDescription() + "] (Enter to keep): ");
        if (!description.trim().isEmpty()) drug.setDescription(description.trim());
        String category = readLine(sc, "Category [" + drug.getCategory() + "] (Enter to keep): ");
        if (!category.trim().isEmpty()) drug.setCategory(category.trim());

        actionLog.push("Edited drug: " + drug.getName() + " (ID " + id + ").");
        saveData();
        System.out.println("Drug details updated.");
    }

    private static void deleteDrug(Scanner sc) {
        String id = readNonEmpty(sc, "Enter Drug ID to delete: ");
        Drug drug = findDrugById(id);
        if (drug == null) {
            System.out.println("Drug not found.");
            return;
        }
        String confirm = readLine(sc, "Delete " + drug.getName() + "? Type YES to confirm: ");
        if ("YES".equalsIgnoreCase(confirm.trim())) {
            inventory.remove(drug);
            actionLog.push("Deleted drug: " + drug.getName() + " (ID " + id + ").");
            saveData();
            System.out.println("Drug deleted.");
        } else {
            System.out.println("Delete cancelled.");
        }
    }

    // -------------------- CUSTOMER MANAGEMENT --------------------

    private static void manageCustomers(Scanner sc) {
        while (true) {
            System.out.println("\n------ CUSTOMER MANAGEMENT ------\n");
            System.out.println("1. List Customers");
            System.out.println("2. Register Customer");
            System.out.println("3. Edit Customer");
            System.out.println("4. Delete Customer");
            System.out.println("5. Back to Main Menu\n");

            int choice = readInt(sc, "Choose an option: ");
            switch (choice) {
                case 1: listCustomers(); actionLog.push("Viewed customer list."); break;
                case 2: addCustomer(sc); break;
                case 3: editCustomer(sc); break;
                case 4: deleteCustomer(sc); break;
                case 5: return;
                default: System.out.println("Invalid option. Try again.");
            }
        }
    }

    private static void listCustomers() {
        System.out.println("\n------ CUSTOMERS ------\n");
        if (customers.isEmpty()) {
            System.out.println("No customers registered.");
            return;
        }
        for (Customer customer : customers) System.out.println(customer);
    }

    private static void addCustomer(Scanner sc) {
        String id = readNonEmpty(sc, "Enter Customer ID: ");
        if (findCustomerById(id) != null) {
            System.out.println("A customer with that ID already exists.");
            return;
        }
        String name = readNonEmpty(sc, "Enter Name: ");
        String phone = readNonEmpty(sc, "Enter Phone: ");
        customers.add(new Customer(id, name, phone));
        actionLog.push("Registered customer: " + name + " (ID " + id + ").");
        saveData();
        System.out.println("Customer registered successfully.");
    }

    private static void editCustomer(Scanner sc) {
        String id = readNonEmpty(sc, "Enter Customer ID to edit: ");
        Customer customer = findCustomerById(id);
        if (customer == null) {
            System.out.println("Customer not found.");
            return;
        }
        String name = readLine(sc, "Name [" + customer.getName() + "] (Enter to keep): ");
        if (!name.trim().isEmpty()) customer.setName(name.trim());
        String phone = readLine(sc, "Phone [" + customer.getPhone() + "] (Enter to keep): ");
        if (!phone.trim().isEmpty()) customer.setPhone(phone.trim());
        actionLog.push("Edited customer: " + customer.getName() + " (ID " + id + ").");
        saveData();
        System.out.println("Customer updated.");
    }

    private static void deleteCustomer(Scanner sc) {
        String id = readNonEmpty(sc, "Enter Customer ID to delete: ");
        Customer customer = findCustomerById(id);
        if (customer == null) {
            System.out.println("Customer not found.");
            return;
        }
        String confirm = readLine(sc, "Delete " + customer.getName() + "? Type YES to confirm: ");
        if ("YES".equalsIgnoreCase(confirm.trim())) {
            customers.remove(customer);
            actionLog.push("Deleted customer: " + customer.getName() + " (ID " + id + ").");
            saveData();
            System.out.println("Customer deleted.");
        } else {
            System.out.println("Delete cancelled.");
        }
    }

    // -------------------- CART / POINT OF SALE --------------------

    private static void manageCart(Scanner sc) {
        Map<String, Integer> cart = new LinkedHashMap<>();
        while (true) {
            System.out.println("\n------ CART / POS CHECKOUT ------\n");
            System.out.println("1. List Drugs");
            System.out.println("2. Add Drug to Cart");
            System.out.println("3. View Cart");
            System.out.println("4. Remove Drug from Cart");
            System.out.println("5. Checkout");
            System.out.println("6. Clear Cart");
            System.out.println("7. Back to Main Menu\n");

            int choice = readInt(sc, "Choose an option: ");
            switch (choice) {
                case 1: listDrugs(); break;
                case 2: addToCart(sc, cart); break;
                case 3: displayCart(cart); break;
                case 4: removeFromCart(sc, cart); break;
                case 5:
                    if (checkout(sc, cart)) return;
                    break;
                case 6: cart.clear(); System.out.println("Cart cleared."); break;
                case 7: return;
                default: System.out.println("Invalid option. Try again.");
            }
        }
    }

    private static void addToCart(Scanner sc, Map<String, Integer> cart) {
        String id = readNonEmpty(sc, "Enter Drug ID: ");
        Drug drug = findDrugById(id);
        if (drug == null) {
            System.out.println("Drug not found.");
            return;
        }
        int quantity = readInt(sc, "Enter quantity: ");
        if (quantity <= 0) {
            System.out.println("Quantity must be greater than zero.");
            return;
        }
        int alreadyInCart = cart.containsKey(id) ? cart.get(id) : 0;
        if (alreadyInCart + quantity > drug.getQuantity()) {
            System.out.println("Insufficient stock. Available: " + drug.getQuantity()
                    + ", already in cart: " + alreadyInCart + ".");
            return;
        }
        cart.put(id, alreadyInCart + quantity);
        System.out.println("Added to cart.");
    }

    private static void displayCart(Map<String, Integer> cart) {
        System.out.println("\n------ CURRENT CART ------\n");
        if (cart.isEmpty()) {
            System.out.println("Cart is empty.");
            return;
        }
        double total = 0;
        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            Drug drug = findDrugById(entry.getKey());
            if (drug != null) {
                double subtotal = drug.getPrice() * entry.getValue();
                total += subtotal;
                System.out.printf(Locale.US, "%s x %d @ Rs. %.2f = Rs. %.2f%n",
                        drug.getName(), entry.getValue(), drug.getPrice(), subtotal);
            }
        }
        System.out.printf(Locale.US, "Cart total: Rs. %.2f%n", total);
    }

    private static void removeFromCart(Scanner sc, Map<String, Integer> cart) {
        String id = readNonEmpty(sc, "Enter Drug ID to remove from cart: ");
        if (cart.remove(id) != null) System.out.println("Removed from cart.");
        else System.out.println("That drug is not in the cart.");
    }

    private static boolean checkout(Scanner sc, Map<String, Integer> cart) {
        if (cart.isEmpty()) {
            System.out.println("Cart is empty.");
            return false;
        }
        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            Drug drug = findDrugById(entry.getKey());
            if (drug == null || entry.getValue() > drug.getQuantity()) {
                System.out.println("Stock changed or drug unavailable. Please review the cart.");
                return false;
            }
        }

        displayCart(cart);
        String confirm = readLine(sc, "Confirm checkout? (Y/N): ");
        if (!"Y".equalsIgnoreCase(confirm.trim())) {
            System.out.println("Checkout cancelled. Cart retained.");
            return false;
        }

        double total = 0;
        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            Drug drug = findDrugById(entry.getKey());
            int quantity = entry.getValue();
            drug.setQuantity(drug.getQuantity() - quantity);
            total += drug.getPrice() * quantity;
        }
        totalSales += total;
        actionLog.push(String.format(Locale.US, "Sale completed: Rs. %.2f", total));
        cart.clear();
        saveData();
        System.out.printf(Locale.US, "Checkout successful. Total: Rs. %.2f%n", total);
        return true;
    }

    // -------------------- SEARCH / ALERTS / REPORTS --------------------

    private static void searchDrugs(Scanner sc) {
        while (true) {
            System.out.println("\n------ SEARCH DRUGS ------\n");
            System.out.println("1. Search by ID");
            System.out.println("2. Search by Name");
            System.out.println("3. Search by Category");
            System.out.println("4. Back to Main Menu\n");
            int choice = readInt(sc, "Choose an option: ");

            if (choice == 4) return;
            if (choice < 1 || choice > 3) {
                System.out.println("Invalid option. Try again.");
                continue;
            }
            String term = readNonEmpty(sc, "Enter search text: ").toLowerCase(Locale.ROOT);
            boolean found = false;
            for (Drug drug : inventory) {
                boolean match = choice == 1
                        ? drug.getId().equalsIgnoreCase(term)
                        : choice == 2
                            ? drug.getName().toLowerCase(Locale.ROOT).contains(term)
                            : drug.getCategory().toLowerCase(Locale.ROOT).contains(term);
                if (match) {
                    System.out.println(drug);
                    found = true;
                }
            }
            if (!found) System.out.println("No matching drugs found.");
            actionLog.push("Searched drugs using: " + term + ".");
        }
    }

    private static void showAlerts() {
        System.out.println("\n------ EXPIRY AND LOW-STOCK ALERTS ------\n");
        boolean any = false;
        YearMonth currentMonth = YearMonth.now();
        for (Drug drug : inventory) {
            if (drug.getQuantity() <= 10) {
                System.out.println("LOW STOCK: " + drug.getName() + " (quantity: " + drug.getQuantity() + ")");
                any = true;
            }
            try {
                YearMonth expiry = YearMonth.parse(drug.getExpiryDate());
                if (!expiry.isAfter(currentMonth)) {
                    System.out.println("EXPIRED/EXPIRING: " + drug.getName() + " (expiry: " + drug.getExpiryDate() + ")");
                    any = true;
                } else if (!expiry.isAfter(currentMonth.plusMonths(3))) {
                    System.out.println("EXPIRING SOON: " + drug.getName() + " (expiry: " + drug.getExpiryDate() + ")");
                    any = true;
                }
            } catch (DateTimeParseException ignored) {
                System.out.println("INVALID EXPIRY DATE: " + drug.getName() + " (expiry: " + drug.getExpiryDate() + ")");
                any = true;
            }
        }
        if (!any) System.out.println("No current alerts.");
        actionLog.push("Viewed expiry and stock alerts.");
    }

    private static void showActionLog() {
        System.out.println("\n------ ACTION LOG (newest first) ------\n");
        if (actionLog.isEmpty()) {
            System.out.println("No actions recorded.");
            return;
        }
        for (int i = actionLog.size() - 1; i >= 0; i--) {
            System.out.println((actionLog.size() - i) + ". " + actionLog.get(i));
        }
    }

    private static void showSalesReport() {
        System.out.println("\n------ SALES REPORT ------\n");
        System.out.printf(Locale.US, "Total sales recorded: Rs. %.2f%n", totalSales);
        System.out.println("Current number of drug types: " + inventory.size());
        int units = 0;
        for (Drug drug : inventory) units += drug.getQuantity();
        System.out.println("Total units currently in stock: " + units);
        System.out.println("Registered customers: " + customers.size());
    }

    // -------------------- LOOKUPS / INPUT HELPERS --------------------

    private static Drug findDrugById(String id) {
        for (Drug drug : inventory) {
            if (drug.getId().equalsIgnoreCase(id.trim())) return drug;
        }
        return null;
    }

    private static Customer findCustomerById(String id) {
        for (Customer customer : customers) {
            if (customer.getId().equalsIgnoreCase(id.trim())) return customer;
        }
        return null;
    }

    private static int readInt(Scanner sc, String prompt) {
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

    private static double readDouble(Scanner sc, String prompt) {
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

    private static String readLine(Scanner sc, String prompt) {
        System.out.print(prompt);
        return sc.nextLine();
    }

    private static String readNonEmpty(Scanner sc, String prompt) {
        while (true) {
            String value = readLine(sc, prompt).trim();
            if (!value.isEmpty()) return value;
            System.out.println("This field cannot be empty.");
        }
    }

    private static boolean validExpiry(String value) {
        try {
            YearMonth.parse(value);
            return value.matches("\\d{4}-\\d{2}");
        } catch (DateTimeParseException ex) {
            return false;
        }
    }

    // -------------------- PERSISTENCE --------------------

    private static void saveData() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            out.writeObject(inventory);
            out.writeObject(customers);
            out.writeDouble(totalSales);
            out.writeObject(actionLog);
        } catch (IOException ex) {
            System.out.println("Could not save data: " + ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static void loadData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            seedData();
            saveData();
            System.out.println("First run: sample data loaded and saved.");
            return;
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            inventory = (List<Drug>) in.readObject();
            customers = (List<Customer>) in.readObject();
            totalSales = in.readDouble();
            actionLog = (Stack<String>) in.readObject();
            System.out.println("Saved data loaded successfully.");
        } catch (IOException | ClassNotFoundException | ClassCastException ex) {
            System.out.println("Could not load saved data: " + ex.getMessage());
            System.out.println("Starting with sample data. The existing data file was not overwritten automatically.");
            inventory = new ArrayList<>();
            customers = new ArrayList<>();
            actionLog = new Stack<>();
            totalSales = 0.0;
            seedData();
        }
    }

    private static void seedData() {
        inventory.add(new Drug("D101", "Paracetamol", 20.00, 15, "2027-05",
                "Pain and fever relief", "Pain Relief"));
        inventory.add(new Drug("D102", "Amoxicillin", 85.50, 40, "2027-02",
                "Antibiotic", "Antibiotics"));
        inventory.add(new Drug("D103", "Cetirizine", 12.00, 8, "2026-12",
                "Allergy relief", "Allergy"));
        customers.add(new Customer("C101", "Walk-in Customer", "0000000000"));
        actionLog.push("Sample data initialized.");
    }

    // -------------------- DATA CLASSES --------------------

    private static class Drug implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String id;
        private String name;
        private double price;
        private int quantity;
        private String expiryDate;
        private String description;
        private String category;

        Drug(String id, String name, double price, int quantity, String expiryDate,
             String description, String category) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.quantity = quantity;
            this.expiryDate = expiryDate;
            this.description = description;
            this.category = category;
        }

        String getId() { return id; }
        String getName() { return name; }
        double getPrice() { return price; }
        int getQuantity() { return quantity; }
        String getExpiryDate() { return expiryDate; }
        String getDescription() { return description; }
        String getCategory() { return category; }

        void setName(String name) { this.name = name; }
        void setPrice(double price) { this.price = price; }
        void setQuantity(int quantity) { this.quantity = quantity; }
        void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
        void setDescription(String description) { this.description = description; }
        void setCategory(String category) { this.category = category; }

        @Override
        public String toString() {
            return String.format(Locale.US,
                    "ID: %s | Name: %s | Price: Rs. %.2f | Stock: %d | Expiry: %s | Category: %s | Description: %s",
                    id, name, price, quantity, expiryDate, category, description);
        }
    }

    private static class Customer implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String id;
        private String name;
        private String phone;

        Customer(String id, String name, String phone) {
            this.id = id;
            this.name = name;
            this.phone = phone;
        }

        String getId() { return id; }
        String getName() { return name; }
        String getPhone() { return phone; }

        void setName(String name) { this.name = name; }
        void setPhone(String phone) { this.phone = phone; }

        @Override
        public String toString() {
            return "ID: " + id + " | Name: " + name + " | Phone: " + phone;
        }
    }
}
