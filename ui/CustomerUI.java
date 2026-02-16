import java.awt.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class CustomerUI extends JFrame {

    private JPanel mainPanel;
    private CardLayout cardLayout;

    private static final Dimension DEFAULT_BUTTON_SIZE = new Dimension(140, 34);

    // Data
    private String[] vehicleTypes = {"Motorcycle", "Car", "SUV/Truck", "Handicapped Vehicle"};
    private String[] parkingTypes = {"Compact", "Regular", "Handicapped", "Reserved"};
    private String[] floors = {"Floor 1", "Floor 2", "Floor 3", "Floor 4", "Floor 5"};
    private String[] paymentTypes = {"Cash", "Debit/Credit", "QR"};

    // Runtime data storage (shared with AdminUI via shared manager)
    private Map<String, UIDataManager.ParkedVehicleData> parkedVehicles;
    private UIDataManager dataManager;
    private MainPage mainPage;
    private Set<String> finesSavedThisSession = new HashSet<>(); // Track which vehicles' fines have been saved

    // Track selected vehicle type and plate
    private String selectedVehicleType = "";
    private String selectedPlate = "";
    private String selectedParkingType = "";
    private long entryTime = 0;
    
    // Page 2 Components (to update dynamically)
    private JTextArea rateTextArea;
    private JComboBox<String> parkingComboBox;
    private JLabel homeFineSchemeLabel;
    private JLabel page5FineSchemeLabel;
    private JLabel page5PlateLabel;
    private JLabel page5EntryTimeLabel;
    private JLabel page5DurationLabel;
    private JLabel page5ParkingTypeLabel;
    private JLabel page5SpotLabel;
    private JLabel page5ParkingRateLabel;
    private JLabel page5FineLabel;
    private JLabel page5ParkingFeeLabel;

    // Track selected spot for the current parking session
    private String selectedSpotId = "";

    // Page4 (Thank You) dynamic labels
    private JLabel page4EntryTimeLabel;
    private JLabel page4SpotLabel;
    
    // Cache for receipt display - preserved after payment
    private UIDataManager.ParkedVehicleData lastReceiptData = null;
    private long lastExitTimeMillis = 0;
    private double lastParkingFee = 0.0;
    private double lastFine = 0.0;

    // Payment / receipt bookkeeping
    private String selectedPaymentMethod = "";
    private double amountPaid = 0.0;
    private long exitTimeMillis = 0L;

    // Receipt area (Page8)
    private JTextArea receiptArea;

    public CustomerUI(UIDataManager dataManager, String plate, String vehicleType, boolean isExiting) {
        this.dataManager = dataManager;
        this.parkedVehicles = dataManager.getParkedVehicles();
        this.selectedPlate = plate;
        this.selectedVehicleType = vehicleType;
        this.mainPage = null;

        setTitle("Poke Mall Parking System - Customer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Initialize All Customer Pages (no Page1, starts from Page2)
        mainPanel.add(createPage2(), "Page2");
        mainPanel.add(createPage3(), "Page3");
        mainPanel.add(createPage4(), "Page4");
        mainPanel.add(createPage5(), "Page5");
        mainPanel.add(createPage6(), "Page6");
        mainPanel.add(createPage7(), "Page7");
        mainPanel.add(createPage8(), "Page8");

        applyGlobalButtonSizing(mainPanel);

        add(mainPanel);
        
        // Start from appropriate page
        if (isExiting) {
            showCard("Page5");
        } else {
            cardLayout.show(mainPanel, "Page2");
        }
    }

    public void setMainPage(MainPage mainPage) {
        this.mainPage = mainPage;
    }

    private void showCard(String cardName) {
        if (cardName.equals("Page2")) {
            updatePage2Content();
        }

        if (cardName.equals("Page4")) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("hh:mm:ss a");
            String entryTimeStr = entryTime > 0 ? sdf.format(new java.util.Date(entryTime)) : "-";
            if (page4EntryTimeLabel != null) page4EntryTimeLabel.setText("Entry Time: " + entryTimeStr);
            if (page4SpotLabel != null) page4SpotLabel.setText("Spot: " + (selectedSpotId == null || selectedSpotId.isEmpty() ? "-" : selectedSpotId));
        }

        if (cardName.equals("Page5")) {
            // Refresh parked vehicles data from database to get latest info
            parkedVehicles = dataManager.getParkedVehicles();
            
            String norm = normalizePlate(selectedPlate);
            UIDataManager.ParkedVehicleData v = null;
            if (selectedPlate != null && parkedVehicles.containsKey(norm)) {
                v = parkedVehicles.get(norm);
            } else if (selectedPlate != null && !selectedPlate.isEmpty()) {
                // Try to find vehicle directly in database
                v = dataManager.getParkedVehicleData(norm);
            }
            
            if (v != null) {
                page5PlateLabel.setText("Plate No: " + selectedPlate);
                page5EntryTimeLabel.setText("Entry Time: " + v.entryTime);

                // Calculate and display duration
                long parkedMillis = System.currentTimeMillis() - v.entryMillis;
                if (parkedMillis < 0) parkedMillis = 0;
                long hoursRoundedUp = Math.max(1L, (parkedMillis + 60L*60*1000 - 1) / (60L*60*1000));
                String durationText = formatDuration(hoursRoundedUp);
                page5DurationLabel.setText("Duration: " + durationText);

                String parkingType = extractSpotType(v.parkingSpot);
                page5ParkingTypeLabel.setText("Parking Type: " + parkingType);
                page5SpotLabel.setText("Parking Spot: " + v.parkingSpot);

                double rate = getParkingRate(parkingType, v.vehicleType, norm);
                page5ParkingRateLabel.setText(String.format("Parking Rate: RM %.2f/hour", rate));

                double parkingFee = rate * hoursRoundedUp;
                page5ParkingFeeLabel.setText(String.format("Parking Fee: RM %.2f", parkingFee));

                // Calculate fine based on parking duration using the fine scheme that was active at entry time
                double autoCalculatedFine = dataManager.calculateFineForParkingAtEntryTime(hoursRoundedUp, v.entryMillis, v.plate);
                // Get manual fines from unpaid_fines (e.g., reserved spot violations)
                double manualFine = dataManager.getUnpaidFine(norm);
                // Display total fine (auto-calculated + manual fines)
                double totalFine = autoCalculatedFine + manualFine;
                page5FineLabel.setText("Fine (If Any): RM " + String.format("%.2f", totalFine));
            } else {
                page5PlateLabel.setText("Plate No: " + (selectedPlate == null ? "-" : selectedPlate));
                page5EntryTimeLabel.setText("Entry Time: -");
                page5DurationLabel.setText("Duration: -");
                page5ParkingTypeLabel.setText("Parking Type: -");
                page5SpotLabel.setText("Parking Spot: -");
                page5ParkingRateLabel.setText("Parking Rate: RM 0.00/hour");
                page5FineLabel.setText("Fine (If Any): RM 0.00");
                page5ParkingFeeLabel.setText("Parking Fee: RM 0.00");
            }
            page5FineSchemeLabel.setText("Current Fine Scheme: " + dataManager.getCurrentFineScheme());
        }

        if (cardName.equals("Page8")) {
            updateReceiptPage();
        }

        cardLayout.show(mainPanel, cardName);
    }

    private void updateReceiptPage() {
        String norm = normalizePlate(selectedPlate);
        String plate = (selectedPlate == null || selectedPlate.isEmpty()) ? "-" : selectedPlate;
        String type = (selectedVehicleType == null || selectedVehicleType.isEmpty()) ? "-" : selectedVehicleType;
        String spot = "-";
        String entry = "-";
        String exit = "-";
        String duration = "-";

        double parkingFee = lastParkingFee;
        double fine = lastFine;
        double remaining = fine;
        
        // Use cached receipt data if available
        if (lastReceiptData != null) {
            spot = lastReceiptData.parkingSpot != null ? lastReceiptData.parkingSpot : "-";
            entry = lastReceiptData.entryTime != null ? lastReceiptData.entryTime : "-";
            
            long usedExit = lastExitTimeMillis > 0 ? lastExitTimeMillis : System.currentTimeMillis();
            long parkedMillis = usedExit - lastReceiptData.entryMillis;
            if (parkedMillis < 0) parkedMillis = 0;
            
            exit = new java.text.SimpleDateFormat("hh:mm a").format(new java.util.Date(usedExit));
            
            long hrs = parkedMillis / (1000*60*60);
            long mins = (parkedMillis / (1000*60)) % 60;
            duration = String.format("%dh %02dm", hrs, mins);
        } else if (parkedVehicles.containsKey(norm)) {
            UIDataManager.ParkedVehicleData dv = parkedVehicles.get(norm);
            spot = dv.parkingSpot != null ? dv.parkingSpot : "-";
            entry = dv.entryTime != null ? dv.entryTime : "-";

            long usedExit = exitTimeMillis > 0 ? exitTimeMillis : System.currentTimeMillis();
            long parkedMillis = usedExit - dv.entryMillis;
            if (parkedMillis < 0) parkedMillis = 0;

            exit = new java.text.SimpleDateFormat("hh:mm a").format(new java.util.Date(usedExit));

            long hrs = parkedMillis / (1000*60*60);
            long mins = (parkedMillis / (1000*60)) % 60;
            duration = String.format("%dh %02dm", hrs, mins);

            long hoursRoundedUp = Math.max(1L, (parkedMillis + 60L*60*1000 - 1) / (60L*60*1000));
            double rate = getParkingRate(extractSpotType(spot), dv.vehicleType, norm);
            parkingFee = rate * hoursRoundedUp;

            fine = dataManager.getUnpaidFine(norm);
            remaining = fine;
        }

        String feeBreakdown = parkingFee > 0 ? String.format("Parking Fee: RM %.2f\n", parkingFee) : "Parking Fee: RM 0.00\n";
        String fineLine = String.format("Fine (If Any): RM %.2f\n", fine);
        String paidLine = String.format("Total Amount Paid: RM %.2f\n", amountPaid);
        String methodLine = String.format("Payment Method: %s\n", selectedPaymentMethod == null || selectedPaymentMethod.isEmpty() ? "-" : selectedPaymentMethod);
        String remainingLine = String.format("Remaining Balance: RM %.2f\n", remaining);

        String receipt = "*************************\n" +
                         "       RECEIPT           \n" +
                         "*************************\n" +
                         "Plate No: " + plate + "\n" +
                         "Entry Time: " + entry + "\n" +
                         "Exit Time:  " + exit + "\n" +
                         "Duration:   " + duration + "\n\n" +
                         "Fee Breakdown:\n" +
                         feeBreakdown +
                         fineLine +
                         "\n" +
                         paidLine +
                         methodLine +
                         remainingLine +
                         "\n" +
                         "Total Paid: " + String.format("RM %.2f", amountPaid) + "\n" +
                         "*************************";

        if (receiptArea != null) receiptArea.setText(receipt);
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
    
    private void updatePage2Content() {
        if (rateTextArea != null) {
            String allRatesInfo = "All Spot Types & Rates:\n" +
                "• Compact: RM 2/hour (Motorcycles)\n" +
                "• Regular: RM 5/hour (Cars, SUV/Trucks)\n" +
                "• Handicapped: RM 2/hour (FREE for card holders)\n" +
                "• Reserved: RM 10/hour (VIP)\n\n" +
                "Your Vehicle Details:\n" +
                "Plate Number: " + selectedPlate + "\n" +
                buildRateInfoForVehicle(selectedVehicleType);
            rateTextArea.setText(allRatesInfo);
            rateTextArea.setCaretPosition(0);
        }
        
        if (parkingComboBox != null) {
            String[] availableParkingTypes = getAvailableParkingTypes(selectedVehicleType);
            parkingComboBox.removeAllItems();
            for (String type : availableParkingTypes) {
                parkingComboBox.addItem(type);
            }
        }
    }

    // --- PAGE 2: Parking Rate & Vehicle Type ---
    private JPanel createPage2() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JPanel ratePanel = new JPanel(new BorderLayout());
        ratePanel.setBorder(BorderFactory.createTitledBorder("Parking Rates & Vehicle Compatibility"));
        ratePanel.setPreferredSize(new Dimension(0, 200));
        
        String allRatesInfo = "All Spot Types & Rates:\n" +
            "• Compact: RM 2/hour (Motorcycles)\n" +
            "• Regular: RM 5/hour (Cars, SUV/Trucks)\n" +
            "• Handicapped: RM 2/hour (FREE for card holders)\n" +
            "• Reserved: RM 10/hour (VIP)\n\n" +
            "Your Vehicle Details:\n" +
            "Plate Number: " + selectedPlate + "\n" +
            buildRateInfoForVehicle(selectedVehicleType);
        
        rateTextArea = new JTextArea(allRatesInfo);
        rateTextArea.setEditable(false);
        rateTextArea.setBackground(panel.getBackground());
        rateTextArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        rateTextArea.setLineWrap(true);
        rateTextArea.setWrapStyleWord(true);
        ratePanel.add(new JScrollPane(rateTextArea), BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new BorderLayout());
        
        JPanel selectionPanel = new JPanel(new GridLayout(1, 1, 10, 20));
        selectionPanel.setBorder(new EmptyBorder(20, 50, 10, 50));

        String[] availableParkingTypes = getAvailableParkingTypes(selectedVehicleType);
        parkingComboBox = new JComboBox<>(availableParkingTypes);
        parkingComboBox.setBorder(BorderFactory.createTitledBorder("Select Parking Type"));

        selectionPanel.add(parkingComboBox);
        
        JPanel availabilityPanel = new JPanel(new BorderLayout());
        availabilityPanel.setBorder(new EmptyBorder(10, 50, 20, 50));
        
        JTextArea availabilityTextArea = new JTextArea();
        availabilityTextArea.setEditable(false);
        availabilityTextArea.setBackground(panel.getBackground());
        availabilityTextArea.setFont(new Font("Monospaced", Font.BOLD, 12));
        availabilityTextArea.setBorder(BorderFactory.createTitledBorder("Available Spots Per Floor"));
        
        parkingComboBox.addActionListener(e -> {
            String selectedType = (String) parkingComboBox.getSelectedItem();
            availabilityTextArea.setText(getAvailabilitySummary(selectedType));
        });
        
        if (availableParkingTypes.length > 0) {
            availabilityTextArea.setText(getAvailabilitySummary(availableParkingTypes[0]));
        }
        
        availabilityPanel.add(availabilityTextArea, BorderLayout.CENTER);
        
        centerPanel.add(selectionPanel, BorderLayout.NORTH);
        centerPanel.add(availabilityPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton btnNext = new JButton("Next");
        btnNext.addActionListener(e -> {
            selectedParkingType = (String) parkingComboBox.getSelectedItem();
            showCard("Page3");
        });

        JButton btnBack = new JButton("Back");
        btnBack.addActionListener(e -> {
            this.setVisible(false);
            this.dispose();
            if (mainPage != null) {
                mainPage.setVisible(true);
            }
        });

        btnPanel.add(btnBack);
        btnPanel.add(btnNext);

        panel.add(ratePanel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private String buildRateInfoForVehicle(String vehicleType) {
        if (vehicleType == null || vehicleType.isEmpty()) {
            return "Please select a vehicle type on the previous page.";
        }
        
        boolean isOKU = dataManager.isOKUCardHolder(selectedPlate);
        boolean isVIP = dataManager.isVIPPlate(selectedPlate);
        String okuStatus = isOKU ? "YES - Eligible for RM 2/hour discount" : "NO";
        String discountInfo = isOKU ? "\n*** OKU Card Holder: Eligible for RM 2/hour discount on all spots ***" : "";
        String vipInfo = isVIP ? "\n*** VIP Member: Can park in Reserved spots (RM 10/hour) ***" : "";
        
        switch (vehicleType) {
            case "Motorcycle":
                return "Vehicle Type: Motorcycle\n" +
                       "OKU Card Status: " + okuStatus + "\n" +
                       "Compatible Spots: Compact\n" +
                       "Rate: RM 2/hour\n" + 
                       discountInfo + vipInfo;
            case "Car":
                return "Vehicle Type: Car\n" +
                       "OKU Card Status: " + okuStatus + "\n" +
                       "Compatible Spots: Compact, Regular" +
                       (isVIP ? ", Reserved (VIP)" : "") + "\n" +
                       "Rates: Compact (RM 2/hour), Regular (RM 5/hour)" +
                       (isVIP ? ", Reserved (RM 10/hour)" : "") +
                       discountInfo + vipInfo;
            case "SUV/Truck":
                return "Vehicle Type: SUV/Truck\n" +
                       "OKU Card Status: " + okuStatus + "\n" +
                       "Compatible Spots: Regular" +
                       (isVIP ? ", Reserved (VIP)" : "") + "\n" +
                       "Rate: RM 5/hour" +
                       (isVIP ? ", Reserved (RM 10/hour)" : "") +
                       discountInfo + vipInfo;
            case "Handicapped Vehicle":
                return "Vehicle Type: Handicapped Vehicle\n" +
                       "OKU Card Status: " + okuStatus + "\n" +
                       "Compatible Spots: Compact, Regular, Handicapped" +
                       (isVIP ? ", Reserved (VIP)" : "") + "\n" +
                       "Rates: RM 2/hour (with OKU card)\n" +
                       discountInfo + vipInfo;
            default:
                return "Please select a vehicle type.";
        }
    }

    private String[] getAvailableParkingTypes(String vehicleType) {
        if (vehicleType == null || vehicleType.isEmpty()) {
            return parkingTypes;
        }

        switch (vehicleType) {
            case "Motorcycle":
                return new String[]{"Compact"};
            case "Car":
                if (dataManager.isVIPPlate(selectedPlate)) {
                    return new String[]{"Compact", "Regular", "Reserved"};
                }
                return new String[]{"Compact", "Regular"};
            case "SUV/Truck":
                if (dataManager.isVIPPlate(selectedPlate)) {
                    return new String[]{"Regular", "Reserved"};
                }
                return new String[]{"Regular"};
            case "Handicapped Vehicle":
                if (dataManager.isVIPPlate(selectedPlate)) {
                    return parkingTypes;
                } else {
                    return new String[]{"Compact", "Regular", "Handicapped"};
                }
            default:
                return parkingTypes;
        }
    }
    
    private boolean isVIPPlate(String plate) {
        return dataManager.isVIPPlate(plate);
    }
    
    private boolean isOKUCardHolder(String plate) {
        return dataManager.isOKUCardHolder(plate);
    }

    private String normalizePlate(String plate) {
        if (plate == null) return "";
        return plate.replaceAll("\\s+", "").toUpperCase();
    }
    
    private double getParkingRate(String spotType, String vehicleType, String plate) {
        // CRITICAL: If vehicle is HANDICAPPED, has OKU card, AND parked in HANDICAPPED spot → FREE
        if ("Handicapped Vehicle".equalsIgnoreCase(vehicleType) && dataManager.isOKUCardHolder(plate) && "Handicapped".equalsIgnoreCase(spotType)) {
            return 0.0; // FREE for handicapped OKU cardholders in handicapped spots
        }
        
        // If has OKU card → RM 2 for other spots
        if (dataManager.isOKUCardHolder(plate)) {
            return 2.0;
        }

        // Standard rates for everyone else
        switch (spotType) {
            case "Compact":
                return 2.0;
            case "Regular":
                return 5.0;
            case "Handicapped":
                return 2.0;
            case "Reserved":
                return 10.0;
            default:
                return 0.0;
        }
    }

    // --- PAGE 3: Floor & Spot Selection ---
    private JPanel createPage3() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        String[] floorOptions = new String[floors.length + 1];
        floorOptions[0] = "-- Select Floor --";
        for (int i = 0; i < floors.length; i++) {
            floorOptions[i + 1] = floors[i];
        }
        JComboBox<String> floorCombo = new JComboBox<>(floorOptions);
        topPanel.add(new JLabel("Select Floor: "));
        topPanel.add(floorCombo);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        
        String[] columns = {"Spot ID", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        
        // Don't load spots until floor is selected
        floorCombo.addActionListener(e -> {
            String selected = (String) floorCombo.getSelectedItem();
            if (selected != null && !selected.equals("-- Select Floor --")) {
                updateSpotTable(model, selected);
            }
        });
        
        JLabel mapLabel = new JLabel();
        try {
            ImageIcon mapIcon = new ImageIcon("ui/parkingmap.jpeg");
            Image scaledImage = mapIcon.getImage().getScaledInstance(650, 500, Image.SCALE_SMOOTH);
            mapLabel.setIcon(new ImageIcon(scaledImage));
            mapLabel.setHorizontalAlignment(JLabel.CENTER);
        } catch (Exception ex) {
            mapLabel.setText("[Parking Map]");
            mapLabel.setHorizontalAlignment(JLabel.CENTER);
        }
        
        centerPanel.add(new JScrollPane(table));
        centerPanel.add(mapLabel);

        JPanel botPanel = new JPanel();
        JButton btnConfirm = new JButton("Park Here");
        btnConfirm.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                String spotId = (String) model.getValueAt(row, 0);
                String status = (String) model.getValueAt(row, 1);
                String spotType = extractSpotType(spotId);
                
                if (status.equals("Occupied")) {
                    JOptionPane.showMessageDialog(this, "Spot is already occupied!");
                } else if (spotType.equals("Reserved") && !dataManager.isVIPPlate(selectedPlate)) {
                    JOptionPane.showMessageDialog(this, 
                        "This Reserved spot is only available for VIP customers.",
                        "Access Denied", JOptionPane.ERROR_MESSAGE);
                } else {
                    selectedSpotId = spotId;
                    entryTime = System.currentTimeMillis();

                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("hh:mm a");
                    String entryTimeStr = sdf.format(new java.util.Date(entryTime));
                    if (selectedPlate != null && !selectedPlate.isEmpty()) {
                        String norm = normalizePlate(selectedPlate);
                        dataManager.addParkedVehicle(norm, selectedVehicleType, entryTimeStr, selectedSpotId, entryTime);
                        parkedVehicles.put(norm, new UIDataManager.ParkedVehicleData(norm, selectedVehicleType, entryTimeStr, selectedSpotId, entryTime));
                    }

                    showCard("Page4");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a spot from the table.");
            }
        });

        JButton btnBack = new JButton("Back");
        btnBack.addActionListener(e -> showCard("Page2"));

        botPanel.add(btnBack);
        botPanel.add(btnConfirm);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(botPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void updateSpotTable(DefaultTableModel model, String floor) {
        model.setRowCount(0);
        
        // Refresh parked vehicles from database for accurate spot availability
        parkedVehicles = dataManager.getParkedVehicles();
        
        int floorNum = Integer.parseInt(floor.replace("Floor ", ""));
        java.util.List<String[]> allSpots = getSpotsForFloor(floorNum);
        
        for (String[] spot : allSpots) {
            if (spot[1].equals(selectedParkingType)) {
                model.addRow(new Object[]{spot[0], spot[2]});
            }
        }
    }
    
    private java.util.Set<String> getOccupiedSpots() {
        java.util.Set<String> occupiedSpots = new java.util.HashSet<>();
        java.util.Map<String, UIDataManager.ParkedVehicleData> parkedVehicles = dataManager.getParkedVehicles();
        for (UIDataManager.ParkedVehicleData vehicle : parkedVehicles.values()) {
            if (vehicle.parkingSpot != null && !vehicle.parkingSpot.isEmpty()) {
                occupiedSpots.add(vehicle.parkingSpot);
            }
        }
        return occupiedSpots;
    }
    
    private java.util.List<String[]> getSpotsForFloor(int floorNum) {
        java.util.List<String[]> allSpots = new java.util.ArrayList<>();
        java.util.Set<String> occupiedSpots = getOccupiedSpots();
        
        if (floorNum <= 3) {
            for (int i = 1; i <= 5; i++) {
                String spotId = String.format("F%d-Reserved-R1S%02d", floorNum, i);
                String status = occupiedSpots.contains(spotId) ? "Occupied" : "Available";
                allSpots.add(new String[]{spotId, "Reserved", status});
            }
            for (int i = 6; i <= 10; i++) {
                String spotId = String.format("F%d-Handicapped-R1S%02d", floorNum, i);
                String status = occupiedSpots.contains(spotId) ? "Occupied" : "Available";
                allSpots.add(new String[]{spotId, "Handicapped", status});
            }
        } else {
            for (int i = 1; i <= 10; i++) {
                String spotId = String.format("F%d-Reserved-R1S%02d", floorNum, i);
                String status = occupiedSpots.contains(spotId) ? "Occupied" : "Available";
                allSpots.add(new String[]{spotId, "Reserved", status});
            }
        }
        
        for (int i = 11; i <= 20; i++) {
            String spotId = String.format("F%d-Compact-R2S%02d", floorNum, i);
            String status = occupiedSpots.contains(spotId) ? "Occupied" : "Available";
            allSpots.add(new String[]{spotId, "Compact", status});
        }
        
        for (int i = 21; i <= 30; i++) {
            String spotId = String.format("F%d-Regular-R3S%02d", floorNum, i);
            String status = occupiedSpots.contains(spotId) ? "Occupied" : "Available";
            allSpots.add(new String[]{spotId, "Regular", status});
        }
        
        return allSpots;
    }
    
    private String getAvailabilitySummary(String parkingType) {
        StringBuilder summary = new StringBuilder();
        summary.append("Parking Type: ").append(parkingType).append("\n\n");
        
        java.util.Set<String> occupiedSpots = getOccupiedSpots();
        
        for (int floor = 1; floor <= 5; floor++) {
            java.util.List<String[]> spots = getSpotsForFloor(floor);
            int totalSpots = 0;
            int availableSpots = 0;
            
            for (String[] spot : spots) {
                if (spot[1].equals(parkingType)) {
                    totalSpots++;
                    if (spot[2].equals("Available")) {
                        availableSpots++;
                    }
                }
            }
            
            if (totalSpots > 0) {
                summary.append(String.format("Floor %d: %d available / %d total\n", 
                    floor, availableSpots, totalSpots));
            }
        }
        
        return summary.toString();
    }

    // --- PAGE 4: Thank You ---
    private JPanel createPage4() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);

        JLabel lbl = new JLabel("Thank You! Parking Confirmed.");
        lbl.setFont(new Font("Arial", Font.BOLD, 22));
        lbl.setForeground(new Color(0, 100, 0));
        
        page4EntryTimeLabel = new JLabel("Entry Time: -");
        page4EntryTimeLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        page4EntryTimeLabel.setHorizontalAlignment(SwingConstants.CENTER);

        page4SpotLabel = new JLabel("Spot: -");
        page4SpotLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        page4SpotLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton btnHome = new JButton("Back to Home");
        btnHome.addActionListener(e -> {
            this.setVisible(false);
            this.dispose();
            if (mainPage != null) {
                mainPage.setVisible(true);
            } else {
                new MainPage(dataManager).setVisible(true);
            }
        });

        gbc.gridx = 0; gbc.gridy = 0; panel.add(lbl, gbc);
        gbc.gridy = 1; panel.add(page4EntryTimeLabel, gbc);
        gbc.gridy = 2; panel.add(page4SpotLabel, gbc);
        gbc.gridy = 3; panel.add(btnHome, gbc);

        return panel;
    }

    // --- PAGE 5: Exit Details ---
    private JPanel createPage5() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(40, 120, 40, 120));

        page5FineSchemeLabel = new JLabel("Current Fine Scheme: " + dataManager.getCurrentFineScheme());
        page5FineSchemeLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        page5FineSchemeLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel header = new JLabel("Vehicle Details (Exit)", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 20));

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        page5PlateLabel = new JLabel("Plate No: ");
        page5EntryTimeLabel = new JLabel("Entry Time: ");
        page5DurationLabel = new JLabel("Duration: -");
        page5ParkingTypeLabel = new JLabel("Parking Type: ");
        page5SpotLabel = new JLabel("Parking Spot: ");
        page5ParkingRateLabel = new JLabel("Parking Rate: ");
        page5FineLabel = new JLabel("Fine (If Any): RM 0.00");
        page5ParkingFeeLabel = new JLabel("Parking Fee: RM 0.00");

        JLabel[] items = { page5PlateLabel, page5EntryTimeLabel, page5DurationLabel, page5ParkingTypeLabel, page5SpotLabel, page5ParkingRateLabel, page5FineLabel, page5ParkingFeeLabel };
        for (JLabel lbl : items) {
            lbl.setFont(new Font("Arial", Font.PLAIN, 14));
            lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            listPanel.add(lbl);
            listPanel.add(Box.createVerticalStrut(8));
        }

        JPanel center = new JPanel(new BorderLayout());
        center.add(header, BorderLayout.NORTH);
        center.add(listPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton btnPay = new JButton("Proceed to Payment");
        btnPay.addActionListener(e -> showCard("Page6"));
        JButton btnBack = new JButton("Back");
        btnBack.addActionListener(e -> {
            this.setVisible(false);
            this.dispose();
            if (mainPage != null) {
                mainPage.setVisible(true);
            }
        });
        btnPanel.add(btnBack);
        btnPanel.add(btnPay);

        panel.add(page5FineSchemeLabel, BorderLayout.NORTH);
        panel.add(center, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    // --- PAGE 6: Payment Selection ---
    private JPanel createPage6() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel lbl = new JLabel("Pick Payment Type");
        lbl.setFont(new Font("Arial", Font.BOLD, 18));

        JComboBox<String> payCombo = new JComboBox<>(paymentTypes);

        JLabel qrLabel = new JLabel();
        qrLabel.setHorizontalAlignment(SwingConstants.CENTER);
        qrLabel.setVisible(false);
        
        try {
            ImageIcon qrIcon = new ImageIcon("ui/Qrpayment.jpeg");
            Image img = qrIcon.getImage();
            Image scaledImg = img.getScaledInstance(300, 350, Image.SCALE_SMOOTH);
            qrLabel.setIcon(new ImageIcon(scaledImg));
            qrLabel.setBorder(BorderFactory.createLineBorder(Color.PINK, 3));
        } catch (Exception e) {
            qrLabel.setText("[QR Code]");
        }
        
        payCombo.addActionListener(e -> {
            String selected = (String) payCombo.getSelectedItem();
            qrLabel.setVisible("QR".equals(selected));
            panel.revalidate();
            panel.repaint();
        });

        JButton btnPay = new JButton("Pay & Continue");
        btnPay.addActionListener(e -> {
            if (selectedPlate == null || selectedPlate.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No plate selected for payment.");
                return;
            }
            String norm = normalizePlate(selectedPlate);
            if (!parkedVehicles.containsKey(norm)) {
                JOptionPane.showMessageDialog(this, "No parking record found.");
                return;
            }

            UIDataManager.ParkedVehicleData dv = parkedVehicles.get(norm);
            long now = System.currentTimeMillis();
            long parkedMillis = now - dv.entryMillis;
            String spotType = extractSpotType(dv.parkingSpot);
            long hoursRoundedUp = Math.max(1L, (parkedMillis + 60L*60*1000 - 1) / (60L*60*1000));
            double rate = getParkingRate(spotType, dv.vehicleType, norm);
            double parkingFee = rate * hoursRoundedUp;

            // Recalculate auto fine fresh at payment time using current duration and entry time scheme
            double autoFine = dataManager.calculateFineForParkingAtEntryTime(hoursRoundedUp, dv.entryMillis, dv.plate);
            // Get manual fines (e.g., reserved spot violations) from unpaid_fines
            double manualFine = dataManager.getUnpaidFine(norm);
            // Total fine = auto-calculated + manual
            double fine = autoFine + manualFine;
            double totalDue = parkingFee + fine;

            selectedPaymentMethod = (String) payCombo.getSelectedItem();
            amountPaid = totalDue;
            exitTimeMillis = now;
            
            // Cache receipt data before removing vehicle
            lastReceiptData = dv;
            lastExitTimeMillis = now;
            lastParkingFee = parkingFee;
            lastFine = fine;

            dataManager.recordPayment(norm, amountPaid, parkingFee, fine, selectedPaymentMethod);
            dataManager.removeParkedVehicle(norm);
            parkedVehicles.remove(norm);
            finesSavedThisSession.remove(norm); // Clear the fine saved flag for this vehicle

            showCard("Page7");
        });

        JButton btnBack = new JButton("Back");
        btnBack.addActionListener(e -> showCard("Page5"));

        gbc.gridx = 0; gbc.gridy = 0; panel.add(lbl, gbc);
        gbc.gridy = 1; panel.add(payCombo, gbc);
        gbc.gridy = 2; panel.add(qrLabel, gbc);
        gbc.gridy = 3; panel.add(btnPay, gbc);
        gbc.gridy = 4; panel.add(btnBack, gbc);

        return panel;
    }

    // --- PAGE 7: Exit/Receipt Question ---
    private JPanel createPage7() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel msg = new JLabel("Thank You, Come Again!");
        msg.setFont(new Font("Arial", Font.BOLD, 22));

        JLabel q = new JLabel("Would you like a receipt?");

        JPanel btnPanel = new JPanel();
        JButton btnYes = new JButton("Yes");
        btnYes.addActionListener(e -> showCard("Page8"));
        
        JButton btnNo = new JButton("No");
        btnNo.addActionListener(e -> {
            this.setVisible(false);
            this.dispose();
            if (mainPage != null) {
                mainPage.setVisible(true);
            } else {
                new MainPage(dataManager).setVisible(true);
            }
        });

        btnPanel.add(btnYes);
        btnPanel.add(btnNo);

        gbc.gridx = 0; gbc.gridy = 0; panel.add(msg, gbc);
        gbc.gridy = 1; panel.add(q, gbc);
        gbc.gridy = 2; panel.add(btnPanel, gbc);

        return panel;
    }

    // --- PAGE 8: Receipt View ---
    private JPanel createPage8() {
        JPanel panel = new JPanel(new BorderLayout());
        
        receiptArea = new JTextArea();
        receiptArea.setEditable(false);
        receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        receiptArea.setBorder(new EmptyBorder(20, 20, 20, 20));

        JButton btnDone = new JButton("Done");
        btnDone.addActionListener(e -> {
            this.setVisible(false);
            this.dispose();
            if (mainPage != null) {
                mainPage.setVisible(true);
            } else {
                new MainPage(dataManager).setVisible(true);
            }
        });

        panel.add(new JScrollPane(receiptArea), BorderLayout.CENTER);
        panel.add(btnDone, BorderLayout.SOUTH);

        return panel;
    }

    private String extractSpotType(String spotId) {
        if (spotId == null || spotId.isEmpty()) return "";
        String[] parts = spotId.split("-");
        if (parts.length >= 2) {
            return parts[1];
        }
        return "";
    }

    /**
     * Format parking duration for display
     * @param hours Total hours parked
     * @return Formatted string like "68 hours (2 days 20 hours)"
     */
    private String formatDuration(long hours) {
        if (hours <= 0) {
            return "0 hours";
        }
        
        long days = hours / 24;
        long remainingHours = hours % 24;
        
        StringBuilder duration = new StringBuilder();
        duration.append(hours).append(" hour");
        if (hours != 1) duration.append("s");
        
        // Add breakdown if more than 24 hours
        if (hours >= 24) {
            duration.append(" (");
            if (days > 0) {
                duration.append(days).append(" day");
                if (days != 1) duration.append("s");
            }
            if (remainingHours > 0) {
                if (days > 0) duration.append(" ");
                duration.append(remainingHours).append(" hour");
                if (remainingHours != 1) duration.append("s");
            }
            duration.append(")");
        }
        
        return duration.toString();
    }
}
