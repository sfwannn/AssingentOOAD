package ParkingLot;

public class StructureVerifier {
    public static void main(String[] args) {
        System.out.println("=== PARKING LOT STRUCTURE AUDIT ===\n");

        // 1. Initialize and Build
        ParkingLot lot = ParkingLot.getInstance();
        lot.initializeDefaultStructure(); // Checks the logic you just wrote

        // 2. Verify Floor 1 (The Mixed Floor)
        System.out.println("--- Checking Floor 1 (Mixed Types) ---");
        verifySpot(lot, "F1-R1-S01", "Reserved");      // Start of Row 1
        verifySpot(lot, "F1-R1-S05", "Reserved");      // End of Reserved section
        verifySpot(lot, "F1-R1-S06", "Handicapped");   // Start of Handicapped section (Crucial Check!)
        verifySpot(lot, "F1-R2-S14", "Compact");   // End of Row 1
        verifySpot(lot, "F1-R2-S11", "Compact");       // Start of Row 2
        verifySpot(lot, "F1-R3-S27", "Regular");       // Start of Row 3

        // 3. Verify Floor 4 (The VIP Floor)
        System.out.println("\n--- Checking Floor 4 (VIP Heavy) ---");
        verifySpot(lot, "F4-R1-S01", "Reserved");      // Start of Row 1
        verifySpot(lot, "F4-R1-S06", "Reserved");      // Should be RESERVED here, not Handicapped!
        verifySpot(lot, "F4-R2-S11", "Compact");       // Row 2 is still Compact
        
        System.out.println("\n=== AUDIT COMPLETE ===");
    }

    // Helper to print Pass/Fail
    private static void verifySpot(ParkingLot lot, String id, String expected) {
        ParkingSpot spot = lot.getSpotById(id);
        if (spot != null) {
            String actual = spot.getType();
            if (actual.equalsIgnoreCase(expected)) {
                System.out.println("[PASS] " + id + ": " + actual);
            } else {
                System.out.println("[FAIL] " + id + ": FOUND " + actual + " (Expected: " + expected + ")");
            }
        } else {
            System.out.println("[FAIL] " + id + ": SPOT NOT FOUND (Check ID format)");
        }
    }
}