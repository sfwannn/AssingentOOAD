package ParkingLot;

public class HandicappedSpot extends ParkingSpot {
    public HandicappedSpot(String spotId) {
        super(spotId, "Handicapped", 2.00);
    }

    public HandicappedSpot(int floor, int row, int spot) {
        super(ParkingSpot.formatSpotId(floor, row, spot), "Handicapped", 2.00);
    }
}