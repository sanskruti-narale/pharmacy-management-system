package pharmacy.model;

import java.io.Serializable;
import java.util.Locale;

public class Drug implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private String name;
    private double price;
    private int quantity;
    private String expiryDate;
    private String description;
    private String category;

    public Drug(String id, String name, double price, int quantity, String expiryDate,
                String description, String category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
        this.description = description;
        this.category = category;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public String getExpiryDate() { return expiryDate; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }

    public void setName(String name) { this.name = name; }
    public void setPrice(double price) { this.price = price; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(String category) { this.category = category; }

    @Override
    public String toString() {
        return String.format(Locale.US,
                "ID: %s | Name: %s | Price: Rs. %.2f | Stock: %d | Expiry: %s | Category: %s | Description: %s",
                id, name, price, quantity, expiryDate, category, description);
    }
}