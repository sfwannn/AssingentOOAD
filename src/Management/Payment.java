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

        // Free if handicapped vehicle (OKU card holder) parks in handicapped spot
        if ("HANDICAPPED".equalsIgnoreCase(vehicleType) && OKUCardChecker.isOKUCardHolder(licensePlate) && "Handicapped".equalsIgnoreCase(spotType)) {
            return fineStrategy.calculateFine(hours) + (isReservedMisuse ? 50.0 : 0.0); // Only fine, no parking fee
        }
        // If vehicle is handicapped and is OKU card holder, always RM 2/hour in any spot
        if ("HANDICAPPED".equalsIgnoreCase(vehicleType) && OKUCardChecker.isOKUCardHolder(licensePlate)) {
            rate = 2.0;
        } else {
            switch (spotType) {
                case "Compact": rate = 2.0; break;
                case "Regular": rate = 5.0; break;
                case "Handicapped": rate = 2.0; break; // Assumed paid unless validated card
                case "Reserved": rate = 10.0; break;
                default: rate = 5.0;
            }
        }

        double parkingFee = hours * rate;

        // Calculate Fines
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