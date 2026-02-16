package Management;

import Database.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class User {
    protected String id;
    protected String name;
    protected String role; // "ADMIN" or "CUSTOMER"

    public User(String id, String name, String role) {
        this.id = id;
        this.name = name;
        this.role = role;
    }

    // --- LOGIN LOGIC (Connects to Database) ---
    public static User login(String inputId, String inputPass) {
        String sql = "SELECT * FROM users WHERE user_id = ? AND password = ?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, inputId);
            stmt.setString(2, inputPass);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                String name = rs.getString("name");
                
                if ("ADMIN".equalsIgnoreCase(role)) {
                    return new Admin(inputId, name, inputPass);
                } else {
                    // For customers, the password might be null in DB
                    return new Customer(inputId, name, rs.getString("license_plate"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Login Error: " + e.getMessage());
        }
        return null; // Login failed
    }

    public String getName() { return name; }
    public String getId() { return id; }
    public String getRole() { return role; }
}