package ParkingLot;

import Vehicles.Vehicle;

public abstract class ParkingSpot {
    private final String spotId;
    private final String type;
    private boolean isOccupied;
    private Vehicle currentVehicle; 
    private final double hourlyRate;  

    public ParkingSpot(String spotId, String type, double hourlyRate) {
        this.spotId = spotId;
        this.type = type;
        this.hourlyRate = hourlyRate;
        this.isOccupied = false;
        this.currentVehicle = null;
    }

    public boolean park(Vehicle vehicle) {
        // 1. Check if spot is already taken
        if (isOccupied) {
            return false;
        }

        // 2. Check if this specific vehicle is ALLOWED here
        if (!vehicle.canParkIn(this)) {
            System.out.println("Validation Failed: " + vehicle.getType() + " cannot park in " + this.getType());
            return false;
        }

        // 3. Park the vehicle
        this.currentVehicle = vehicle;
        this.isOccupied = true;
        return true;
    }

    public void removeVehicle() {
        this.currentVehicle = null;
        this.isOccupied = false;
    }

    // Getters
    public String getSpotId() { return spotId; }
    public String getType() { return type; }
    public boolean isOccupied() { return isOccupied; }
    public Vehicle getCurrentVehicle() { return currentVehicle; }
    public double getHourlyRate() { return hourlyRate; }

    // Utility
    public static String formatSpotId(int floor, int row, int spot) {
        return String.format("F%d-R%d-S%02d", floor, row, spot);
    }
}