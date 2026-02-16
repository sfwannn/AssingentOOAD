package Management;

public class FixedFine implements Fine {
    @Override
    public double calculateFine(long hoursParked) {
        if (hoursParked > 24) {
            return 50.0;
        }
        return 0.0;
    }
}