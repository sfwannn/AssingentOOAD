package ParkingLot;

public class CompactSpot extends ParkingSpot {
    public CompactSpot(String spotId) {
        super(spotId, "Compact", 2.00);
    }

    public CompactSpot(int floor, int row, int spot) {
        super(ParkingSpot.formatSpotId(floor, row, spot), "Compact", 2.00);
    }
}