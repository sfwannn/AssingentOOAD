package ParkingLot;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Ticket {
    private final String ticketId;
    private final String licensePlate;
    private final String spotId;
    private final LocalDateTime entryTime;

    public Ticket(String licensePlate, String spotId) {
        this.licensePlate = licensePlate;
        this.spotId = spotId;
        this.entryTime = LocalDateTime.now();
        // Recommended format: T-PLATE-TIMESTAMP
        this.ticketId = "T-" + licensePlate + "-" + entryTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
    }

    public String getTicketId() { return ticketId; }
    public String getLicensePlate() { return licensePlate; }
    public String getSpotId() { return spotId; }
    public LocalDateTime getEntryTime() { return entryTime; }

    @Override
    public String toString() {
        return "Ticket ID: " + ticketId + " | Spot: " + spotId + " | Entry: " + entryTime;
    }
}