package Vehicles;

public final class VehicleFactory {
    private VehicleFactory() {
    }

    public static Vehicle fromDisplayType(String vehicleType, String licensePlate) {
        if (vehicleType == null) {
            return null;
        }

        switch (vehicleType) {
            case "Motorcycle":
                return new Motorcycle(licensePlate);
            case "Car":
                return new Car(licensePlate);
            case "SUV/Truck":
                return new SUV(licensePlate);
            case "Handicapped Vehicle":
                return new HandicappedVehicle(licensePlate);
            default:
                return null;
        }
    }
}
