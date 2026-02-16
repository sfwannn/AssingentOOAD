package Management;

import java.time.LocalDateTime;

public class PaymentTest {
    public static void main(String[] args) {
        Payment payment = new Payment(new FixedFine());
        LocalDateTime entry = LocalDateTime.now();
        LocalDateTime exit = entry.plusHours(2); // 2 hours

        // Handicapped vehicle with OKU card (seeded as 'OKU-1')
        double feeCompact = payment.calculateTotal(entry, exit, "Compact", false, "OKU-1", "HANDICAPPED");
        System.out.println("HANDICAPPED OKU in Compact (2h) -> expected RM 4.00, got RM " + String.format("%.2f", feeCompact));

        double feeHandicappedSpot = payment.calculateTotal(entry, exit, "Handicapped", false, "OKU-1", "HANDICAPPED");
        System.out.println("HANDICAPPED OKU in Handicapped spot (2h) -> expected RM 0.00, got RM " + String.format("%.2f", feeHandicappedSpot));
    }
}
