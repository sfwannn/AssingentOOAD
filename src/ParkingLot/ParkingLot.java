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
                
                ParkingSpot spot = createSpotForLocation(f, row, posInRow, s);
                floor.addSpot(spot);
            }

            this.addFloor(floor);
        }
    }

    private ParkingSpot createSpotForLocation(int floor, int row, int posInRow, int spotNumber) {
        if (floor <= 3) {
            // Floors 1-3
            if (row == 1) {
                if (posInRow <= 5) {
                    return new ReservedSpot(floor, row, spotNumber);
                } else {
                    return new HandicappedSpot(floor, row, spotNumber);
                }
            } else if (row == 2) {
                return new CompactSpot(floor, row, spotNumber);
            } else {
                return new RegularSpot(floor, row, spotNumber);
            }
        } else {
            // Floors 4-5
            if (row == 1) {
                return new ReservedSpot(floor, row, spotNumber);
            } else if (row == 2) {
                return new CompactSpot(floor, row, spotNumber);
            } else {
                return new RegularSpot(floor, row, spotNumber);
            }
        }
    }
}