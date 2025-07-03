import java.util.Scanner;

// MongoDB driver imports
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

// Customer class represents a customer with an ID and name
class Customer {
    protected int customerId;  // Customer's unique ID
    protected String customerName;  // Customer's name

    // Constructor to initialize customer details
    public Customer(int customerId, String customerName) {
        this.customerId = customerId;
        this.customerName = customerName;
    }

    // Getter method for customerId
    public int getCustomerId() {
        return customerId;
    }

    // Getter method for customerName
    public String getCustomerName() {
        return customerName;
    }
}

// ElectricityBill class extends Customer and adds billing functionality
class ElectricityBill extends Customer {
    private int unitsConsumed;  // Number of units consumed by the customer

    // Constructor to initialize electricity bill details
    public ElectricityBill(int customerId, String customerName, int unitsConsumed) {
        super(customerId, customerName);  // Call the parent class constructor
        this.unitsConsumed = unitsConsumed;
    }

    // Method to calculate the electricity bill amount based on units consumed
    public double calculateAmount() {
        double amount = 0;

        if (unitsConsumed <= 50) {
            amount = 0.50 * unitsConsumed;
        } else if (unitsConsumed <= 150) {
            amount = (50 * 0.5) + (unitsConsumed - 50) * 0.75;
        } else if (unitsConsumed < 250) {
            amount = (unitsConsumed - 150) * 1.20 + (50 * 0.50) + (100 * 0.75);
        } else {
            amount = (unitsConsumed - 250) * 1.50 + (100 * 1.20) + (100 * 0.75) + (50 * 0.5);
        }

        return amount;
    }

    // Method to calculate a surcharge (20% of the amount)
    public double calculateSurcharge(double amount) {
        return 0.2 * amount;
    }

    // Method to calculate total amount including surcharge
    public double calculateTotalAmount(double amount, double surcharge) {
        return amount + surcharge;
    }

    // Method to print the electricity bill
    public void printBill() {
        double amount = calculateAmount();
        double surcharge = calculateSurcharge(amount);
        double totalAmount = calculateTotalAmount(amount, surcharge);

        System.out.println("\n--- Electricity Bill ---");
        System.out.println("Customer ID: " + getCustomerId());
        System.out.println("Customer Name: " + getCustomerName());
        System.out.println("Units Consumed: " + unitsConsumed);
        System.out.println("Amount: Rs. " + String.format("%.2f", amount));
        System.out.println("Surcharge (20%): Rs. " + String.format("%.2f", surcharge));
        System.out.println("Total Amount: Rs. " + String.format("%.2f", totalAmount));
    }

    // Method to convert bill data to MongoDB Document
    public Document toDocument() {
        double amount = calculateAmount();
        double surcharge = calculateSurcharge(amount);
        double totalAmount = calculateTotalAmount(amount, surcharge);

        Document doc = new Document("customerId", customerId)
                .append("customerName", customerName)
                .append("unitsConsumed", unitsConsumed)
                .append("amount", amount)
                .append("surcharge", surcharge)
                .append("totalAmount", totalAmount);

        return doc;
    }
}

public class EBill {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter Customer ID: ");
        int customerId = scanner.nextInt();
        scanner.nextLine();  // Consume newline

        System.out.print("Enter Customer Name: ");
        String customerName = scanner.nextLine();

        System.out.print("Enter Units Consumed: ");
        int unitsConsumed = scanner.nextInt();

        if (unitsConsumed < 0) {
            System.out.println("Units consumed cannot be negative.");
            scanner.close();
            return;
        }

        // Create bill
        ElectricityBill bill = new ElectricityBill(customerId, customerName, unitsConsumed);
        bill.printBill();

        // ✅ Connect to MongoDB Atlas and insert bill
        String uri = "mongodb+srv://username:password@cluster0.wx473j7.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";

        try (MongoClient mongoClient = MongoClients.create(uri)) {
            MongoDatabase database = mongoClient.getDatabase("billing"); // Creates 'billing' DB if not exists
            MongoCollection<Document> collection = database.getCollection("electricity_bills"); // Creates collection if not exists

            Document doc = bill.toDocument();
            collection.insertOne(doc);

            System.out.println("\n✅ Bill inserted into MongoDB Atlas successfully!");
        } catch (Exception e) {
            System.out.println("❌ Failed to connect or insert: " + e.getMessage());
        }

        scanner.close();
    }
}
