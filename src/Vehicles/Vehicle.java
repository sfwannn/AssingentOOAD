package Vehicles;
// Vehicle.java
import java.time.Duration;
import java.time.LocalDateTime;

import ParkingLot.ParkingSpot;

public abstract class Vehicle {
    private final String licensePlate;
    private final VehicleType type;
    private final LocalDateTime entryTime;
    private LocalDateTime exitTime;

    // Constructor
    public Vehicle(String licensePlate, VehicleType type) {
        this.licensePlate = licensePlate;
        this.type = type;
        this.entryTime = LocalDateTime.now(); // Record entry time immediately 
        this.exitTime = null; // Null indicates still parked 
    }

    // --- Core Logic Methods ---

    // Duration rounded up to the nearest hour 
    public long calculateDuration() {
        if (exitTime == null) {
            // If vehicle hasn't exited, calculate duration up to 'now'
            return (long) Math.ceil(Duration.between(entryTime, LocalDateTime.now()).toMinutes() / 60.0);
        }
        
        long minutes = Duration.between(entryTime, exitTime).toMinutes();
        // Example: 61 mins / 60.0 = 1.01 -> ceil -> 2.0 -> 2 hours
        return (long) Math.ceil(minutes / 60.0);
    }

    public void markExit() {
        this.exitTime = LocalDateTime.now();
    }

    // Parking eligibility check
    public abstract boolean canParkIn(ParkingSpot spot);

    // --- Getters (Encapsulation) ---
    public String getLicensePlate() { return licensePlate; }
    public VehicleType getType() { return type; }
    public LocalDateTime getEntryTime() { return entryTime; }
    public LocalDateTime getExitTime() { return exitTime; }
}