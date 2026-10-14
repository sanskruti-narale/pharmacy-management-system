# Pharmacy Management System

A beginner-friendly, console-based Pharmacy Management System developed using **Core Java**. The project demonstrates basic inventory management, customer management, point-of-sale (POS) checkout, search, expiry and low-stock alerts, and local file-based data persistence.

## Features

### Drug Management
- View the available drug inventory.
- Add new drugs with ID, name, price, quantity, expiry month, description, and category.
- Update stock quantities.
- Edit drug details.
- Delete drugs.

### Customer Management
- Register new customers.
- View registered customers.
- Edit customer details.
- Delete customers.

### Cart and POS Checkout
- Add drugs to a cart.
- View cart items and calculate the total.
- Remove items from the cart or clear the cart.
- Check available stock before adding items.
- Confirm checkout to deduct quantities from inventory and record sales.

### Search and Alerts
- Search drugs by ID, name, or category.
- Display low-stock alerts.
- Identify expired, expiring, and soon-to-expire drugs.

### Data Persistence
- Save inventory, customer records, sales totals, and action logs to a local file named `pharmacy-data.ser`.
- Load saved data when the application starts, so changes can persist between runs.

## Technologies Used

- **Programming Language:** Java
- **Concepts:** Classes and objects, methods, loops, conditionals, input validation, exception handling, and Java Collections Framework
- **Collections:** `ArrayList`, `Map` / `HashMap`, and `Stack`
- **File handling:** Java object serialization for local persistence
- **Version control:** Git and GitHub

## Requirements

- Java Development Kit (JDK) installed
- A terminal or command prompt

Check that Java is installed:

```bash
java -version
javac -version
```

## How to Run

1. Clone or download this repository.
2. Open a terminal in the project directory.
3. Compile the source file:

   ```bash
   javac PharmacySystem.java
   ```

4. Run the application:

   ```bash
   java PharmacySystem
   ```

On the first run, the application creates sample data and saves it locally. Later runs load the saved data file.

## Main Menu

The application provides these menu areas:

1. Drug Management
2. Customer Management
3. Cart / POS Checkout
4. Search Drugs
5. Expiry and Low-Stock Alerts
6. View Action Log
7. Sales Report
8. Save and Exit

Submenus continue to display after an operation until the user chooses the option to return to the main menu or exit that submenu.

## Data File Notes

- `pharmacy-data.ser` is created in the application's **working directory**.
- Keep the data file in the same working directory when you want to continue using the saved records.

## Project Structure

```text
Pharmacy-Management-System/
├── PharmacySystem.java
├── README.md
└── pharmacy-data.ser   # Created locally when the application runs;
```

## Learning Goals

This project is intended to practise Core Java programming, collections, input validation, basic business logic, file handling, and version control through a practical console application.

## Author

Sanskruti Narale
