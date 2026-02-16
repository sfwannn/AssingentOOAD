package ParkingLot;

import java.util.ArrayList;
import java.util.List;

public class ParkingLot {
    // Singleton Instance
    private static ParkingLot instance;
    
    private final List<Floor> floors;

    // Private Constructor for Singleton
    private ParkingLot() {
        this.floors = new ArrayList<>();
    }

    // Global Access Point
    public static synchronized ParkingLot getInstance() {
        if (instance == null) {
            instance = new ParkingLot();
        }
        return instance;
    }

    public void addFloor(Floor floor) {
        floors.add(floor);
    }

    public List<Floor> getFloors() {
        return floors;
    }
    
    // Helper to get a specific spot by ID
    public ParkingSpot getSpotById(String spotId) {
        for (Floor floor : floors) {
            for (ParkingSpot spot : floor.getSpots()) {
                if (spot.getSpotId().equals(spotId)) {
                    return spot;
                }
            }
        }
        return null;
    }
    
    // Helper: Reset the lot (useful for Admin config)
    public void clearLot() {
        floors.clear();
    }

    // Initialize default 5-floor structure as specified
    public void initializeDefaultStructure() {
        // Clear existing floors
        this.floors.clear();

        // Create 5 floors
        for (int f = 1; f <= 5; f++) {
            Floor floor = new Floor("Floor " + f);

            // Create 30 spots per floor
            for (int s = 1; s <= 30; s++) {
                int row = ((s - 1) / 10) + 1; // 1..3
                int posInRow = ((s - 1) % 10) + 1; // 1..10
                ParkingSpot spot;

                if (f <= 3) {
                    // Floors 1-3
                    spot = switch (row) {
                        case 1 -> posInRow <= 5 ? new ReservedSpot(f, row, s) : new HandicappedSpot(f, row, s);
                        case 2 -> new CompactSpot(f, row, s);
                        default -> new RegularSpot(f, row, s);
                    };
                } else {
                    // Floors 4-5
                    spot = switch (row) {
                        case 1 -> new ReservedSpot(f, row, s);
                        case 2 -> new CompactSpot(f, row, s);
                        default -> new RegularSpot(f, row, s);
                    };
                }

                floor.addSpot(spot);
            }

            this.addFloor(floor);
        }
    }
}