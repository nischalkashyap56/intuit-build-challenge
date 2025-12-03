# Assignment 2: Sales Data Analysis Engine

This project implements a functional programming pipeline to process sales data from a CSV file. It was built using Java 21 to take advantage of Records and the Stream API. The application reads raw data, handles errors gracefully, cleans up missing values using statistical imputation, and then runs a series of analytics queries.

## Prerequisites
You need to have these installed on your machine:
* Java Development Kit (JDK) 21
* Maven (for dependency management and running tests)

## Project Structure
The source code is organized into a few main packages:
* `model`: Contains the `Sale` and `RawSale` records.
* `service`: Handles the parsing of CSVs, calculating stats for imputation, and the main `AnalyticsService`.
* `util`: Just a simple logger to write errors to file instead of clogging up the console.
* `exception`: Just custom exceptions for better syste m design and readability of errors
## How to Run

1. Make sure the `sales_data.csv` file is present in the `src/main/resources` folder. I included a script to generate data if you need fresh test data.
2. Open your terminal or command prompt in the project root directory.
3. Build the project to download dependencies:
   ```bash
   mvn clean install
   ```
4. Run the main class
   ```bash
   mvn exec:java -Dexec.mainClass="com.analytics.Main"
   ```
## Image of Output Screenshots 

![Screenshot 2025-12-03 at 9 58 24 PM](https://github.com/user-attachments/assets/5c879994-19db-4f22-a92a-acdf4a8ab3d7)
![Screenshot 2025-12-03 at 9 58 16 PM](https://github.com/user-attachments/assets/e2f4d60f-85b2-4f2d-bca9-c8d4fefc1dbc)
![Screenshot 2025-12-03 at 9 58 04 PM](https://github.com/user-attachments/assets/6c65e4e1-578a-4871-829e-354c96f6d845)
![Screenshot 2025-12-03 at 9 57 40 PM](https://github.com/user-attachments/assets/983566ee-d046-4c6e-86bf-f79ce3b38f17)


## Design Decisions
I used a Two-Pass Stream approach because data imputation (filling in missing values) requires global stats like Mean and Mode.

Pass 1: Scans the file to calculate statistics (Mean Price, Mode Category, etc).

Pass 2: Reads the file again to clean the data using those stats and then runs the analysis.

This ensures we don't load the whole file into memory which is better for performance on large datasets. I also used custom Exceptions to handle CSV parsing errors gracefully so one bad line doesn't stop the whole program.


## Sample Output

```
/Users/nischalkashyap/.sdkman/candidates/java/21.0.8-tem/bin/java -javaagent:/Applications/IntelliJ IDEA CE.app/Contents/lib/idea_rt.jar=64834 -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -classpath /Users/nischalkashyap/Desktop/BuildChallenge/intuit-build-challenge/assignment2/target/classes com.analytics.Main
Sales Analytics Service on CSV ---
Phase 1: Analyzing data distribution
Mean for price: 177.68
Mode for category: Beauty
Mode for region: North
Phase 2: Processing stream and imputing missing values

---- SALES ANALYTICS REPORT ----

1. Total Revenue by Region:
- West : Rs. 365,900.11
- South : Rs. 298,586.80
- North : Rs. 338,978.36
- East : Rs. 294,177.01

2. Monthly Revenue Trends:
- 2023-01 : Rs. 51,678.39
- 2023-02 : Rs. 58,109.61
- 2023-03 : Rs. 52,823.25
- 2023-04 : Rs. 44,361.44
- 2023-05 : Rs. 75,210.73
- 2023-06 : Rs. 54,972.08
- 2023-07 : Rs. 74,257.90
- 2023-08 : Rs. 65,147.74
- 2023-09 : Rs. 33,939.63
- 2023-10 : Rs. 50,753.78
- 2023-11 : Rs. 53,309.49
- 2023-12 : Rs. 59,204.21
- 2024-01 : Rs. 43,530.36
- 2024-02 : Rs. 39,432.47
- 2024-03 : Rs. 39,839.17
- 2024-04 : Rs. 54,587.80
- 2024-05 : Rs. 67,474.51
- 2024-06 : Rs. 59,281.36
- 2024-07 : Rs. 49,996.18
- 2024-08 : Rs. 66,334.28
- 2024-09 : Rs. 36,670.26
- 2024-10 : Rs. 43,876.41
- 2024-11 : Rs. 46,059.98
- 2024-12 : Rs. 76,791.26

3. Top 3 Selling Products:
- Vacuum : 569 units
- Moisturizer : 548 units
- Lipstick : 526 units

4. Payment Method Usage:
- Credit Card : 474 transactions
- Bank Transfer : 536 transactions
- PayPal : 491 transactions
- Cash : 506 transactions
- Debit Card : 493 transactions

5. Avg Unit Price by Category:
- Beauty : Rs. 41.85
- Clothing : Rs. 51.24
- Electronics : Rs. 667.42
- Books : Rs. 40.71
- Home : Rs. 92.66

6. Regional Market Share (Top category per region):
- West : Top Category is Electronics  (Rs. 281,754.56)
- South : Top Category is Electronics  (Rs. 219,957.81)
- North : Top Category is Electronics  (Rs. 245,498.79)
- East : Top Category is Electronics  (Rs. 212,897.80)

7. Transaction Value Distribution (histograms):
- Low Value (<Rs. 50) : 363 transactions
- Mid Value (Rs. 50-Rs. 150) : 872 transactions
- High Value (>Rs. 150) : 1265 transactions

8. Day-of-Week Profitability (heatmap):
- MONDAY : Avg Order Value Rs. 478.16
- TUESDAY : Avg Order Value Rs. 592.14
- WEDNESDAY : Avg Order Value Rs. 520.14
- THURSDAY : Avg Order Value Rs. 555.56
- FRIDAY : Avg Order Value Rs. 498.48
- SATURDAY : Avg Order Value Rs. 524.55
- SUNDAY : Avg Order Value Rs. 464.36 

Processing complete! For more detailed logs on ingestion errors and cleaning, check 'DataIngestionErrors.log' and 'DataCleaning.log' files

Process finished with exit code 0
```

## Logs
The program generates two log files in the root directory if it finds issues:
- DataIngestionErrors.log: Tracks malformed CSV lines.
- DataCleaning.log: Tracks whenever a missing value was replaced (imputed) with the mean or mode.

## Testing
I wrote unit tests using JUnit 5. You can run them using:

```bash
mvn test
```
