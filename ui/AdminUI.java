import java.awt.*;
import java.util.Calendar;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import Database.Database;

public class AdminUI extends JFrame {

    private JPanel mainPanel;
    private CardLayout cardLayout;
    private UIDataManager dataManager;
    private MainPage mainPage;

    private static final Dimension DEFAULT_BUTTON_SIZE = new Dimension(140, 34);

    private String[] floors = {"Floor 1", "Floor 2", "Floor 3", "Floor 4", "Floor 5"};
    private String[] fineSchemes = {"Option A (Fixed)", "Option B (Progressive)", "Option C (Hourly)"};

    private JLabel adminFineSchemeLabel;
    private JLabel page14FineSchemeLabel;
    
    // Parking management table references for dynamic updates
    private DefaultTableModel parkingTableModel;
    private JComboBox<String> parkingFloorCombo;
    private JLabel dashboardOutstandingFinesLabel;
    private JLabel dashboardOccupancyLabel;
    private JLabel dashboardRevenueLabel;

    public AdminUI(UIDataManager dataManager, MainPage mainPage) {
        this.dataManager = dataManager;
        this.mainPage = mainPage;

        setTitle("Poke Mall Parking System - Admin");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Initialize All Admin Pages
        mainPanel.add(createPage9(), "Page9");
        mainPanel.add(createPage10(), "Page10");
        mainPanel.add(createPage11(), "Page11");
        mainPanel.add(createPage12(), "Page12");
        mainPanel.add(createPage13(), "Page13");
        mainPanel.add(createPage14(), "Page14");
        mainPanel.add(createPage15(), "Page15");
        mainPanel.add(createPage16(), "Page16");

        applyGlobalButtonSizing(mainPanel);

        add(mainPanel);
        cardLayout.show(mainPanel, "Page9");
    }

    private void showCard(String cardName) {
        if (cardName.equals("Page14")) {
            if (page14FineSchemeLabel != null) {
                page14FineSchemeLabel.setText("Current Fine Scheme: " + dataManager.getCurrentFineScheme());
            }
        }
        
        if (cardName.equals("Page10")) {
            refreshDashboard();
        }
        
        if (cardName.equals("Page12")) {
            refreshUserTable();
        }

        cardLayout.show(mainPanel, cardName);
    }

    private void applyGlobalButtonSizing(Container root) {
        for (Component component : root.getComponents()) {
            if (component instanceof JButton) {
                ((JButton) component).setPreferredSize(DEFAULT_BUTTON_SIZE);
            } else if (component instanceof Container) {
                applyGlobalButtonSizing((Container) component);
            }
        }
    }

    // --- PAGE 9: Admin Login ---
    private JPanel createPage9() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel lbl = new JLabel("ADMIN LOGIN");
        lbl.setFont(new Font("Arial", Font.BOLD, 20));

        JTextField userField = new JTextField(15);
        userField.setBorder(BorderFactory.createTitledBorder("Username (Name)"));
        
        JPasswordField passField = new JPasswordField(15);
        passField.setBorder(BorderFactory.createTitledBorder("Password (Staff ID)"));

        JButton btnLogin = new JButton("Login");
        btnLogin.addActionListener(e -> {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword()).trim();
            
            if (dataManager.authenticateUser(username, password)) {
                showCard("Page10");
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Invalid username or password!",
                    "Login Failed", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton btnBack = new JButton("Back");
        btnBack.addActionListener(e -> {
            this.setVisible(false);
            this.dispose();
            if (mainPage != null) {
                mainPage.refreshFineScheme();
                mainPage.setVisible(true);
            }
        });

        gbc.gridx = 0; gbc.gridy = 0; panel.add(lbl, gbc);
        gbc.gridy = 1; panel.add(userField, gbc);
        gbc.gridy = 2; panel.add(passField, gbc);
        gbc.gridy = 3; panel.add(btnLogin, gbc);
        gbc.gridy = 4; panel.add(btnBack, gbc);

        return panel;
    }

    // --- PAGE 10: Admin Dashboard ---
    private JPanel createPage10() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createAdminSidebar(), BorderLayout.WEST);

        JPanel content = new JPanel(new GridLayout(2, 2, 20, 20));
        content.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Calculate occupancy dynamically
        int totalSpots = dataManager.getTotalSpotCount();
        int occupiedSpots = dataManager.getParkedVehicles().size();
        double occupancyRate = occupiedSpots > 0 ? (occupiedSpots / (double) totalSpots) * 100 : 0;
        
        dashboardOccupancyLabel = new JLabel("<html><center>" + String.format("Occupancy Rate: %.1f%% (%d/%d)", occupancyRate, occupiedSpots, totalSpots) + "</center></html>", SwingConstants.CENTER);
        dashboardOccupancyLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        dashboardOccupancyLabel.setOpaque(true);
        dashboardOccupancyLabel.setBackground(Color.LIGHT_GRAY);
        content.add(dashboardOccupancyLabel);
        
        double totalRevenue = dataManager.getTotalRevenue();
        dashboardRevenueLabel = new JLabel("<html><center>Total Revenue: RM " + String.format("%.2f", totalRevenue) + "</center></html>", SwingConstants.CENTER);
        dashboardRevenueLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        dashboardRevenueLabel.setOpaque(true);
        dashboardRevenueLabel.setBackground(Color.LIGHT_GRAY);
        content.add(dashboardRevenueLabel);
        
        dashboardOutstandingFinesLabel = new JLabel("<html><center>Outstanding Fines: RM " + String.format("%.2f", dataManager.getTotalUnpaidFines()) + "</center></html>", SwingConstants.CENTER);
        dashboardOutstandingFinesLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        dashboardOutstandingFinesLabel.setOpaque(true);
        dashboardOutstandingFinesLabel.setBackground(Color.LIGHT_GRAY);
        content.add(dashboardOutstandingFinesLabel);
        
        JButton btnReport = new JButton("Download Report");
        btnReport.addActionListener(e -> showCard("Page16"));
        content.add(btnReport);

        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    // --- PAGE 11: Parking Management ---
    private JPanel createPage11() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createAdminSidebar(), BorderLayout.WEST);

        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel();
        top.add(new JLabel("Floor:"));
        
        String[] floorOptions = new String[floors.length + 1];
        floorOptions[0] = "All Floors";
        for (int i = 0; i < floors.length; i++) {
            floorOptions[i + 1] = floors[i];
        }
        
        JComboBox<String> floorCombo = new JComboBox<>(floorOptions);
        top.add(floorCombo);
        content.add(top, BorderLayout.NORTH);

        String[] cols = {"Spot ID", "Plate No", "Type", "Entry", "Duration", "Unpaid Fine"};
        DefaultTableModel tableModel = new DefaultTableModel(cols, 0);
        JTable table = new JTable(tableModel);
        
        // Store references for dynamic updates
        this.parkingTableModel = tableModel;
        this.parkingFloorCombo = floorCombo;
        
        updateParkingManagementTable(tableModel, "All Floors");
        
        floorCombo.addActionListener(e -> {
            String selectedFloor = (String) floorCombo.getSelectedItem();
            updateParkingManagementTable(tableModel, selectedFloor);
        });
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        
        JPanel actionPanel = new JPanel();
        JButton btnFine = new JButton("Fine Vehicle");
        btnFine.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                String plateNo = (String) tableModel.getValueAt(row, 1);
                showFineOffenseDialog(plateNo);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a vehicle from the table to fine.");
            }
        });
        actionPanel.add(btnFine);
        centerPanel.add(actionPanel, BorderLayout.SOUTH);
        
        content.add(centerPanel, BorderLayout.CENTER);

        // Bottom: VIP and OKU Registration Info
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        bottomPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JPanel vipPanel = new JPanel(new BorderLayout());
        vipPanel.setBorder(BorderFactory.createTitledBorder("VIP/Reserved Registered Plates"));
        JTextArea vipArea = new JTextArea();
        vipArea.setEditable(false);
        vipArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        vipArea.setLineWrap(true);
        vipArea.setWrapStyleWord(true);
        updateVIPDisplay(vipArea);
        vipPanel.add(new JScrollPane(vipArea), BorderLayout.CENTER);
        
        JPanel vipBtnPanel = new JPanel();
        JButton btnAddVIP = new JButton("Add VIP");
        btnAddVIP.addActionListener(e -> showAddVIPDialog(vipArea));
        JButton btnDelVIP = new JButton("Delete VIP");
        btnDelVIP.addActionListener(e -> showDeleteVIPDialog(vipArea));
        vipBtnPanel.add(btnAddVIP);
        vipBtnPanel.add(btnDelVIP);
        vipPanel.add(vipBtnPanel, BorderLayout.SOUTH);
        
        JPanel okuPanel = new JPanel(new BorderLayout());
        okuPanel.setBorder(BorderFactory.createTitledBorder("OKU Card Holder Plates"));
        JTextArea okuArea = new JTextArea();
        okuArea.setEditable(false);
        okuArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        okuArea.setLineWrap(true);
        okuArea.setWrapStyleWord(true);
        updateOKUDisplay(okuArea);
        okuPanel.add(new JScrollPane(okuArea), BorderLayout.CENTER);
        
        JPanel okuBtnPanel = new JPanel();
        JButton btnAddOKU = new JButton("Add OKU");
        btnAddOKU.addActionListener(e -> showAddOKUDialog(okuArea));
        JButton btnDelOKU = new JButton("Delete OKU");
        btnDelOKU.addActionListener(e -> showDeleteOKUDialog(okuArea));
        okuBtnPanel.add(btnAddOKU);
        okuBtnPanel.add(btnDelOKU);
        okuPanel.add(okuBtnPanel, BorderLayout.SOUTH);
        
        bottomPanel.add(vipPanel);
        bottomPanel.add(okuPanel);
        
        content.add(bottomPanel, BorderLayout.SOUTH);

        panel.add(content, BorderLayout.CENTER);
        return panel;
    }
    
    private void updateVIPDisplay(JTextArea vipArea) {
        StringBuilder vipList = new StringBuilder();
        for (String plate : dataManager.getVIPPlates()) {
            vipList.append("• ").append(plate).append("\n");
        }
        vipArea.setText(vipList.length() > 0 ? vipList.toString() : "No VIP plates registered");
    }
    
    private void updateOKUDisplay(JTextArea okuArea) {
        StringBuilder okuList = new StringBuilder();
        for (String plate : dataManager.getOKUCardHolders()) {
            okuList.append("• ").append(plate).append("\n");
        }
        okuArea.setText(okuList.length() > 0 ? okuList.toString() : "No OKU card holders registered");
    }
    
    private void showFineOffenseDialog(String plateNo) {
        String[] offenses = {"Reserved Spot Without Reservation"};

        int choice = JOptionPane.showOptionDialog(
            this,
            "Select the offense for plate: " + plateNo,
            "Vehicle Fine",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            offenses,
            offenses[0]
        );

        if (choice >= 0) {
            double fineAmount = 100.0;
            String offenseType = "Reserved Spot Without Reservation";

            String normPlate = normalizePlate(plateNo);
            dataManager.issueFine(normPlate, fineAmount);

            JOptionPane.showMessageDialog(
                this,
                "Fine Issued Successfully!\n\n" +
                "Plate: " + plateNo + "\n" +
                "Offense: " + offenseType + "\n" +
                "Fine Amount: RM " + String.format("%.2f", fineAmount) + "\n" +
                "Total Unpaid Fines: RM " + String.format("%.2f", dataManager.getUnpaidFine(normPlate)) + "\n\n" +
                "This fine will be charged at vehicle exit.",
                "Fine Confirmation",
                JOptionPane.INFORMATION_MESSAGE
            );
            
            // Refresh the parking management table to show updated fines
            refreshParkingManagementTable();
            
            // Refresh dashboard outstanding fines
            refreshDashboardFines();
        }
    }
    
    private void refreshParkingManagementTable() {
        if (parkingTableModel != null && parkingFloorCombo != null) {
            String selectedFloor = (String) parkingFloorCombo.getSelectedItem();
            updateParkingManagementTable(parkingTableModel, selectedFloor != null ? selectedFloor : "All Floors");
        }
    }
    
    private void refreshDashboardFines() {
        if (dashboardOutstandingFinesLabel != null) {
            dashboardOutstandingFinesLabel.setText("<html><center>Outstanding Fines: RM " + String.format("%.2f", dataManager.getTotalUnpaidFines()) + "</center></html>");
        }
    }
    
    private void refreshDashboard() {
        // Refresh occupancy
        if (dashboardOccupancyLabel != null) {
            int totalSpots = dataManager.getTotalSpotCount();
            int occupiedSpots = dataManager.getParkedVehicles().size();
            double occupancyRate = occupiedSpots > 0 ? (occupiedSpots / (double) totalSpots) * 100 : 0;
            dashboardOccupancyLabel.setText("<html><center>" + String.format("Occupancy Rate: %.1f%% (%d/%d)", occupancyRate, occupiedSpots, totalSpots) + "</center></html>");
        }
        // Refresh revenue
        if (dashboardRevenueLabel != null) {
            double totalRevenue = dataManager.getTotalRevenue();
            dashboardRevenueLabel.setText("<html><center>Total Revenue: RM " + String.format("%.2f", totalRevenue) + "</center></html>");
        }
        // Refresh fines
        refreshDashboardFines();
    }
    
    private void showAddVIPDialog(JTextArea vipArea) {
        String plateNo = JOptionPane.showInputDialog(
            this,
            "Enter VIP Plate Number:",
            ""
        );
        
        if (plateNo != null && !plateNo.trim().isEmpty()) {
            plateNo = normalizePlate(plateNo);
            if (dataManager.isVIPPlate(plateNo)) {
                JOptionPane.showMessageDialog(this, "This plate is already registered as VIP.");
            } else {
                dataManager.addVIPPlate(plateNo);
                updateVIPDisplay(vipArea);
                JOptionPane.showMessageDialog(this, "VIP plate " + plateNo + " added successfully!");
            }
        }
    }
    
    private void showDeleteVIPDialog(JTextArea vipArea) {
        java.util.Set<String> vips = dataManager.getVIPPlates();
        if (vips.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No VIP plates to delete.");
            return;
        }
        
        String[] plates = vips.toArray(new String[0]);
        String selectedPlate = (String) JOptionPane.showInputDialog(
            this,
            "Select VIP plate to delete:",
            "Delete VIP Plate",
            JOptionPane.QUESTION_MESSAGE,
            null,
            plates,
            plates[0]
        );
        
        if (selectedPlate != null) {
            dataManager.removeVIPPlate(selectedPlate);
            updateVIPDisplay(vipArea);
            JOptionPane.showMessageDialog(this, "VIP plate " + selectedPlate + " deleted successfully!");
        }
    }
    
    private void showAddOKUDialog(JTextArea okuArea) {
        String plateNo = JOptionPane.showInputDialog(
            this,
            "Enter OKU Card Holder Plate Number:",
            ""
        );
        
        if (plateNo != null && !plateNo.trim().isEmpty()) {
            plateNo = normalizePlate(plateNo);
            if (dataManager.isOKUCardHolder(plateNo)) {
                JOptionPane.showMessageDialog(this, "This plate is already registered as OKU card holder.");
            } else {
                dataManager.addOKUCardHolder(plateNo);
                updateOKUDisplay(okuArea);
                JOptionPane.showMessageDialog(this, "OKU card holder plate " + plateNo + " added successfully!");
            }
        }
    }
    
    private void showDeleteOKUDialog(JTextArea okuArea) {
        java.util.Set<String> okus = dataManager.getOKUCardHolders();
        if (okus.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No OKU plates to delete.");
            return;
        }
        
        String[] plates = okus.toArray(new String[0]);
        String selectedPlate = (String) JOptionPane.showInputDialog(
            this,
            "Select OKU card holder plate to delete:",
            "Delete OKU Plate",
            JOptionPane.QUESTION_MESSAGE,
            null,
            plates,
            plates[0]
        );
        
        if (selectedPlate != null) {
            dataManager.removeOKUCardHolder(selectedPlate);
            updateOKUDisplay(okuArea);
            JOptionPane.showMessageDialog(this, "OKU card holder plate " + selectedPlate + " deleted successfully!");
        }
    }
    
    private void updateParkingManagementTable(DefaultTableModel model, String selectedFloor) {
        model.setRowCount(0);

        // Get all parked vehicles from database
        Map<String, UIDataManager.ParkedVehicleData> parkedVehicles = dataManager.getParkedVehicles();

        for (UIDataManager.ParkedVehicleData vehicle : parkedVehicles.values()) {
            String spotId = vehicle.parkingSpot;
            if (spotId == null || spotId.isEmpty()) continue;
            
            // Extract floor from spot ID (e.g., "F1-Reserved-R1S01" -> "1")
            int floor = dataManager.getFloorFromSpot(spotId);
            String floorLabel = "Floor " + floor;

            if (selectedFloor.equals("All Floors") || selectedFloor.contains(floorLabel)) {
                String normPlate = normalizePlate(vehicle.plate);
                
                // Calculate duration
                long now = System.currentTimeMillis();
                long elapsedMillis = now - vehicle.entryMillis;
                long hours = elapsedMillis / (1000 * 60 * 60);
                long minutes = (elapsedMillis / (1000 * 60)) % 60;
                String durationStr = String.format("%dh %02dm", hours, minutes);
                
                // Calculate fresh auto fine based on current duration and entry time scheme
                long hoursRoundedUp = Math.max(1L, (elapsedMillis + 60L*60*1000 - 1) / (60L*60*1000));
                double autoFine = dataManager.calculateFineForParkingAtEntryTime(hoursRoundedUp, vehicle.entryMillis, vehicle.plate);
                // Get manual fines (e.g., reserved spot violations)
                double manualFine = dataManager.getUnpaidFine(normPlate);
                // Total unpaid fine (fresh calculation)
                double unpaidFine = autoFine + manualFine;

                // Format entry date and time from entryMillis
                java.text.SimpleDateFormat dateTimeFormat = new java.text.SimpleDateFormat("dd-MMM HH:mm a");
                String entryDateTimeStr = dateTimeFormat.format(new java.util.Date(vehicle.entryMillis));

                Object[] rowData = new Object[6];
                rowData[0] = spotId;
                rowData[1] = vehicle.plate;
                rowData[2] = vehicle.vehicleType;
                rowData[3] = entryDateTimeStr;
                rowData[4] = durationStr;
                rowData[5] = unpaidFine > 0.0 ? "RM " + String.format("%.2f", unpaidFine) : "RM 0.00";
                model.addRow(rowData);
            }
        }
    }
    

    // --- PAGE 12: User Management ---
    private DefaultTableModel userTableModel;
    
    private JPanel createPage12() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createAdminSidebar(), BorderLayout.WEST);

        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] cols = {"Name", "Staff ID"};
        userTableModel = new DefaultTableModel(cols, 0);
        JTable table = new JTable(userTableModel);
        content.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton btnAdd = new JButton("Add New");
        btnAdd.addActionListener(e -> showCard("Page13"));
        
        JButton btnDel = new JButton("Delete");
        btnDel.addActionListener(e -> {
            int row = table.getSelectedRow();
            if(row != -1) {
                String userId = (String) userTableModel.getValueAt(row, 1);
                int confirm = JOptionPane.showConfirmDialog(this, "Do you want to delete staff " + userId + "?");
                if(confirm == JOptionPane.YES_OPTION) {
                    dataManager.deleteUser(userId);
                    refreshUserTable();
                    JOptionPane.showMessageDialog(this, "Staff deleted successfully!");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a staff to delete.");
            }
        });

        btnPanel.add(btnAdd);
        btnPanel.add(btnDel);
        content.add(btnPanel, BorderLayout.SOUTH);

        panel.add(content, BorderLayout.CENTER);
        return panel;
    }
    
    private void refreshUserTable() {
        if (userTableModel != null) {
            userTableModel.setRowCount(0);
            java.util.List<java.util.Map<String, String>> users = dataManager.getAllUsers();
            for (java.util.Map<String, String> user : users) {
                userTableModel.addRow(new Object[]{
                    user.get("name"),
                    user.get("user_id")
                });
            }
        }
    }

    // --- PAGE 13: Add User ---
    private JPanel createPage13() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createAdminSidebar(), BorderLayout.WEST);

        JPanel content = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JTextField nameF = new JTextField(15);
        nameF.setBorder(BorderFactory.createTitledBorder("Name"));
        JTextField staffIdF = new JTextField(15);
        staffIdF.setBorder(BorderFactory.createTitledBorder("Staff ID"));

        JButton btnConfirm = new JButton("Confirm");
        btnConfirm.addActionListener(e -> {
            String name = nameF.getText().trim();
            String staffId = staffIdF.getText().trim();
            
            if (name.isEmpty() || staffId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.");
                return;
            }
            
            if (dataManager.isUserExists(staffId)) {
                JOptionPane.showMessageDialog(this, "Staff ID already exists!");
                return;
            }
            
            // Use Staff ID as password for login
            dataManager.addUser(staffId, name, staffId, "STAFF");
            refreshUserTable();
            JOptionPane.showMessageDialog(this, "Staff " + staffId + " added successfully!\nPassword is the Staff ID.");
            nameF.setText("");
            staffIdF.setText("");
            showCard("Page12");
        });
        JButton btnBack = new JButton("Back");
        btnBack.addActionListener(e -> showCard("Page12"));

        gbc.gridx = 0; gbc.gridy = 0; content.add(new JLabel("Add New Staff"), gbc);
        gbc.gridy = 1; content.add(nameF, gbc);
        gbc.gridy = 2; content.add(staffIdF, gbc);
        gbc.gridy = 3; content.add(btnConfirm, gbc);
        gbc.gridy = 4; content.add(btnBack, gbc);

        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    // --- PAGE 14: Fine Management ---
    private JPanel createPage14() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createAdminSidebar(), BorderLayout.WEST);

        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        page14FineSchemeLabel = new JLabel("Current Fine Scheme: " + dataManager.getCurrentFineScheme());
        page14FineSchemeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        page14FineSchemeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JComboBox<String> schemeCombo = new JComboBox<>(fineSchemes);
        schemeCombo.setBorder(BorderFactory.createTitledBorder("Select Fine Scheme"));
        schemeCombo.setSelectedItem(dataManager.getCurrentFineScheme());

        gbc.gridx = 0; gbc.gridy = 0; topPanel.add(page14FineSchemeLabel, gbc);
        gbc.gridy = 1; topPanel.add(schemeCombo, gbc);
        
        JTextArea detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        detailsArea.setBorder(BorderFactory.createTitledBorder("Fine Scheme Details"));
        
        detailsArea.setText(getFineSchemeDetails((String) schemeCombo.getSelectedItem()));
        
        schemeCombo.addActionListener(e -> {
            String selectedScheme = (String) schemeCombo.getSelectedItem();
            detailsArea.setText(getFineSchemeDetails(selectedScheme));
            detailsArea.setCaretPosition(0);
        });

        JPanel bottomPanel = new JPanel();
        JButton btnGen = new JButton("Apply");
        btnGen.setPreferredSize(new Dimension(150, 40));
        btnGen.addActionListener(e -> {
            String selectedScheme = (String) schemeCombo.getSelectedItem();
            dataManager.setCurrentFineScheme(selectedScheme);
            if (page14FineSchemeLabel != null) page14FineSchemeLabel.setText("Current Fine Scheme: " + selectedScheme);
            JOptionPane.showMessageDialog(this, 
                "Fine Scheme Updated!\n\n" +
                "Current Scheme: " + selectedScheme + "\n\n" +
                "This scheme will apply to future fines issued.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
        });
        bottomPanel.add(btnGen);

        content.add(topPanel, BorderLayout.NORTH);
        content.add(new JScrollPane(detailsArea), BorderLayout.CENTER);
        content.add(bottomPanel, BorderLayout.SOUTH);

        panel.add(content, BorderLayout.CENTER);
        return panel;
    }
    
    private String getFineSchemeDetails(String scheme) {
        if (scheme == null || scheme.isEmpty()) {
            return "Please select a fine scheme.";
        }
        
        switch (scheme) {
            case "Option A (Fixed)":
                return "OPTION A: FIXED FINE SCHEME\n\n" +
                       "Description:\nA flat fine applied for any overstaying scenario.\n\n" +
                       "Fine Amount:\n• RM 50 - Flat fine for overstaying\n\n" +
                       "How it works:\n- Regardless of how long a vehicle overstays,\n" +
                       "  the fine is always RM 50.\n- This scheme is simple and uniform.\n- Ideal for standard enforcement.";
            case "Option B (Progressive)":
                return "OPTION B: PROGRESSIVE FINE SCHEME\n\n" +
                       "Description:\nFine increases based on overstay duration.\n\n" +
                       "Fine Schedule:\n• 0-24 hours: RM 50\n• 24-48 hours: RM 150\n" +
                       "• 48-72 hours: RM 300\n• Above 72 hours: RM 500\n\n" +
                       "How it works:\n- Longer overstays incur progressively higher fines.\n" +
                       "- Encourages quicker vehicle removal.\n- Ideal for maximum deterrence.";
            case "Option C (Hourly)":
                return "OPTION C: HOURLY FINE SCHEME\n\n" +
                       "Description:\nFine accumulates based on overstay hours.\n\n" +
                       "Fine Amount:\n• RM 20 per hour for overstaying\n\n" +
                       "How it works:\n- Each hour of overstay incurs RM 20 fine.\n" +
                       "- Example: 3 hours overstay = RM 60 fine\n- Fines are added to customer's account.\n" +
                       "- Customers can pay fines when exiting.\n- Unpaid fines carry over to next parking.\n" +
                       "- Fair and proportional to violation duration.";
            default:
                return "Please select a valid fine scheme.";
        }
    }

    // --- PAGE 15: Download Report ---
    // --- PAGE 15: Test Fine Calculator ---
    private JPanel createPage15() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createAdminSidebar(), BorderLayout.WEST);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("Test Fine Calculator - Manual Exit Time");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        content.add(titleLabel, gbc);

        // Vehicle selection
        gbc.gridy++; gbc.gridwidth = 1;
        content.add(new JLabel("Select Parked Vehicle:"), gbc);
        
        gbc.gridx = 1;
        Map<String, UIDataManager.ParkedVehicleData> parkedVehicles = dataManager.getParkedVehicles();
        String[] vehiclePlates = parkedVehicles.keySet().toArray(new String[0]);
        JComboBox<String> vehicleCombo = new JComboBox<>(vehiclePlates);
        vehicleCombo.setPreferredSize(new Dimension(200, 25));
        content.add(vehicleCombo, gbc);

        // Entry time display
        gbc.gridx = 0; gbc.gridy++;
        content.add(new JLabel("Entry Time:"), gbc);
        
        gbc.gridx = 1;
        JLabel entryTimeLabel = new JLabel("-");
        entryTimeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        content.add(entryTimeLabel, gbc);

        // Manual exit time inputs
        gbc.gridx = 0; gbc.gridy++;
        content.add(new JLabel("Manual Exit Time:"), gbc);

        JPanel dateTimePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        // Year
        dateTimePanel.add(new JLabel("Year:"));
        JTextField yearField = new JTextField("2026", 4);
        dateTimePanel.add(yearField);
        
        // Month
        dateTimePanel.add(new JLabel("Month:"));
        JComboBox<String> monthCombo = new JComboBox<>(new String[]{"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"});
        monthCombo.setSelectedItem("02");
        dateTimePanel.add(monthCombo);
        
        // Day
        dateTimePanel.add(new JLabel("Day:"));
        JComboBox<String> dayCombo = new JComboBox<>();
        for (int i = 1; i <= 31; i++) {
            dayCombo.addItem(String.format("%02d", i));
        }
        dayCombo.setSelectedItem("17");
        dateTimePanel.add(dayCombo);
        
        // Hour
        dateTimePanel.add(new JLabel("Hour:"));
        JComboBox<String> hourCombo = new JComboBox<>();
        for (int i = 0; i <= 23; i++) {
            hourCombo.addItem(String.format("%02d", i));
        }
        dateTimePanel.add(hourCombo);
        
        // Minute
        dateTimePanel.add(new JLabel("Min:"));
        JComboBox<String> minCombo = new JComboBox<>();
        for (int i = 0; i <= 59; i++) {
            minCombo.addItem(String.format("%02d", i));
        }
        dateTimePanel.add(minCombo);
        
        gbc.gridx = 1;
        content.add(dateTimePanel, gbc);

        // Results area
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        JTextArea resultArea = new JTextArea(8, 50);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        resultArea.setBorder(BorderFactory.createTitledBorder("Calculation Results"));
        JScrollPane scrollPane = new JScrollPane(resultArea);
        content.add(scrollPane, gbc);

        // Calculate button
        gbc.gridy++;
        JButton calculateBtn = new JButton("Calculate Fine");
        calculateBtn.setFont(new Font("Arial", Font.BOLD, 14));
        calculateBtn.addActionListener(e -> {
            if (vehiclePlates.length == 0) {
                resultArea.setText("No parked vehicles available.");
                return;
            }
            
            String selectedPlate = (String) vehicleCombo.getSelectedItem();
            UIDataManager.ParkedVehicleData vehicleData = parkedVehicles.get(selectedPlate);
            
            try {
                // Parse manual exit time
                int year = Integer.parseInt(yearField.getText());
                int month = Integer.parseInt((String) monthCombo.getSelectedItem()) - 1; // Calendar months are 0-based
                int day = Integer.parseInt((String) dayCombo.getSelectedItem());
                int hour = Integer.parseInt((String) hourCombo.getSelectedItem());
                int minute = Integer.parseInt((String) minCombo.getSelectedItem());
                
                Calendar exitCal = Calendar.getInstance();
                exitCal.set(year, month, day, hour, minute, 0);
                exitCal.set(Calendar.MILLISECOND, 0);
                long exitMillis = exitCal.getTimeInMillis();
                
                // Get entry time
                long entryMillis = vehicleData.entryMillis;
                
                if (exitMillis <= entryMillis) {
                    resultArea.setText("Error: Exit time must be after entry time!");
                    return;
                }
                
                // Calculate parking duration and charges
                double durationHours = (exitMillis - entryMillis) / (1000.0 * 60 * 60);
                double ceilingHours = Math.ceil(durationHours);
                double hourlyRate = dataManager.getHourlyRate(vehicleData.parkingSpot, vehicleData.vehicleType, selectedPlate);
                double parkingCharge = ceilingHours * hourlyRate;
                
                // Get fine scheme at entry time
                String schemeAtEntry = dataManager.getFineSchemeAtTime(entryMillis);
                long hoursParked = (long) ceilingHours;
                double fineAtEntry = dataManager.calculateFineForParkingAtEntryTime(hoursParked, entryMillis, selectedPlate);
                
                // Format output
                StringBuilder output = new StringBuilder();
                output.append("=== Fine Calculation Test ===").append("\n\n");
                output.append("Vehicle: ").append(selectedPlate).append("\n");
                output.append("Entry Time: ").append(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(entryMillis))).append("\n");
                output.append("Manual Exit Time: ").append(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(exitMillis))).append("\n");
                output.append("Duration: ").append(String.format("%.2f", durationHours)).append(" hours (Ceiling: ").append((int)ceilingHours).append(" hours)").append("\n");
                output.append("Parking Charge: RM ").append(String.format("%.2f", parkingCharge)).append("\n\n");
                output.append("--- Fine Scheme at Entry Time ---").append("\n");
                output.append("Scheme: ").append(schemeAtEntry).append("\n");
                output.append("Fine Amount: RM ").append(String.format("%.2f", fineAtEntry)).append("\n\n");
                output.append("Note: The system uses the fine scheme active at ENTRY TIME.");
                
                resultArea.setText(output.toString());
                
            } catch (Exception ex) {
                resultArea.setText("Error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        content.add(calculateBtn, gbc);

        // Update entry time when vehicle selection changes
        vehicleCombo.addActionListener(e -> {
            String selectedPlate = (String) vehicleCombo.getSelectedItem();
            if (selectedPlate != null) {
                UIDataManager.ParkedVehicleData vehicleData = parkedVehicles.get(selectedPlate);
                if (vehicleData != null) {
                    String entryTimeStr = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(vehicleData.entryMillis));
                    entryTimeLabel.setText(entryTimeStr);
                }
            }
        });
        
        // Initialize with first vehicle if available
        if (vehiclePlates.length > 0) {
            vehicleCombo.setSelectedIndex(0);
            UIDataManager.ParkedVehicleData firstVehicle = parkedVehicles.get(vehiclePlates[0]);
            String entryTimeStr = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(firstVehicle.entryMillis));
            entryTimeLabel.setText(entryTimeStr);
        }

        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    // --- PAGE 16: Download Report ---
    private JPanel createPage16() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createAdminSidebar(), BorderLayout.WEST);

        JPanel content = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JCheckBox c1 = new JCheckBox("Revenue Report");
        JCheckBox c2 = new JCheckBox("Occupancy Report");
        JCheckBox c3 = new JCheckBox("Fine Report");

        JButton btnProc = new JButton("Download CSV");
        btnProc.addActionListener(e -> {
            if (!c1.isSelected() && !c2.isSelected() && !c3.isSelected()) {
                JOptionPane.showMessageDialog(this, "Please select at least one report type.");
                return;
            }
            
            try {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Save Report");
                fileChooser.setSelectedFile(new java.io.File("parking_report.csv"));
                
                int userSelection = fileChooser.showSaveDialog(this);
                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    java.io.File fileToSave = fileChooser.getSelectedFile();
                    generateCSVReport(fileToSave, c1.isSelected(), c2.isSelected(), c3.isSelected());
                    JOptionPane.showMessageDialog(this, "Report saved successfully to:\n" + fileToSave.getAbsolutePath());
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error generating report: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridx = 0; gbc.gridy = 0; content.add(new JLabel("Select Reports:"), gbc);
        gbc.gridy = 1; content.add(c1, gbc);
        gbc.gridy = 2; content.add(c2, gbc);
        gbc.gridy = 3; content.add(c3, gbc);
        gbc.gridy = 4; content.add(btnProc, gbc);
        
        gbc.gridy = 5; 
        JLabel finesInfoLabel = new JLabel("Current Unpaid Fines: RM " + String.format("%.2f", dataManager.getTotalUnpaidFines()));
        finesInfoLabel.setFont(new Font("Arial", Font.BOLD, 12));
        content.add(finesInfoLabel, gbc);

        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    // --- Shared Admin Sidebar ---
    private JPanel createAdminSidebar() {
        JPanel sidebar = new JPanel(new GridLayout(6, 1, 5, 5));
        sidebar.setPreferredSize(new Dimension(150, 0));
        sidebar.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        String[] labels = {"Dashboard", "Parking Management", "User Management", "Fine Management", "Test Fine Calculator", "Logout"};
        String[] pages = {"Page10", "Page11", "Page12", "Page14", "Page15", "Page9"};

        for (int i=0; i<labels.length; i++) {
            String page = pages[i];
            JButton btn = new JButton(labels[i]);
            btn.addActionListener(e -> {
                if (page.equals("Page9")) {
                    // Logout - return to MainPage
                    this.setVisible(false);
                    this.dispose();
                    if (mainPage != null) {
                        mainPage.refreshFineScheme();
                        mainPage.setVisible(true);
                    }
                } else {
                    showCard(page);
                }
            });
            sidebar.add(btn);
        }
        
        return sidebar;
    }

    // Helper for labels
    private JLabel createBorderedLabel(String text) {
        JLabel l = new JLabel("<html><center>" + text.replaceAll("\n", "<br>") + "</center></html>", SwingConstants.CENTER);
        l.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        l.setOpaque(true);
        l.setBackground(Color.LIGHT_GRAY);
        return l;
    }

    private void generateCSVReport(java.io.File file, boolean revenue, boolean occupancy, boolean fines) throws java.io.IOException {
        java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(file));
        
        writer.println("Poke Mall Parking System - Report");
        writer.println("Generated: " + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
        writer.println("");
        
        if (revenue) {
            writer.println("REVENUE REPORT");
            double totalRevenue = dataManager.getTotalRevenue();
            writer.println("Total Revenue,RM " + String.format("%.2f", totalRevenue));
            writer.println("Payment Method,Amount");
            writer.println("Cash,RM 800.00");
            writer.println("Debit/Credit,RM 500.00");
            writer.println("QR,RM 200.00");
            writer.println("");
        }
        
        if (occupancy) {
            int totalSpots = dataManager.getTotalSpotCount();
            int occupiedSpots = dataManager.getParkedVehicles().size();
            writer.println("OCCUPANCY REPORT");
            writer.println("Total Spots," + totalSpots);
            writer.println("Occupied Spots," + occupiedSpots);
            writer.println("Available Spots," + (totalSpots - occupiedSpots));
            writer.println("Occupancy Rate," + String.format("%.1f%%", (occupiedSpots / (double) totalSpots) * 100));
            writer.println("");
        }
        
        if (fines) {
            writer.println("FINE REPORT");
            writer.println("Current Fine Scheme," + dataManager.getCurrentFineScheme());
            writer.println("Total Unpaid Fines,RM " + String.format("%.2f", dataManager.getTotalUnpaidFines()));
            writer.println("");
            writer.println("License Plate,Unpaid Fine Amount");
            
            // Get all unpaid fines from database (including vehicles not currently parked)
            java.util.Map<String, Double> allUnpaidFines = getAllUnpaidFinesFromDatabase();
            for (java.util.Map.Entry<String, Double> entry : allUnpaidFines.entrySet()) {
                writer.println(entry.getKey() + ",RM " + String.format("%.2f", entry.getValue()));
            }
        }
        
        writer.close();
    }

    private String normalizePlate(String plate) {
        if (plate == null) return "";
        return plate.replaceAll("\\s+", "").toUpperCase();
    }

    private java.util.Map<String, Double> getAllUnpaidFinesFromDatabase() {
        java.util.Map<String, Double> unpaidFines = new java.util.TreeMap<>();
        try (java.sql.Connection conn = Database.getConnection()) {
            if (conn != null) {
                java.sql.Statement stmt = conn.createStatement();
                java.sql.ResultSet rs = stmt.executeQuery("SELECT license_plate, amount FROM unpaid_fines ORDER BY license_plate");
                
                while (rs.next()) {
                    String plate = rs.getString("license_plate");
                    double amount = rs.getDouble("amount");
                    unpaidFines.put(plate, amount);
                }
                
                rs.close();
                stmt.close();
            }
        } catch (Exception e) {
            System.err.println("Error retrieving unpaid fines from database: " + e.getMessage());
            e.printStackTrace();
        }
        return unpaidFines;
    }
}
