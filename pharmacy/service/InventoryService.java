package pharmacy.service;

import pharmacy.model.Customer;
import pharmacy.model.Drug;

import java.util.List;
import java.util.Stack;

public final class InventoryService {

    private InventoryService() { }

    public static Drug findDrugById(List<Drug> inventory, String id) {
        if (id == null) return null;
        for (Drug drug : inventory) {
            if (drug.getId().equalsIgnoreCase(id.trim())) return drug;
        }
        return null;
    }

    public static Customer findCustomerById(List<Customer> customers, String id) {
        if (id == null) return null;
        for (Customer customer : customers) {
            if (customer.getId().equalsIgnoreCase(id.trim())) return customer;
        }
        return null;
    }

    public static void seedData(List<Drug> inventory,
                                List<Customer> customers,
                                Stack<String> actionLog) {
        inventory.add(new Drug("D101", "Paracetamol", 20.00, 15, "2027-05",
                "Pain and fever relief", "Pain Relief"));
        inventory.add(new Drug("D102", "Amoxicillin", 85.50, 40, "2027-02",
                "Antibiotic", "Antibiotics"));
        inventory.add(new Drug("D103", "Cetirizine", 12.00, 8, "2026-12",
                "Allergy relief", "Allergy"));
        customers.add(new Customer("C101", "Walk-in Customer", "0000000000"));
        actionLog.push("Sample data initialized.");
    }
}