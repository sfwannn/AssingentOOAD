package Management;

public class ProgressiveFine implements Fine {
    @Override
    public double calculateFine(long hoursParked) {
        double fine = 0.0;
        
        // Progressive Fine Scheme (as per requirements):
        // First 24 hours: RM 50
        // Hours 24-48: Additional RM 100
        // Hours 48-72: Additional RM 150
        // Above 72 hours: Additional RM 200
        
        if (hoursParked > 24) fine += 50.0;   // First 24 hours overstay: RM 50
        if (hoursParked > 48) fine += 100.0;  // Hours 24-48: Additional RM 100
        if (hoursParked > 48) fine += 150.0;  // Hours 48-72: Additional RM 150
        if (hoursParked > 72) fine += 200.0;  // Above 72 hours: Additional RM 200
        
        return fine;
    }
}