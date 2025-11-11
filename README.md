# Bajaj Finserv Health - Java Assignment
## Webhook-Based SQL Problem Solver

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.9.5-blue.svg)](https://maven.apache.org/)

## 📋 Problem Statement

This Spring Boot application solves a SQL-based hiring assessment where:
1. On startup, it generates a webhook by sending user registration details
2. Receives a webhook URL and JWT access token
3. Solves an assigned SQL problem based on registration number (odd/even)
4. Submits the SQL solution to the webhook endpoint with authentication

**My Question (Question 2 - Even RegNo):**
> Calculate the number of employees who are younger than each employee, grouped by their respective departments.

## 🎯 Solution Overview

### Registration Number Logic
- **RegNo**: `PES2UG22CS496` (ends in **96** - EVEN)
- **Question**: Question 2 - Employee Age Comparison by Department

### SQL Solution

```sql
SELECT 
    e1.EMP_ID, 
    e1.FIRST_NAME, 
    e1.LAST_NAME, 
    d.DEPARTMENT_NAME, 
    COUNT(e2.EMP_ID) AS YOUNGER_EMPLOYEES_COUNT 
FROM EMPLOYEE e1 
JOIN DEPARTMENT d ON e1.DEPARTMENT = d.DEPARTMENT_ID 
LEFT JOIN EMPLOYEE e2 ON e1.DEPARTMENT = e2.DEPARTMENT 
                     AND e2.DOB > e1.DOB 
GROUP BY e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME 
ORDER BY e1.EMP_ID DESC
```

### 🔍 Why This Query Works

#### 1. **Main Employee Selection (e1)**
```sql
FROM EMPLOYEE e1 
JOIN DEPARTMENT d ON e1.DEPARTMENT = d.DEPARTMENT_ID
```
- Selects each employee (`e1`) and joins with their department
- This gives us the base set of employees we're analyzing

#### 2. **Self-Join for Age Comparison (e2)**
```sql
LEFT JOIN EMPLOYEE e2 ON e1.DEPARTMENT = e2.DEPARTMENT 
                     AND e2.DOB > e1.DOB
```
- **Self-join**: Joins the EMPLOYEE table with itself
- **Same department**: `e1.DEPARTMENT = e2.DEPARTMENT` ensures we only compare within departments
- **Younger employees**: `e2.DOB > e1.DOB` finds employees born AFTER e1 (younger)
- **LEFT JOIN**: Ensures all employees are included, even if they have no younger colleagues (count = 0)

#### 3. **Counting Younger Employees**
```sql
COUNT(e2.EMP_ID) AS YOUNGER_EMPLOYEES_COUNT
```
- Counts how many employees (`e2`) matched the join conditions
- If no match (oldest in department), COUNT returns 0

#### 4. **Grouping and Ordering**
```sql
GROUP BY e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME 
ORDER BY e1.EMP_ID DESC
```
- Groups by each unique employee to get their count
- Orders by employee ID in descending order (as required)

### 📊 Example Walkthrough

Given the data:
- **Liam Miller** (EMP_ID: 9, DOB: 1979-12-01, HR)
- **Olivia Davis** (EMP_ID: 6, DOB: 1995-04-12, HR)

For Liam Miller:
1. Find all HR employees: Liam (1979), Olivia (1995)
2. Count those born after 1979: Olivia (1995) ✓
3. Result: `YOUNGER_EMPLOYEES_COUNT = 1`

For Olivia Davis:
1. Find all HR employees: Liam (1979), Olivia (1995)
2. Count those born after 1995: None
3. Result: `YOUNGER_EMPLOYEES_COUNT = 0`

## 🏗️ Project Architecture

### 📁 Project Structure

```
bajaj/
├── src/main/java/com/bajaj/
│   ├── WebhookSolverApplication.java    # Main Spring Boot application entry point
│   ├── config/
│   │   └── WebClientConfig.java         # WebClient bean configuration for HTTP calls
│   ├── model/
│   │   ├── WebhookRequest.java          # DTO for webhook generation request
│   │   ├── WebhookResponse.java         # DTO for webhook API response
│   │   └── SolutionRequest.java         # DTO for solution submission
│   ├── service/
│   │   └── WebhookService.java          # Core business logic for API interactions
│   └── runner/
│       └── StartupRunner.java           # CommandLineRunner - executes on startup
├── src/main/resources/
│   └── application.properties           # Configuration file
├── target/
│   └── webhook-solver.jar              # Final executable JAR
├── pom.xml                             # Maven configuration
└── README.md                           # This file
```

### 🔄 Application Flow

```
[Application Starts]
        ↓
[StartupRunner executes]
        ↓
[1. Generate Webhook]
   POST: /generateWebhook/JAVA
   Body: {name, regNo, email}
        ↓
[2. Receive Response]
   webhook URL + JWT access token
        ↓
[3. Determine Question Type]
   RegNo last 2 digits: 96 (EVEN) → Question 2
        ↓
[4. Submit SQL Solution]
   POST: webhook URL
   Header: Authorization (JWT)
   Body: {finalQuery: "SELECT..."}
        ↓
[5. Receive Confirmation]
   {"success":true,"message":"Webhook processed successfully"}
        ↓
[Application Exits]
```

## 🔧 Technical Implementation

### Key Components

#### 1. **WebhookService.java**
- **generateWebhook()**: Makes POST request to generate webhook
- **submitSolution()**: Submits SQL query with JWT authentication
- **determineQuestion()**: Extracts last 2 digits from regNo to determine odd/even

#### 2. **StartupRunner.java**
- Implements `CommandLineRunner` interface
- Runs automatically after Spring Boot context initialization
- Orchestrates the entire workflow without any manual trigger

#### 3. **WebClient Configuration**
```java
@Bean
public WebClient webClient() {
    return WebClient.builder()
            .codecs(configurer -> configurer
                    .defaultCodecs()
                    .maxInMemorySize(16 * 1024 * 1024))
            .build();
}
```
- Uses reactive WebClient (Spring WebFlux) instead of RestTemplate
- Configured with increased buffer size for large responses

### Database Schema

The problem involves three tables on the server:

#### DEPARTMENT Table
```
DEPARTMENT_ID (PK) | DEPARTMENT_NAME
-------------------|----------------
1                  | HR
2                  | Finance
3                  | Engineering
4                  | Sales
5                  | Marketing
6                  | IT
```

#### EMPLOYEE Table
```
EMP_ID (PK) | FIRST_NAME | LAST_NAME | DOB        | GENDER | DEPARTMENT (FK)
------------|------------|-----------|------------|--------|----------------
1           | John       | Williams  | 1980-05-15 | Male   | 3
2           | Sarah      | Johnson   | 1990-07-20 | Female | 2
...         | ...        | ...       | ...        | ...    | ...
```

#### PAYMENTS Table
```
PAYMENT_ID (PK) | EMP_ID (FK) | AMOUNT    | PAYMENT_TIME
----------------|-------------|-----------|-------------------------
1               | 2           | 65784.00  | 2025-01-01 13:44:12.824
...             | ...         | ...       | ...
```

## 🚀 How to Build and Run

### Prerequisites
- Java 17 or higher
- Maven (optional - wrapper included)

### Build the JAR

**Using Maven Wrapper (No Maven installation needed):**
```powershell
.\mvnw.cmd clean package
```

**Or with Maven installed:**
```powershell
mvn clean package
```

The JAR file will be created at: `target/webhook-solver.jar`

### Run the Application
```powershell
java -jar target/webhook-solver.jar
```

### Expected Console Output
```
Starting Webhook Solver Application
==================================================
Registration number ends with EVEN digits (96): Solve Question 2
Question Type: QUESTION_2_EVEN
Generating webhook for user: Sameer Beedi
Webhook generated successfully
Submitting solution to webhook
SQL Query: SELECT e1.EMP_ID, e1.FIRST_NAME...
Solution submitted successfully
Response: {"success":true,"message":"Webhook processed successfully"}
==================================================
Submission Complete!
```

## 📊 SQL Query Breakdown

### Visual Representation

```
Employee Table (e1)          Employee Table (e2)
┌─────────────────┐          ┌─────────────────┐
│ EMP_ID: 9       │          │ EMP_ID: 6       │
│ NAME: Liam      │  ◄────►  │ NAME: Olivia    │
│ DOB: 1979-12-01 │   Same   │ DOB: 1995-04-12 │
│ DEPT: HR (1)    │   Dept   │ DEPT: HR (1)    │
└─────────────────┘          └─────────────────┘
      Older                      Younger
    (1979)                       (1995)
                                   ↓
                      e2.DOB > e1.DOB ✓
                                   ↓
                           COUNT(e2) = 1
```

### Step-by-Step Execution

For **Employee 9 (Liam Miller, HR, DOB: 1979-12-01)**:

1. **Join with Department**: Get department name "HR"
2. **Self-join conditions**:
   - Find employees in department 1 (HR)
   - Find employees with DOB > 1979-12-01
3. **Match found**: Olivia Davis (DOB: 1995-04-12)
4. **Count**: 1 younger employee
5. **Output**: `9 | Liam | Miller | HR | 1`

### Performance Considerations

- **LEFT JOIN**: Ensures all employees appear in results (even those with count = 0)
- **Indexed columns**: Primary keys (EMP_ID) and foreign keys (DEPARTMENT) are indexed
- **Single pass**: Query completes in one execution without subqueries
- **Complexity**: O(n²) in worst case per department, but optimized with indexes

## 🎯 Features

- ✅ **No manual triggering**: Runs automatically on startup
- ✅ **JWT Authentication**: Secure token-based authorization
- ✅ **Reactive HTTP**: Uses WebClient for non-blocking I/O
- ✅ **Comprehensive logging**: Tracks every step of execution
- ✅ **Self-join optimization**: Efficient age comparison within departments
- ✅ **Error handling**: Graceful failure with detailed error messages
- ✅ **Configuration-driven**: All settings in `application.properties`

## 📝 Configuration

All configurable values in `src/main/resources/application.properties`:

```properties
# User credentials
user.name=Sameer Beedi
user.regNo=PES2UG22CS496
user.email=pes2ug22cs496@pesu.pes.edu

# SQL Solution
sql.query=SELECT e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME...
```

## 🔍 Validation & Testing

### Server Validation
The server validates:
- ✅ Query syntax correctness
- ✅ Required columns in output
- ✅ Correct ordering (DESC by EMP_ID)
- ✅ Accurate age calculations

### Success Response
```json
{
  "success": true,
  "message": "Webhook processed successfully"
}
```

## 📦 Deliverables

- ✅ Source code with comprehensive documentation
- ✅ Executable JAR file (`webhook-solver.jar`)
- ✅ README explaining solution approach
- ✅ Git repository with clean commit history

## 🔗 Repository Links

- **GitHub Repository**: `https://github.com/Sameerbeedi/Bajaj-Finserv-Health-Java-assignment-`
- **JAR Download**: `https://github.com/Sameerbeedi/Bajaj-Finserv-Health-Java-assignment-/tree/main/target`

## 👤 Author

**Sameer Beedi**
- Registration: PES2UG22CS496
- Email: pes2ug22cs496@pesu.pes.edu

## 📄 License

This project is developed for the Bajaj Finserv Health hiring assessment.

---

**Note**: This application demonstrates proficiency in Spring Boot, REST APIs, JWT authentication, SQL query optimization, and automated task execution.
