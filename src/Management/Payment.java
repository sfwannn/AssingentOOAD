package Management;

import Database.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;

public class Payment {
    private Fine fineStrategy; // The selected strategy

    // Constructor requires a Fine Strategy (Dependency Injection)
    public Payment(Fine fineStrategy) {
        this.fineStrategy = fineStrategy;
    }

    // 1. CALCULATE TOTAL FEE
    public double calculateTotal(LocalDateTime entryTime, LocalDateTime exitTime, String spotType, boolean isReservedMisuse, String licensePlate, String vehicleType) {
        // Calculate Duration (Ceiling Rounding)
        long minutes = Duration.between(entryTime, exitTime).toMinutes();
        long hours = (long) Math.ceil(minutes / 60.0);
        if (hours <= 0) hours = 1; // Minimum 1 hour charge

        // Base Parking Rates 
        double rate;

        // FIXED LOGIC: If vehicle is handicapped, has a valid card, AND is in a handicapped spot, rate is 0
        if ("Handicapped Vehicle".equalsIgnoreCase(vehicleType) && OKUCardChecker.isOKUCardHolder(licensePlate) && "Handicapped".equalsIgnoreCase(spotType)) {
            rate = 0.0;
        } 
        // If vehicle has OKU card but is in a regular/compact/reserved spot, the rate is RM 2
        else if (OKUCardChecker.isOKUCardHolder(licensePlate)) {
            rate = 2.0;
        } 
        // Standard rates for everyone else
        else {
            switch (spotType) {
                case "Compact": rate = 2.0; break;
                case "Regular": rate = 5.0; break;
                case "Handicapped": rate = 2.0; break; // Paid for non-card holders
                case "Reserved": rate = 10.0; break;
                default: rate = 5.0;
            }
        }

        double parkingFee = hours * rate;

        // Calculate Fines from overstay strategy
        double fineAmount = fineStrategy.calculateFine(hours);
        
        // Add Misuse Fine if applicable (e.g. non-reserved car in reserved spot)
        if (isReservedMisuse) {
            fineAmount += 50.0; // Flat penalty for misuse
        }

        return parkingFee + fineAmount;
    }

    // 2. SAVE TO DATABASE
    public void processPayment(double amount, String method) {
        String sql = "INSERT INTO payments (amount, method) VALUES (?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDouble(1, amount);
            stmt.setString(2, method);
            stmt.executeUpdate();
            
            System.out.println("Payment of RM " + amount + " (" + method + ") saved successfully.");
            
        } catch (SQLException e) {
            System.err.println("Payment Save Error: " + e.getMessage());
        }
    }
}