package ParkingLot;

import java.util.ArrayList;
import java.util.List;

public class Floor {
    private final String floorId; // e.g., "Floor 1"
    private final List<ParkingSpot> spots;

    public Floor(String floorId) {
        this.floorId = floorId;
        this.spots = new ArrayList<>();
    }

    public void addSpot(ParkingSpot spot) {
        spots.add(spot);
    }

    public List<ParkingSpot> getSpots() {
        return spots;
    }
    
    public String getFloorId() {
        return floorId;
    }

    // Helper to find available spots on this floor
    public List<ParkingSpot> getAvailableSpots() {
        List<ParkingSpot> available = new ArrayList<>();
        for (ParkingSpot spot : spots) {
            if (!spot.isOccupied()) {
                available.add(spot);
            }
        }
        return available;
    }
}