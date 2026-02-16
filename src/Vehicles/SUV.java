package Vehicles;

import ParkingLot.ParkingSpot;

public class SUV extends Vehicle {
    public SUV(String licensePlate) {
        super(licensePlate, VehicleType.SUV);
    }

    @Override
    public boolean canParkIn(ParkingSpot spot) {
        // SUV/Truck can park in Regular OR Reserved spots
        String type = spot.getType();
        return type.equals("Regular") || type.equals("Reserved");
    }
}