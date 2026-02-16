package ParkingLot;

import Vehicles.Vehicle;
import Vehicles.Car;
import Vehicles.SUV;
import Vehicles.Motorcycle;
import Vehicles.HandicappedVehicle;


public class ParkingLotTest {
    public static void main(String[] args) {
        System.out.println("=== 2.0 RULE VALIDATION TEST ===");
        System.out.println("Verifying: Compact/Regular restrictions + Reserved Access for All\n");

        // 1. Setup Logic
        ParkingLot lot = ParkingLot.getInstance();
        lot.initializeDefaultStructure();

        // 2. Fetch one of each spot type (Based on default 5-floor structure)
        // Floor 1 Structure: R1=Reserved(1-5)/Handicapped(6-10), R2=Compact, R3=Regular
        ParkingSpot reservedSpot = lot.getSpotById("F1-R1-S01");
        ParkingSpot handicappedSpot = lot.getSpotById("F1-R1-S06");
        ParkingSpot compactSpot = lot.getSpotById("F1-R2-S11");
        ParkingSpot regularSpot = lot.getSpotById("F1-R3-S21");

        if (reservedSpot == null || compactSpot == null) {
            System.err.println("CRITICAL: Could not find test spots. Check IDs.");
            return;
        }

        // 3. Create Fleet
        Vehicle moto = new Motorcycle("MOTO-1");
        Vehicle car = new Car("CAR-1");
        Vehicle suv = new SUV("SUV-1");
        Vehicle okUVan = new HandicappedVehicle("OKU-1");

        // ==========================================
        // TEST SUITE 1: MOTORCYCLE (Compact + Reserved ONLY)
        // ==========================================
        System.out.println("--- TESTING MOTORCYCLE RULES ---");
        testRule(moto, compactSpot, true);    // Should Pass
        testRule(moto, regularSpot, false);   // Should Fail (Too big for compact, waste of regular?)
        testRule(moto, reservedSpot, true);   // Should Pass (Clarification: All can park in Reserved)
        testRule(moto, handicappedSpot, false); // Should Fail

        // ==========================================
        // TEST SUITE 2: CAR (Compact + Regular + Reserved)
        // ==========================================
        System.out.println("\n--- TESTING CAR RULES ---");
        testRule(car, compactSpot, true);     // Should Pass
        testRule(car, regularSpot, true);     // Should Pass
        testRule(car, reservedSpot, true);    // Should Pass
        testRule(car, handicappedSpot, false); // Should Fail

        // ==========================================
        // TEST SUITE 3: SUV (Regular + Reserved ONLY)
        // ==========================================
        System.out.println("\n--- TESTING SUV RULES ---");
        testRule(suv, compactSpot, false);    // Should Fail (Too big)
        testRule(suv, regularSpot, true);     // Should Pass
        testRule(suv, reservedSpot, true);    // Should Pass
        testRule(suv, handicappedSpot, false); // Should Fail

        // ==========================================
        // TEST SUITE 4: HANDICAPPED (ALL)
        // ==========================================
        System.out.println("\n--- TESTING HANDICAPPED VEHICLE RULES ---");
        testRule(okUVan, compactSpot, true);
        testRule(okUVan, regularSpot, true);
        testRule(okUVan, reservedSpot, true);
        testRule(okUVan, handicappedSpot, true);

        System.out.println("\n=== TEST COMPLETE ===");
    }

    /**
     * Helper logic to test a park attempt and verify it matches expectation.
     * Automatically cleans up (unparks) if the park was successful.
     */
    private static void testRule(Vehicle v, ParkingSpot s, boolean expectedSuccess) {
        boolean result = s.park(v);
        
        String status = (result == expectedSuccess) ? "[PASS]" : "[FAIL]";
        String outcome = result ? "Allowed" : "Denied";
        
        System.out.printf("%s %-10s -> %-12s : %s (Expected: %s)%n", 
            status, 
            v.getType(), 
            s.getType(), 
            outcome, 
            expectedSuccess ? "Allowed" : "Denied"
        );

        // CLEANUP: If we successfully parked, we must leave so the next test can use the spot
        if (result) {
            s.removeVehicle();
        }
    }
}