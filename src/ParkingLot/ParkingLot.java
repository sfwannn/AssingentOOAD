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

    public int getTotalSpotCount() {
        int total = 0;
        for (Floor floor : floors) {
            total += floor.getSpots().size();
        }
        return total;
    }

    public List<ParkingSpot> getSpotsForFloor(int floorNumber) {
        List<ParkingSpot> spots = new ArrayList<>();
        String targetId = "Floor " + floorNumber;
        for (Floor floor : floors) {
            if (targetId.equalsIgnoreCase(floor.getFloorId())) {
                spots.addAll(floor.getSpots());
                break;
            }
        }
        return spots;
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

    public ParkingSpot getSpotByUiId(String uiSpotId) {
        String internalId = ParkingSpot.toInternalSpotId(uiSpotId);
        return getSpotById(internalId);
    }

    public boolean isSpotAllowed(Vehicles.Vehicle vehicle, ParkingSpot spot, boolean isVip) {
        if (vehicle == null || spot == null) {
            return false;
        }

        String type = spot.getType();
        if ("Reserved".equalsIgnoreCase(type)) {
            if (!isVip) {
                return false;
            }
            if (vehicle instanceof Vehicles.Motorcycle) {
                return false;
            }
        }

        return vehicle.canParkIn(spot);
    }

    public List<String> getAllowedSpotTypes(Vehicles.Vehicle vehicle, boolean isVip) {
        List<String> allowed = new ArrayList<>();
        String[] orderedTypes = {"Compact", "Regular", "Handicapped", "Reserved"};

        for (String type : orderedTypes) {
            ParkingSpot sample = getSampleSpotByType(type);
            if (sample != null && isSpotAllowed(vehicle, sample, isVip)) {
                allowed.add(type);
            }
        }

        return allowed;
    }

    private ParkingSpot getSampleSpotByType(String type) {
        for (Floor floor : floors) {
            for (ParkingSpot spot : floor.getSpots()) {
                if (spot.getType().equalsIgnoreCase(type)) {
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
                    switch (row) {
                        case 1:
                            spot = posInRow <= 5 ? new ReservedSpot(f, row, s) : new HandicappedSpot(f, row, s);
                            break;
                        case 2:
                            spot = new CompactSpot(f, row, s);
                            break;
                        default:
                            spot = new RegularSpot(f, row, s);
                            break;
                    }
                } else {
                    // Floors 4-5
                    switch (row) {
                        case 1:
                            spot = new ReservedSpot(f, row, s);
                            break;
                        case 2:
                            spot = new CompactSpot(f, row, s);
                            break;
                        default:
                            spot = new RegularSpot(f, row, s);
                            break;
                    }
                }

                floor.addSpot(spot);
            }

            this.addFloor(floor);
        }
    }
}