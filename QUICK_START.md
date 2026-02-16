# Parking System - Quick Start Guide

## What Was Changed

### ✅ Project Structure Refactored
- **Customer UI**: `ui/CustomerUI.java` - Handles customer parking interaction
- **Admin UI**: `ui/AdminUI.java` - Handles administrative functions  
- **Data Manager**: `ui/UIDataManager.java` - Shared data between both UIs
- **Database Initializer**: `src/DataInitializer.java` - Loads dummy test data
- **Main Entry Point**: `src/Main.java` - Start the application here

### ✅ Removed
- ❌ `ParkingSystemUI.java` (old monolithic class - **DELETED**)
- ❌ All hardcoded dummy data from UI classes
- ❌ All hardcoded VIP/OKU plate lists

### ✅ Added Database Layer
- Dummy data now loaded from MySQL database tables
- `parked_vehicles` table
- `vip_plates` table  
- `oku_card_holders` table
- `unpaid_fines` table

## How to Run (XAMPP MySQL Setup)

### Step 1: Setup XAMPP MySQL
1. Start XAMPP Control Panel and click **Start** on MySQL
2. Download MySQL JDBC Driver from https://dev.mysql.com/downloads/connector/j/
3. Extract and copy the JAR file (e.g., `mysql-connector-j-8.0.33.jar`) to the **lib** folder
4. See [XAMPP_SETUP.md](XAMPP_SETUP.md) for detailed instructions

### Step 2: Compile the Project
```bash
# Using the batch file (recommended):
compile.bat

# OR manually:
cd c:\Users\Riana\Downloads\OOADFINAL\OOAD_FINAL
javac -d bin -cp "lib\*" ^
    "src\*.java" ^
    "src\Database\*.java" ^
    "src\Management\*.java" ^
    "src\ParkingLot\*.java" ^
    "src\Vehicles\*.java" ^
    "ui\*.java"
```

### Step 3: Run the Application
```bash
# Using the batch file (recommended):
run.bat

# OR manually:
java -cp "lib\*;bin" Main
```

**Database will auto-create on first run!**

## User Flow

### For Customers
1. **Application starts** → CustomerUI appears (Page 1)
2. **Enter plate number** and vehicle type
3. **Browse parking rates** and select spot type
4. **Choose floor and parking spot**
5. **Confirm parking** and receive entry details
6. **Exit vehicle** and process payment
7. **View receipt** and return home

### For Administrators  
1. **From Page 1 → Click "Admin"** button
2. **Login** to access admin dashboard
3. **Available Admin Functions**:
   - View parking occupancy dashboard
   - Manage parked vehicles and issue fines
   - Add/remove VIP and OKU registered plates
   - Switch between 3 fine schemes
   - Generate reports
4. **Return to Customer** by clicking Logout

## Key Features (All Preserved)

✅ **Three Fine Schemes**:
- Option A: Fixed RM 50 fine
- Option B: Progressive fines (RM 150/300/500)
- Option C: Hourly RM 20/hour fines

✅ **Parking Types**:
- Compact (RM 2/hour)
- Regular (RM 5/hour)
- Handicapped (RM 2/hour)
- Reserved VIP (RM 10/hour)

✅ **Special Features**:
- OKU card holders get RM 2/hour discount on all spots
- VIP plates can access Reserved spots
- Dynamic pricing based on vehicle & spot type
- Unpaid fines carry over between sessions

## Test Data Available

When you first run the application, dummy data is automatically loaded:

**Parked Vehicles**:
- ABC1234 (Car) - 2 hours parked
- MYS001 (Motorcycle) - 1.5 hours parked
- KKL555 (SUV/Truck) - 3 hours parked
- HAND99 (Handicapped) - 5 hours parked

**VIP Plates**:
- VIP001
- VIP002
- EXEC99

**OKU Card Holders**:
- OKU001
- OKU002
- HAND99

## Important Notes

⚠️ **All Business Logic Preserved**:
- No calculation methods changed
- No UI flow changed
- No fine scheme logic changed
- Only code organization and data storage changed

📝 **Files Organization**:
- Entry point: `Main.java`
- Customer UI: `CustomerUI.java` (in ui/ folder)
- Admin UI: `AdminUI.java` (in ui/ folder)
- Shared Data: `UIDataManager.java` (in ui/ folder)
- DB Setup: `DataInitializer.java` (in src/ folder)

## Troubleshooting

**Database Connection Failed?**
- Check MySQL is running on localhost:3306 (XAMPP Control Panel)
- Verify credentials in `Database.java` (root / empty password for XAMPP)
- Ensure `parking_lot` database exists (auto-created on first run)
- Verify MySQL JDBC driver JAR is in the `lib/` folder

**Import Errors?**
- Ensure MySQL JDBC driver is in `lib/` folder
- Check all source files are compiled together
- Re-run `compile.bat` after adding the driver

**UI Not Launching?**
- Make sure Java SE is properly installed
- Ensure XAMPP MySQL is running
- Check compilation was successful (no errors)
- Try running: `java -cp "lib\*;bin" Main`

**"Class.forName('com.mysql.cj.jdbc.Driver') failed"**
- MySQL JDBC driver not found in lib/ folder
- Download from: https://dev.mysql.com/downloads/connector/j/
- Extract and copy `.jar` file to `lib/` folder
- Recompile the project

## Summary of Changes

This refactoring improves code maintainability by:
1. **Separating concerns** - Customer and Admin features are now independent
2. **Eliminating duplication** - Shared logic is in UIDataManager
3. **Database integration** - Dummy data is loaded from MySQL, not hardcoded
4. **Cleaner initialization** - Single Main.java entry point orchestrates everything
5. **Easier testing** - Can swap test data sources or add real database queries

All original functionality is 100% preserved. The application behaves exactly the same from the user's perspective.
