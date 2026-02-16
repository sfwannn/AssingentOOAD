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

    public String toUiSpotId() {
        return toUiSpotId(this.spotId, this.type);
    }

    public static String toUiSpotId(String spotId, String type) {
        SpotIdParts parts = parseSpotId(spotId, type);
        if (parts == null) {
            return spotId;
        }
        return String.format("F%d-%s-R%dS%02d", parts.floor, parts.type, parts.row, parts.spot);
    }

    public static String toInternalSpotId(String spotId) {
        SpotIdParts parts = parseSpotId(spotId, null);
        if (parts == null) {
            return spotId;
        }
        return String.format("F%d-R%d-S%02d", parts.floor, parts.row, parts.spot);
    }

    public static String extractType(String spotId) {
        SpotIdParts parts = parseSpotId(spotId, null);
        return parts == null ? "" : parts.type;
    }

    public static int extractFloor(String spotId) {
        SpotIdParts parts = parseSpotId(spotId, null);
        return parts == null ? 1 : parts.floor;
    }

    private static SpotIdParts parseSpotId(String spotId, String fallbackType) {
        if (spotId == null || spotId.isEmpty()) {
            return null;
        }

        // UI format: F1-Reserved-R1S01
        java.util.regex.Pattern uiPattern = java.util.regex.Pattern.compile("F(\\d+)-([A-Za-z]+)-R(\\d+)S(\\d+)");
        java.util.regex.Matcher uiMatcher = uiPattern.matcher(spotId);
        if (uiMatcher.matches()) {
            int floor = Integer.parseInt(uiMatcher.group(1));
            String type = uiMatcher.group(2);
            int row = Integer.parseInt(uiMatcher.group(3));
            int spot = Integer.parseInt(uiMatcher.group(4));
            return new SpotIdParts(floor, row, spot, type);
        }

        // Internal format: F1-R1-S01
        java.util.regex.Pattern internalPattern = java.util.regex.Pattern.compile("F(\\d+)-R(\\d+)-S(\\d+)");
        java.util.regex.Matcher internalMatcher = internalPattern.matcher(spotId);
        if (internalMatcher.matches()) {
            int floor = Integer.parseInt(internalMatcher.group(1));
            int row = Integer.parseInt(internalMatcher.group(2));
            int spot = Integer.parseInt(internalMatcher.group(3));
            String type = fallbackType != null ? fallbackType : "";
            return new SpotIdParts(floor, row, spot, type);
        }

        return null;
    }

    private static final class SpotIdParts {
        private final int floor;
        private final int row;
        private final int spot;
        private final String type;

        private SpotIdParts(int floor, int row, int spot, String type) {
            this.floor = floor;
            this.row = row;
            this.spot = spot;
            this.type = type == null ? "" : type;
        }
    }
}