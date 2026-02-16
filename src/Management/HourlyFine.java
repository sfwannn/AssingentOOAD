package Management;

public class HourlyFine implements Fine {
    @Override
    public double calculateFine(long hoursParked) {
        if (hoursParked > 24) {
            long overdueHours = hoursParked - 24;
            return overdueHours * 20.0;
        }
        return 0.0;
    }
}