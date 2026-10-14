package pharmacy.ui;

import pharmacy.model.Customer;
import pharmacy.model.Drug;
import pharmacy.service.InventoryService;
import pharmacy.service.PersistenceService;
import pharmacy.util.InputHelper;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Holds all menu-driven UI logic. State is passed via AppContext
 * so nothing is stored in static fields.
 */
public class PharmacyMenus {

    /** Shared mutable state — one instance lives in main(). */
    public static class AppContext {
        public final List<Drug> inventory = new ArrayList<>();
        public final List<Customer> customers = new ArrayList<>();
        public final Stack<String> actionLog = new Stack<>();
        public double totalSales = 0.0;

        public void save() {
            PersistenceService.save(inventory, customers, totalSales, actionLog);
        }
    }

    private final AppContext ctx;

    public PharmacyMenus(AppContext ctx) {
        this.ctx = ctx;
    }

    // -------------------- DRUG MANAGEMENT --------------------

    public void manageDrugs(Scanner sc) {
        while (true) {
            System.out.println("\n------ DRUG MANAGEMENT ------\n");
            System.out.println("1. List All Drugs");
            System.out.println("2. Add New Drug");
            System.out.println("3. Update Stock");
            System.out.println("4. Edit Drug Details");
            System.out.println("5. Delete Drug");
            System.out.println("6. Back to Main Menu\n");

            int choice = InputHelper.readInt(sc, "Choose an option: ");
            switch (choice) {
                case 1: listDrugs(); ctx.actionLog.push("Admin viewed inventory."); break;
                case 2: addDrug(sc); break;
                case 3: updateStock(sc); break;
                case 4: editDrug(sc); break;
                case 5: deleteDrug(sc); break;
                case 6: return;
                default: System.out.println("Invalid option. Try again.");
            }
        }
    }

    public void listDrugs() {
        System.out.println("\n------ CURRENT INVENTORY ------\n");
        if (ctx.inventory.isEmpty()) {
            System.out.println("No drugs in inventory.");
            return;
        }
        for (Drug drug : ctx.inventory) System.out.println(drug);
    }

    private void addDrug(Scanner sc) {
        System.out.println("\n------ ADD NEW DRUG ------\n");
        String id = InputHelper.readNonEmpty(sc, "Enter Drug ID: ");
        if (InventoryService.findDrugById(ctx.inventory, id) != null) {
            System.out.println("A drug with that ID already exists.");
            return;
        }
        String name = InputHelper.readNonEmpty(sc, "Enter Name: ");
        double price = InputHelper.readDouble(sc, "Enter Price (Rs.): ");
        while (price < 0) price = InputHelper.readDouble(sc, "Price cannot be negative. Enter Price: ");
        int qty = InputHelper.readInt(sc, "Enter Quantity: ");
        while (qty < 0) qty = InputHelper.readInt(sc, "Quantity cannot be negative. Enter Quantity: ");
        String expiry = InputHelper.readNonEmpty(sc, "Enter Expiry Date (YYYY-MM): ");
        if (!InputHelper.validExpiry(expiry)) {
            System.out.println("Invalid expiry date. Use YYYY-MM (example: 2027-05).");
            return;
        }
        String description = InputHelper.readNonEmpty(sc, "Enter Description: ");
        String category = InputHelper.readNonEmpty(sc, "Enter Category: ");

        ctx.inventory.add(new Drug(id, name, price, qty, expiry, description, category));
        ctx.actionLog.push("Added drug: " + name + " (ID " + id + ").");
        ctx.save();
        System.out.println("Drug added successfully.");
    }

    private void updateStock(Scanner sc) {
        String id = InputHelper.readNonEmpty(sc, "Enter Drug ID: ");
        Drug drug = InventoryService.findDrugById(ctx.inventory, id);
        if (drug == null) { System.out.println("Drug not found."); return; }
        System.out.println("Current stock: " + drug.getQuantity());
        int quantity = InputHelper.readInt(sc, "Enter new quantity: ");
        if (quantity < 0) { System.out.println("Quantity cannot be negative."); return; }
        drug.setQuantity(quantity);
        ctx.actionLog.push("Updated stock for " + drug.getName() + " to " + quantity + ".");
        ctx.save();
        System.out.println("Stock updated successfully.");
    }

    private void editDrug(Scanner sc) {
        String id = InputHelper.readNonEmpty(sc, "Enter Drug ID to edit: ");
        Drug drug = InventoryService.findDrugById(ctx.inventory, id);
        if (drug == null) { System.out.println("Drug not found."); return; }

        System.out.println("Press Enter to keep the current value.");
        System.out.print("Name [" + drug.getName() + "]: ");
        String name = sc.nextLine().trim();
        if (!name.isEmpty()) drug.setName(name);

        String priceText = InputHelper.readLine(sc, "Price [" + drug.getPrice() + "] (Enter to keep): ");
        if (!priceText.trim().isEmpty()) {
            try {
                double price = Double.parseDouble(priceText.trim());
                if (price < 0) throw new NumberFormatException();
                drug.setPrice(price);
            } catch (NumberFormatException ex) {
                System.out.println("Invalid price; keeping old price.");
            }
        }

        String expiry = InputHelper.readLine(sc, "Expiry YYYY-MM [" + drug.getExpiryDate() + "] (Enter to keep): ");
        if (!expiry.trim().isEmpty()) {
            if (InputHelper.validExpiry(expiry.trim())) drug.setExpiryDate(expiry.trim());
            else System.out.println("Invalid expiry; keeping old expiry.");
        }

        String description = InputHelper.readLine(sc, "Description [" + drug.getDescription() + "] (Enter to keep): ");
        if (!description.trim().isEmpty()) drug.setDescription(description.trim());

        String category = InputHelper.readLine(sc, "Category [" + drug.getCategory() + "] (Enter to keep): ");
        if (!category.trim().isEmpty()) drug.setCategory(category.trim());

        ctx.actionLog.push("Edited drug: " + drug.getName() + " (ID " + id + ").");
        ctx.save();
        System.out.println("Drug details updated.");
    }

    private void deleteDrug(Scanner sc) {
        String id = InputHelper.readNonEmpty(sc, "Enter Drug ID to delete: ");
        Drug drug = InventoryService.findDrugById(ctx.inventory, id);
        if (drug == null) { System.out.println("Drug not found."); return; }
        String confirm = InputHelper.readLine(sc, "Delete " + drug.getName() + "? Type YES to confirm: ");
        if ("YES".equalsIgnoreCase(confirm.trim())) {
            ctx.inventory.remove(drug);
            ctx.actionLog.push("Deleted drug: " + drug.getName() + " (ID " + id + ").");
            ctx.save();
            System.out.println("Drug deleted.");
        } else {
            System.out.println("Delete cancelled.");
        }
    }

    // -------------------- CUSTOMER MANAGEMENT --------------------

    public void manageCustomers(Scanner sc) {
        while (true) {
            System.out.println("\n------ CUSTOMER MANAGEMENT ------\n");
            System.out.println("1. List Customers");
            System.out.println("2. Register Customer");
            System.out.println("3. Edit Customer");
            System.out.println("4. Delete Customer");
            System.out.println("5. Back to Main Menu\n");

            int choice = InputHelper.readInt(sc, "Choose an option: ");
            switch (choice) {
                case 1: listCustomers(); ctx.actionLog.push("Viewed customer list."); break;
                case 2: addCustomer(sc); break;
                case 3: editCustomer(sc); break;
                case 4: deleteCustomer(sc); break;
                case 5: return;
                default: System.out.println("Invalid option. Try again.");
            }
        }
    }

    private void listCustomers() {
        System.out.println("\n------ CUSTOMERS ------\n");
        if (ctx.customers.isEmpty()) { System.out.println("No customers registered."); return; }
        for (Customer c : ctx.customers) System.out.println(c);
    }

    private void addCustomer(Scanner sc) {
        String id = InputHelper.readNonEmpty(sc, "Enter Customer ID: ");
        if (InventoryService.findCustomerById(ctx.customers, id) != null) {
            System.out.println("A customer with that ID already exists.");
            return;
        }
        String name = InputHelper.readNonEmpty(sc, "Enter Name: ");
        String phone = InputHelper.readNonEmpty(sc, "Enter Phone: ");
        ctx.customers.add(new Customer(id, name, phone));
        ctx.actionLog.push("Registered customer: " + name + " (ID " + id + ").");
        ctx.save();
        System.out.println("Customer registered successfully.");
    }

    private void editCustomer(Scanner sc) {
        String id = InputHelper.readNonEmpty(sc, "Enter Customer ID to edit: ");
        Customer customer = InventoryService.findCustomerById(ctx.customers, id);
        if (customer == null) { System.out.println("Customer not found."); return; }
        String name = InputHelper.readLine(sc, "Name [" + customer.getName() + "] (Enter to keep): ");
        if (!name.trim().isEmpty()) customer.setName(name.trim());
        String phone = InputHelper.readLine(sc, "Phone [" + customer.getPhone() + "] (Enter to keep): ");
        if (!phone.trim().isEmpty()) customer.setPhone(phone.trim());
        ctx.actionLog.push("Edited customer: " + customer.getName() + " (ID " + id + ").");
        ctx.save();
        System.out.println("Customer updated.");
    }

    private void deleteCustomer(Scanner sc) {
        String id = InputHelper.readNonEmpty(sc, "Enter Customer ID to delete: ");
        Customer customer = InventoryService.findCustomerById(ctx.customers, id);
        if (customer == null) { System.out.println("Customer not found."); return; }
        String confirm = InputHelper.readLine(sc, "Delete " + customer.getName() + "? Type YES to confirm: ");
        if ("YES".equalsIgnoreCase(confirm.trim())) {
            ctx.customers.remove(customer);
            ctx.actionLog.push("Deleted customer: " + customer.getName() + " (ID " + id + ").");
            ctx.save();
            System.out.println("Customer deleted.");
        } else {
            System.out.println("Delete cancelled.");
        }
    }

    // -------------------- CART / POS --------------------

    public void manageCart(Scanner sc) {
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

            int choice = InputHelper.readInt(sc, "Choose an option: ");
            switch (choice) {
                case 1: listDrugs(); break;
                case 2: addToCart(sc, cart); break;
                case 3: displayCart(cart); break;
                case 4: removeFromCart(sc, cart); break;
                case 5: if (checkout(sc, cart)) return; break;
                case 6: cart.clear(); System.out.println("Cart cleared."); break;
                case 7: return;
                default: System.out.println("Invalid option. Try again.");
            }
        }
    }

    private void addToCart(Scanner sc, Map<String, Integer> cart) {
        String id = InputHelper.readNonEmpty(sc, "Enter Drug ID: ");
        Drug drug = InventoryService.findDrugById(ctx.inventory, id);
        if (drug == null) { System.out.println("Drug not found."); return; }
        int quantity = InputHelper.readInt(sc, "Enter quantity: ");
        if (quantity <= 0) { System.out.println("Quantity must be greater than zero."); return; }
        int alreadyInCart = cart.getOrDefault(id, 0);
        if (alreadyInCart + quantity > drug.getQuantity()) {
            System.out.println("Insufficient stock. Available: " + drug.getQuantity()
                    + ", already in cart: " + alreadyInCart + ".");
            return;
        }
        cart.put(id, alreadyInCart + quantity);
        System.out.println("Added to cart.");
    }

    private void displayCart(Map<String, Integer> cart) {
        System.out.println("\n------ CURRENT CART ------\n");
        if (cart.isEmpty()) { System.out.println("Cart is empty."); return; }
        double total = 0;
        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            Drug drug = InventoryService.findDrugById(ctx.inventory, entry.getKey());
            if (drug != null) {
                double subtotal = drug.getPrice() * entry.getValue();
                total += subtotal;
                System.out.printf(Locale.US, "%s x %d @ Rs. %.2f = Rs. %.2f%n",
                        drug.getName(), entry.getValue(), drug.getPrice(), subtotal);
            }
        }
        System.out.printf(Locale.US, "Cart total: Rs. %.2f%n", total);
    }

    private void removeFromCart(Scanner sc, Map<String, Integer> cart) {
        String id = InputHelper.readNonEmpty(sc, "Enter Drug ID to remove from cart: ");
        if (cart.remove(id) != null) System.out.println("Removed from cart.");
        else System.out.println("That drug is not in the cart.");
    }

    private boolean checkout(Scanner sc, Map<String, Integer> cart) {
        if (cart.isEmpty()) { System.out.println("Cart is empty."); return false; }
        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            Drug drug = InventoryService.findDrugById(ctx.inventory, entry.getKey());
            if (drug == null || entry.getValue() > drug.getQuantity()) {
                System.out.println("Stock changed or drug unavailable. Please review the cart.");
                return false;
            }
        }

        displayCart(cart);
        String confirm = InputHelper.readLine(sc, "Confirm checkout? (Y/N): ");
        if (!"Y".equalsIgnoreCase(confirm.trim())) {
            System.out.println("Checkout cancelled. Cart retained.");
            return false;
        }

        double total = 0;
        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            Drug drug = InventoryService.findDrugById(ctx.inventory, entry.getKey());
            int quantity = entry.getValue();
            drug.setQuantity(drug.getQuantity() - quantity);
            total += drug.getPrice() * quantity;
        }
        ctx.totalSales += total;
        ctx.actionLog.push(String.format(Locale.US, "Sale completed: Rs. %.2f", total));
        cart.clear();
        ctx.save();
        System.out.printf(Locale.US, "Checkout successful. Total: Rs. %.2f%n", total);
        return true;
    }

    // -------------------- SEARCH / ALERTS / REPORTS --------------------

    public void searchDrugs(Scanner sc) {
        while (true) {
            System.out.println("\n------ SEARCH DRUGS ------\n");
            System.out.println("1. Search by ID");
            System.out.println("2. Search by Name");
            System.out.println("3. Search by Category");
            System.out.println("4. Back to Main Menu\n");
            int choice = InputHelper.readInt(sc, "Choose an option: ");

            if (choice == 4) return;
            if (choice < 1 || choice > 3) { System.out.println("Invalid option. Try again."); continue; }

            String term = InputHelper.readNonEmpty(sc, "Enter search text: ").toLowerCase(Locale.ROOT);
            boolean found = false;
            for (Drug drug : ctx.inventory) {
                boolean match = choice == 1
                        ? drug.getId().equalsIgnoreCase(term)
                        : choice == 2
                            ? drug.getName().toLowerCase(Locale.ROOT).contains(term)
                            : drug.getCategory().toLowerCase(Locale.ROOT).contains(term);
                if (match) { System.out.println(drug); found = true; }
            }
            if (!found) System.out.println("No matching drugs found.");
            ctx.actionLog.push("Searched drugs using: " + term + ".");
        }
    }

    public void showAlerts() {
        System.out.println("\n------ EXPIRY AND LOW-STOCK ALERTS ------\n");
        boolean any = false;
        YearMonth currentMonth = YearMonth.now();
        for (Drug drug : ctx.inventory) {
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
        ctx.actionLog.push("Viewed expiry and stock alerts.");
    }

    public void showActionLog() {
        System.out.println("\n------ ACTION LOG (newest first) ------\n");
        if (ctx.actionLog.isEmpty()) { System.out.println("No actions recorded."); return; }
        for (int i = ctx.actionLog.size() - 1; i >= 0; i--) {
            System.out.println((ctx.actionLog.size() - i) + ". " + ctx.actionLog.get(i));
        }
    }

    public void showSalesReport() {
        System.out.println("\n------ SALES REPORT ------\n");
        System.out.printf(Locale.US, "Total sales recorded: Rs. %.2f%n", ctx.totalSales);
        System.out.println("Current number of drug types: " + ctx.inventory.size());
        int units = 0;
        for (Drug drug : ctx.inventory) units += drug.getQuantity();
        System.out.println("Total units currently in stock: " + units);
        System.out.println("Registered customers: " + ctx.customers.size());
    }
}