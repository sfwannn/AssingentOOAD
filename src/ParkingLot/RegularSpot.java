package ParkingLot;

public class RegularSpot extends ParkingSpot {
    public RegularSpot(String spotId) {
        super(spotId, "Regular", 5.00);
    }

    public RegularSpot(int floor, int row, int spot) {
        super(ParkingSpot.formatSpotId(floor, row, spot), "Regular", 5.00);
    }
}