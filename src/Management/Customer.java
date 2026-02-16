package Management;

public class Customer extends User {
    private String licensePlate;

    public Customer(String id, String name, String licensePlate) {
        super(id, name, "CUSTOMER");
        this.licensePlate = licensePlate;
    }

    public String getLicensePlate() { return licensePlate; }
}