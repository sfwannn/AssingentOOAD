package ParkingLot;

public class ReservedSpot extends ParkingSpot {
    public ReservedSpot(String spotId) {
        super(spotId, "Reserved", 10.00);
    }

    public ReservedSpot(int floor, int row, int spot) {
        super(ParkingSpot.formatSpotId(floor, row, spot), "Reserved", 10.00);
    }
}