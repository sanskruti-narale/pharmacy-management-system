import pharmacy.model.Drug;
import pharmacy.service.InventoryService;
import pharmacy.service.PersistenceService;
import pharmacy.ui.PharmacyMenus;
import pharmacy.util.InputHelper;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        PharmacyMenus.AppContext ctx = new PharmacyMenus.AppContext();

        // ---- Load or seed ----
        PersistenceService.AppState loaded = PersistenceService.load();
        if (loaded != null) {
            ctx.inventory.addAll(loaded.inventory);
            ctx.customers.addAll(loaded.customers);
            ctx.totalSales = loaded.totalSales;
            ctx.actionLog.addAll(loaded.actionLog);
        } else {
            InventoryService.seedData(ctx.inventory, ctx.customers, ctx.actionLog);
            ctx.save();
            System.out.println("First run: sample data loaded and saved.");
        }

        ctx.actionLog.push("System started.");
        PharmacyMenus menus = new PharmacyMenus(ctx);
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

            int choice = InputHelper.readInt(sc, "Choose an option: ");
            switch (choice) {
                case 1: menus.manageDrugs(sc); break;
                case 2: menus.manageCustomers(sc); break;
                case 3: menus.manageCart(sc); break;
                case 4: menus.searchDrugs(sc); break;
                case 5: menus.showAlerts(); break;
                case 6: menus.showActionLog(); break;
                case 7: menus.showSalesReport(); break;
                case 8:
                    ctx.actionLog.push("System closed.");
                    ctx.save();
                    running = false;
                    System.out.println("Data saved. Goodbye!");
                    break;
                default: System.out.println("Invalid option. Please choose 1-8.");
            }
        }
        sc.close();
    }
}