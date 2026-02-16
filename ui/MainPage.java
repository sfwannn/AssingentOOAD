import java.awt.*;
import java.util.Map;
import javax.swing.*;

/**
 * MainPage - Entry point for the Parking System
 * Allows users to enter their plate and vehicle type
 * Routes to Customer UI for parking or Admin UI for management
 */
public class MainPage extends JFrame {

    private UIDataManager dataManager;

    private String[] vehicleTypes = {"-- Select Vehicle --", "Motorcycle", "Car", "SUV/Truck", "Handicapped Vehicle"};

    public MainPage(UIDataManager dataManager) {
        this.dataManager = dataManager;

        setTitle("Poke University Parking Lot Management Office");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);

        JPanel panel = createMainPanel();
        add(panel);
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel title = new JLabel("Welcome to Poke University Parking Lot");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        
        JLabel plateLabel = new JLabel("Enter Plate No.");
        JTextField plateInput = new JTextField(15);
        plateInput.setHorizontalAlignment(JTextField.CENTER);

        JLabel vehicleLabel = new JLabel("Vehicle Type");
        JComboBox<String> vehicleCombo = new JComboBox<>(vehicleTypes);

        JButton btnEnter = new JButton("Enter");
        btnEnter.setPreferredSize(new Dimension(140, 34));
        btnEnter.addActionListener(e -> {
            String plate = plateInput.getText().trim().toUpperCase();
            String vehicleType = (String) vehicleCombo.getSelectedItem();
            
            if (plate.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a plate number.");
            } else if (vehicleType == null || vehicleType.isEmpty() || vehicleType.equals("-- Select Vehicle --")) {
                JOptionPane.showMessageDialog(this, "Please select a vehicle type.");
            } else {
                String normalizedPlate = normalizePlate(plate);
                
                // Check if vehicle is already parked
                Map<String, UIDataManager.ParkedVehicleData> parkedVehicles = dataManager.getParkedVehicles();
                if (parkedVehicles.containsKey(normalizedPlate)) {
                    UIDataManager.ParkedVehicleData vehicle = parkedVehicles.get(normalizedPlate);
                    if (!vehicle.vehicleType.equals(vehicleType)) {
                        JOptionPane.showMessageDialog(this, 
                            "Vehicle type mismatch!\n" +
                            "Plate " + plate + " is registered as: " + vehicle.vehicleType + "\n" +
                            "You selected: " + vehicleType,
                            "Validation Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        // Vehicle already parked, go to exit page
                        this.setVisible(false);
                        CustomerUI customerUI = new CustomerUI(dataManager, normalizedPlate, vehicleType, true);
                        customerUI.setMainPage(this);
                        customerUI.setVisible(true);
                    }
                } else {
                    // New vehicle, go to parking page
                    this.setVisible(false);
                    CustomerUI customerUI = new CustomerUI(dataManager, normalizedPlate, vehicleType, false);
                    customerUI.setMainPage(this);
                    customerUI.setVisible(true);
                }
            }
        });

        JButton btnAdmin = new JButton("Admin");
        btnAdmin.setPreferredSize(new Dimension(140, 34));
        btnAdmin.addActionListener(e -> {
            this.setVisible(false);
            AdminUI adminUI = new AdminUI(dataManager, this);
            adminUI.setVisible(true);
        });

        gbc.gridx = 0; gbc.gridy = 0; panel.add(title, gbc);
        gbc.gridy = 1; panel.add(plateLabel, gbc);
        gbc.gridy = 2; panel.add(plateInput, gbc);
        gbc.gridy = 3; panel.add(vehicleLabel, gbc);
        gbc.gridy = 4; panel.add(vehicleCombo, gbc);
        gbc.gridy = 5; panel.add(btnEnter, gbc);
        gbc.gridy = 6; panel.add(btnAdmin, gbc);

        return panel;
    }

    public void refreshFineScheme() {
        // Fine scheme display removed from main page
    }

    private String normalizePlate(String plate) {
        if (plate == null) return "";
        return plate.replaceAll("\\s+", "").toUpperCase();
    }
}
