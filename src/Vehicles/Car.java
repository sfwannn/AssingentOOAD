package Vehicles;

import ParkingLot.ParkingSpot;

public class Car extends Vehicle {
    public Car(String licensePlate) {
        super(licensePlate, VehicleType.CAR);
    }

    @Override
    public boolean canParkIn(ParkingSpot spot) {
        // Can park in Compact, Regular, OR Reserved spots
        String type = spot.getType();
        return type.equals("Compact") || type.equals("Regular") || type.equals("Reserved");
    }
}