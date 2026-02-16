package Management;

public class Admin extends User {
    public Admin(String id, String name, String password) {
        super(id, name, "ADMIN");
    }

    // Admin Feature: Change Fine Strategy
    public void setFineScheme(Fine fineStrategy) {
        System.out.println("System Fine Scheme Updated to: " + fineStrategy.getClass().getSimpleName());
        // In a real app, you might save this preference to a file/DB
    }
}