import Database.Database;
import Management.Fine;
import Management.FixedFine;
import Management.HourlyFine;
import Management.ProgressiveFine;
import java.sql.*;
import java.util.*;

/**
 * Centralized data manager for UI - handles parking data, fees, fines, VIP/OKU cards
 * All operations are backed by database for persistence
 */
public class UIDataManager {
    
    private String currentFineScheme;

    // Inner class to hold parked vehicle data
    public static class ParkedVehicleData {
        public String plate;
        public String vehicleType;
        public String entryTime;
        public String parkingSpot;
        public long entryMillis;

        public ParkedVehicleData(String plate, String vehicleType, String entryTime, String parkingSpot, long entryMillis) {
            this.plate = plate;
            this.vehicleType = vehicleType;
            this.entryTime = entryTime;
            this.parkingSpot = parkingSpot;
            this.entryMillis = entryMillis;
        }
    }

    public UIDataManager() {
        this.currentFineScheme = loadFineSchemeFromDB();
    }

    // Load fine scheme from database or use default
    private String loadFineSchemeFromDB() {
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT scheme_name FROM fine_schemes WHERE is_active = 1 LIMIT 1");
            if (rs.next()) {
                return rs.getString("scheme_name");
            }
        } catch (SQLException e) {
            System.err.println("Error loading fine scheme: " + e.getMessage());
        }
        return "Option A (Fixed)";
    }

    // Save fine scheme to database
    private void saveFineSchemeToDB(String scheme) {
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()) {
            // Deactivate all schemes
            stmt.executeUpdate("UPDATE fine_schemes SET is_active = 0");
            // Activate selected scheme with current timestamp
            PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO fine_schemes (scheme_name, is_active, changed_at) VALUES (?, 1, CURRENT_TIMESTAMP) " +
                "ON DUPLICATE KEY UPDATE is_active = 1, changed_at = CURRENT_TIMESTAMP");
            pstmt.setString(1, scheme);
            pstmt.executeUpdate();
            
            // Log this scheme change to history for future reference
            PreparedStatement historyPstmt = conn.prepareStatement(
                "INSERT INTO fine_scheme_history (scheme_name, activated_at) VALUES (?, CURRENT_TIMESTAMP)");
            historyPstmt.setString(1, scheme);
            historyPstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error saving fine scheme: " + e.getMessage());
        }
    }

    // Getters and Setters for shared data
    public Map<String, ParkedVehicleData> getParkedVehicles() {
        Map<String, ParkedVehicleData> vehicles = new HashMap<>();
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT license_plate, vehicle_type, entry_time, parking_spot, entry_millis FROM parked_vehicles");
            while (rs.next()) {
                String plate = rs.getString("license_plate");
                vehicles.put(plate, new ParkedVehicleData(
                    plate,
                    rs.getString("vehicle_type"),
                    rs.getString("entry_time"),
                    rs.getString("parking_spot"),
                    rs.getLong("entry_millis")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error loading parked vehicles: " + e.getMessage());
        }
        return vehicles;
    }
    
    public ParkedVehicleData getParkedVehicleData(String plate) {
        String norm = normalizePlate(plate);
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                "SELECT license_plate, vehicle_type, entry_time, parking_spot, entry_millis FROM parked_vehicles WHERE license_plate = ?")) {
            pstmt.setString(1, norm);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new ParkedVehicleData(
                    rs.getString("license_plate"),
                    rs.getString("vehicle_type"),
                    rs.getString("entry_time"),
                    rs.getString("parking_spot"),
                    rs.getLong("entry_millis")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error getting parked vehicle data: " + e.getMessage());
        }
        return null;
    }

    public Set<String> getVIPPlates() {
        Set<String> plates = new HashSet<>();
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT license_plate FROM vip_plates");
            while (rs.next()) {
                plates.add(rs.getString("license_plate"));
            }
        } catch (SQLException e) {
            System.err.println("Error loading VIP plates: " + e.getMessage());
        }
        return plates;
    }

    public Set<String> getOKUCardHolders() {
        Set<String> holders = new HashSet<>();
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT license_plate FROM oku_card_holders");
            while (rs.next()) {
                holders.add(rs.getString("license_plate"));
            }
        } catch (SQLException e) {
            System.err.println("Error loading OKU card holders: " + e.getMessage());
        }
        return holders;
    }

    public String getCurrentFineScheme() {
        return currentFineScheme;
    }

    // Get the fine scheme that was active at a specific entry time
    public String getFineSchemeAtTime(long entryMillis) {
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                "SELECT scheme_name FROM fine_schemes " +
                "WHERE changed_at <= FROM_UNIXTIME(?) " +
                "ORDER BY changed_at DESC LIMIT 1")) {
            pstmt.setLong(1, entryMillis / 1000); // Convert milliseconds to seconds
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("scheme_name");
            }
        } catch (SQLException e) {
            System.err.println("Error getting fine scheme at time: " + e.getMessage());
        }
        return "Option A (Fixed)"; // Default fallback
    }

    public void setCurrentFineScheme(String scheme) {
        this.currentFineScheme = scheme;
        saveFineSchemeToDB(scheme);
    }

    // VIP Plate operations
    public boolean isVIPPlate(String plate) {
        String norm = normalizePlate(plate);
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT 1 FROM vip_plates WHERE license_plate = ?")) {
            pstmt.setString(1, norm);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Error checking VIP plate: " + e.getMessage());
            return false;
        }
    }

    public void addVIPPlate(String plate) {
        String norm = normalizePlate(plate);
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("INSERT IGNORE INTO vip_plates (license_plate) VALUES (?)")) {
            pstmt.setString(1, norm);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding VIP plate: " + e.getMessage());
        }
    }

    public void removeVIPPlate(String plate) {
        String norm = normalizePlate(plate);
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM vip_plates WHERE license_plate = ?")) {
            pstmt.setString(1, norm);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error removing VIP plate: " + e.getMessage());
        }
    }

    // OKU Card operations
    public boolean isOKUCardHolder(String plate) {
        String norm = normalizePlate(plate);
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT 1 FROM oku_card_holders WHERE license_plate = ?")) {
            pstmt.setString(1, norm);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Error checking OKU card holder: " + e.getMessage());
            return false;
        }
    }

    public void addOKUCardHolder(String plate) {
        String norm = normalizePlate(plate);
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("INSERT IGNORE INTO oku_card_holders (license_plate) VALUES (?)")) {
            pstmt.setString(1, norm);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding OKU card holder: " + e.getMessage());
        }
    }

    public void removeOKUCardHolder(String plate) {
        String norm = normalizePlate(plate);
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM oku_card_holders WHERE license_plate = ?")) {
            pstmt.setString(1, norm);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error removing OKU card holder: " + e.getMessage());
        }
    }

    // Fine operations
    public double getUnpaidFine(String plate) {
        String norm = normalizePlate(plate);
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT amount FROM unpaid_fines WHERE license_plate = ?")) {
            pstmt.setString(1, norm);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("amount");
            }
        } catch (SQLException e) {
            System.err.println("Error getting unpaid fine: " + e.getMessage());
        }
        return 0.0;
    }

    public void issueFine(String plate, double amount) {
        String norm = normalizePlate(plate);
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO unpaid_fines (license_plate, amount) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE amount = amount + ?")) {
            pstmt.setString(1, norm);
            pstmt.setDouble(2, amount);
            pstmt.setDouble(3, amount);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error issuing fine: " + e.getMessage());
        }
    }

    public void recordPayment(String plate, double amount) {
        recordPayment(plate, amount, 0.0, 0.0, "Cash/Card");
    }
    
    public void recordPayment(String plate, double amount, double parkingFee, double fineAmount) {
        recordPayment(plate, amount, parkingFee, fineAmount, "Cash/Card");
    }
    
    public void recordPayment(String plate, double amount, double parkingFee, double fineAmount, String paymentMethod) {
        String norm = normalizePlate(plate);
        try (Connection conn = Database.getConnection()) {
            double currentFine = getUnpaidFine(norm);
            double remaining = Math.max(0.0, currentFine - amount);
            
            // Insert into payments table with full details
            PreparedStatement paymentStmt = conn.prepareStatement("INSERT INTO payments (license_plate, amount, method, parking_fee, fine_amount) VALUES (?, ?, ?, ?, ?)");
            paymentStmt.setString(1, norm);
            paymentStmt.setDouble(2, amount);
            paymentStmt.setString(3, paymentMethod);
            paymentStmt.setDouble(4, parkingFee);
            paymentStmt.setDouble(5, fineAmount);
            paymentStmt.executeUpdate();
            
            if (remaining <= 0.0) {
                PreparedStatement pstmt = conn.prepareStatement("DELETE FROM unpaid_fines WHERE license_plate = ?");
                pstmt.setString(1, norm);
                pstmt.executeUpdate();
            } else {
                PreparedStatement pstmt = conn.prepareStatement("UPDATE unpaid_fines SET amount = ? WHERE license_plate = ?");
                pstmt.setDouble(1, remaining);
                pstmt.setString(2, norm);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error recording payment: " + e.getMessage());
        }
    }

    public double getTotalUnpaidFines() {
        double total = 0.0;
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT SUM(amount) as total FROM unpaid_fines");
            if (rs.next()) {
                total = rs.getDouble("total");
            }
        } catch (SQLException e) {
            System.err.println("Error getting total unpaid fines: " + e.getMessage());
        }
        return total;
    }

    // Parked vehicle operations
    public void addParkedVehicle(String plate, String vehicleType, String entryTime, String parkingSpot, long entryMillis) {
        String norm = normalizePlate(plate);
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO parked_vehicles (license_plate, vehicle_type, entry_time, parking_spot, entry_millis) " +
                "VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
                "vehicle_type = VALUES(vehicle_type), entry_time = VALUES(entry_time), " +
                "parking_spot = VALUES(parking_spot), entry_millis = VALUES(entry_millis)")) {
            pstmt.setString(1, norm);
            pstmt.setString(2, vehicleType);
            pstmt.setString(3, entryTime);
            pstmt.setString(4, parkingSpot);
            pstmt.setLong(5, entryMillis);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding parked vehicle: " + e.getMessage());
        }
    }

    public void removeParkedVehicle(String plate) {
        String norm = normalizePlate(plate);
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM parked_vehicles WHERE license_plate = ?")) {
            pstmt.setString(1, norm);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error removing parked vehicle: " + e.getMessage());
        }
    }

    public Map<String, Object> getParkedVehicleInfo(String plate) {
        String norm = normalizePlate(plate);
        Map<String, ParkedVehicleData> vehicles = getParkedVehicles();
        ParkedVehicleData vehicle = vehicles.get(norm);
        if (vehicle == null) return null;

        Map<String, Object> info = new HashMap<>();
        info.put("plate", vehicle.plate);
        info.put("vehicleType", vehicle.vehicleType);
        info.put("entryTime", vehicle.entryTime);
        info.put("parkingSpot", vehicle.parkingSpot);
        info.put("entryMillis", vehicle.entryMillis);

        // Calculate duration
        long now = System.currentTimeMillis();
        long elapsedMillis = now - vehicle.entryMillis;
        long hours = elapsedMillis / (1000 * 60 * 60);
        long minutes = (elapsedMillis / (1000 * 60)) % 60;
        info.put("duration", hours + "h " + minutes + "m");

        return info;
    }

    public double getTotalRevenue() {
        double total = 0.0;
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT SUM(amount) as total FROM payments");
            if (rs.next()) {
                total = rs.getDouble("total");
            }
        } catch (SQLException e) {
            System.err.println("Error getting total revenue: " + e.getMessage());
        }
        return total;
    }

    // User management operations
    public void addUser(String userId, String name, String password, String role) {
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO users (user_id, name, password, role) VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE name = VALUES(name), password = VALUES(password), role = VALUES(role)")) {
            pstmt.setString(1, userId);
            pstmt.setString(2, name);
            pstmt.setString(3, password);
            pstmt.setString(4, role);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding/updating user: " + e.getMessage());
        }
    }
    
    public void deleteUser(String userId) {
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM users WHERE user_id = ?")) {
            pstmt.setString(1, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
        }
    }
    
    public java.util.List<java.util.Map<String, String>> getAllUsers() {
        java.util.List<java.util.Map<String, String>> users = new java.util.ArrayList<>();
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT user_id, name, role FROM users WHERE role != 'ADMIN'");
            while (rs.next()) {
                java.util.Map<String, String> user = new java.util.HashMap<>();
                user.put("user_id", rs.getString("user_id"));
                user.put("name", rs.getString("name"));
                user.put("role", rs.getString("role"));
                users.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all users: " + e.getMessage());
        }
        return users;
    }
    
    public boolean isUserExists(String userId) {
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT 1 FROM users WHERE user_id = ?")) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Error checking user existence: " + e.getMessage());
        }
        return false;
    }
    
    public boolean authenticateUser(String userName, String password) {
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                "SELECT 1 FROM users WHERE name = ? AND password = ?")) {
            pstmt.setString(1, userName);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Error authenticating user: " + e.getMessage());
        }
        return false;
    }

    /**
     * Calculate fine for a vehicle based on hours parked and current fine scheme
     * This uses the Strategy Pattern with Fine implementations
     */
    public double calculateFineForParking(long hoursParked) {
        Fine fineStrategy = getFineStrategy();
        return fineStrategy.calculateFine(hoursParked);
    }

    // New method to calculate fine based on entry time (uses the scheme that was active at entry time)
    public double calculateFineForParkingAtEntryTime(long hoursParked, long entryMillis, String plate) {
        String schemeAtEntry = getFineSchemeAtTime(entryMillis);
        Fine fineStrategy = getFineStrategyByName(schemeAtEntry);
        double fine = fineStrategy.calculateFine(hoursParked);

        // Check if the vehicle has an OKU card (uses the `oku_card_holders` table)
        if (isOKUCardHolder(plate)) {
            fine = Math.max(0, fine - 2); // Subtract RM2, but ensure it doesn't go below zero
        }

        return fine;
    }

    // Legacy helper removed — use public isOKUCardHolder(String) which checks `oku_card_holders` table

    /**
     * Get the appropriate Fine strategy based on current fine scheme
     */
    private Fine getFineStrategy() {
        switch (currentFineScheme) {
            case "Option A (Fixed)":
                return new FixedFine();
            case "Option B (Progressive)":
                return new ProgressiveFine();
            case "Option C (Hourly)":
                return new HourlyFine();
            default:
                return new FixedFine();
        }
    }

    // Get fine strategy by scheme name
    private Fine getFineStrategyByName(String schemeName) {
        switch (schemeName) {
            case "Option A (Fixed)":
                return new FixedFine();
            case "Option B (Progressive)":
                return new ProgressiveFine();
            case "Option C (Hourly)":
                return new HourlyFine();
            default:
                return new FixedFine();
        }
    }

    // Get fine scheme that was active at a specific time (from history)
    public String getFineSchemeAtTime(long entryMillis) {
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                "SELECT scheme_name FROM fine_scheme_history " +
                "WHERE TIMESTAMP(activated_at) <= TIMESTAMP(FROM_UNIXTIME(?)) " +
                "ORDER BY activated_at DESC " +
                "LIMIT 1")) {
            pstmt.setLong(1, entryMillis / 1000); // Convert milliseconds to seconds
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String schemeName = rs.getString("scheme_name");
                System.out.println("DEBUG: Fine scheme for entry time " + new java.util.Date(entryMillis) + " = " + schemeName);
                return schemeName;
            }
        } catch (SQLException e) {
            System.err.println("Error getting fine scheme at time: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("DEBUG: No scheme found in history, using default Option A (Fixed)");
        return "Option A (Fixed)"; // Default fallback
    }

    // Utility method
    private String normalizePlate(String plate) {
        if (plate == null) return "";
        return plate.replaceAll("\\s+", "").toUpperCase();
    }
}
