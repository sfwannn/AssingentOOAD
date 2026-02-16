# XAMPP MySQL Setup Guide

## Prerequisites

1. **XAMPP Installed** - Download from https://www.apachefriends.org/
2. **MySQL JDBC Driver** - Required for Java to connect to MySQL

## Step 1: Start XAMPP MySQL Server

1. Open **XAMPP Control Panel**
2. Click **Start** next to "MySQL"
3. You should see: `MySQL running on port 3306`

## Step 2: Download MySQL JDBC Driver

1. Visit: https://dev.mysql.com/downloads/connector/j/
2. Download **Platform Independent (zip)** - Latest version (8.0+)
3. Extract the ZIP file
4. Find the file: `mysql-connector-java-X.X.X.jar` (or `mysql-connector-j-X.X.X.jar`)

## Step 3: Add Driver to Project

1. Copy the JAR file to your project's **lib** folder:
   ```
   c:\Users\Riana\Downloads\OOADFINAL\OOAD_FINAL\lib\
   ```
   
2. Paste the JAR file there (e.g., `mysql-connector-j-8.0.33.jar`)

## Step 4: Verify XAMPP MySQL

Open Command Prompt and test:
```bash
mysql -u root
```

You should see the MySQL prompt (no password required for XAMPP default).

## Step 5: Compile and Run

```bash
cd c:\Users\Riana\Downloads\OOADFINAL\OOAD_FINAL
javac -d bin -cp "lib\*" ^
    "src\*.java" ^
    "src\Database\*.java" ^
    "src\Management\*.java" ^
    "src\ParkingLot\*.java" ^
    "src\Vehicles\*.java" ^
    "ui\*.java"

java -cp "lib\*;bin" Main
```

Or simply use the batch files:
```bash
run.bat
```

## Database Configuration

- **Host**: localhost
- **Port**: 3306
- **Username**: root
- **Password**: (empty - XAMPP default)
- **Database**: parking_lot (auto-created on first run)

## Troubleshooting

### Error: "Class.forName('com.mysql.cj.jdbc.Driver') failed"
- MySQL JDBC driver not in lib folder
- Ensure the JAR is in: `lib\mysql-connector-j-*.jar`

### Error: "Connection refused"
- XAMPP MySQL not running
- Open XAMPP Control Panel and click Start on MySQL

### Error: "Access denied for user 'root'"
- Check username/password in `Database.java`
- Default XAMPP: user=`root`, password=`(empty)`

## Custom MySQL Credentials

If your XAMPP MySQL has a different password:
1. Edit `src\Database\Database.java` (line 11)
2. Change: `private static final String PASSWORD = "your_password";`
3. Recompile and run
