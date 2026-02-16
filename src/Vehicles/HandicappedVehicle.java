package Vehicles;

import ParkingLot.ParkingSpot;

public class HandicappedVehicle extends Vehicle {
    public HandicappedVehicle(String licensePlate) {
        super(licensePlate, VehicleType.HANDICAPPED);
    }

    @Override
    public boolean canParkIn(ParkingSpot spot) {
        // Handicapped vehicles can park in ANY spot (including Reserved/Handicapped)
        return true;
    }
}