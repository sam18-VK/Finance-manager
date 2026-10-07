# Personal Finance Management & Analytics System

A console-based Java mini project (CSC2104 OOP) that records income/expenses,
analyzes spending by category and month, and enforces per-category budget
limits — all with plain Java (no external libraries required).

## How to open in IntelliJ IDEA

1. Extract this folder somewhere on your computer.
2. Open IntelliJ IDEA → **File > New > Project from Existing Sources...**
3. Select the `finance-manager` folder, choose **"Create project from existing sources"**.
4. When IntelliJ asks about source roots, make sure `src` is marked as the
   **Sources Root** (right-click `src` → Mark Directory as → Sources Root, if
   it isn't already blue).
5. Set the Project SDK to Java 11 or newer (File > Project Structure > Project).
6. Right-click `Main.java` inside `com.finance` → **Run 'Main.main()'**.

That's it — no Maven/Gradle setup needed, it's plain `javac`-compatible source.

## Project structure

```
src/com/finance/
  TransactionType.java          enum: INCOME / EXPENSE
  Transaction.java               core model, CSV (de)serialization, validation
  Budget.java                    category -> monthly limit
  InvalidTransactionException.java   custom checked exception
  BudgetExceededException.java       custom checked exception
  FinanceManager.java             all business logic (search, totals, summaries)
  FileHandler.java                save/load transactions to a CSV file
  Main.java                       console menu / entry point
```

## Core Java concepts demonstrated

- **OOP**: encapsulated model classes (`Transaction`, `Budget`), a service
  class (`FinanceManager`) separated from I/O (`FileHandler`) and the UI (`Main`).
- **Collections**: `ArrayList` (transaction log), `HashMap` (budgets, category
  totals), `TreeMap` (chronologically-sorted monthly summaries).
- **Date-Time API**: `LocalDate` for transaction dates, `YearMonth` for
  monthly grouping, `DateTimeFormatter` for parsing/printing.
- **File Handling**: `FileWriter`/`FileReader` with try-with-resources to
  persist transactions as CSV and reload them later.
- **Exception Handling**: custom exceptions (`InvalidTransactionException`,
  `BudgetExceededException`) plus handling of `NumberFormatException` and
  `DateTimeParseException` from user input.

## Features

- Add income / expense entries with category, description, date
- View all transactions (sorted by date)
- Search/filter by type, category, keyword, and date range
- Category-wise totals for income and expense
- Month-by-month summary (income, expense, net, category breakdown)
- Set a monthly budget limit per category and see current spend vs. limit,
  with a warning the moment an expense pushes a category over budget
- Save all transactions to a CSV file and reload them later
- Remove a transaction by ID

## Suggested demo flow (for your viva/evaluation)

1. Run the app — it seeds a few sample transactions and two budgets so the
   menu isn't empty.
2. Option 2: add an expense in "Food" large enough to trigger the budget
   warning (shows exception handling live).
3. Option 5 and 6: show category-wise analysis and the monthly summary.
4. Option 9 then 10: save to `transactions.csv`, then reload it to prove
   file persistence works.
5. Option 4: demonstrate a filtered search (e.g. by category or date range).
