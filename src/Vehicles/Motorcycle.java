package Vehicles;

import ParkingLot.ParkingSpot;

public class Motorcycle extends Vehicle {
    public Motorcycle(String licensePlate) {
        super(licensePlate, VehicleType.MOTORCYCLE);
    }

    // polymorphism
    @Override
    public boolean canParkIn(ParkingSpot spot) {
        // Can park in Compact spots OR Reserved spots
        String type = spot.getType();
        return type.equals("Compact") || type.equals("Reserved");
    }
}