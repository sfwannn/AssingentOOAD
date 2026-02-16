package Management;

public class ProgressiveFine implements Fine {
    @Override
    public double calculateFine(long hoursParked) {
        double fine = 0.0;
        
        if (hoursParked > 24) fine += 50.0;  // First 24h overstay
        if (hoursParked > 48) fine += 100.0; // 2nd Day
        if (hoursParked > 72) fine += 150.0; // 3rd Day and beyond
        
        return fine;
    }
}