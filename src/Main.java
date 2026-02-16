import Database.Database;
import Database.DatabaseSetup;
import java.sql.Connection;

/**
 * Main.java - Entry point for the Parking System Application
 * Initializes database and launches the UI with shared data manager
 */
public class Main {
    public static void main(String[] args) {
        // Step 1: Initialize database
        System.out.println("Initializing database...");
        DatabaseSetup.initialize();

        try {
            // Step 2: Verify database connection
            try (Connection conn = Database.getConnection()) {
                System.out.println("Database connection successful.");
            }

            // Step 3: Create necessary tables and initialize dummy data
            try (Connection conn = Database.getConnection()) {
                DataInitializer.createDatabaseTables(conn);
            }

            // Step 5: Create shared data manager
            UIDataManager dataManager = new UIDataManager();

            // Step 6: Launch Main Page (entry point)
            javax.swing.SwingUtilities.invokeLater(() -> {
                MainPage mainPage = new MainPage(dataManager);
                mainPage.setVisible(true);
            });

        } catch (Exception e) {
            System.err.println("Application startup error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
