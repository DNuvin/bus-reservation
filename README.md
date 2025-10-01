# 🚌 Bus Reservation System

A simple Servlet-based Bus Reservation System deployed as a WAR project on Apache Tomcat 11. The system manages reservations for a 40-seat bus traveling daily between stops A, B, C, and D.

---

## Assumptions and Requirements

1. **Bus Route and Schedule**  
   The bus operates a single round-trip per day:
    - **Outbound:** A → D (stops at B and C)
    - **Return:** D → A (stops at C and B)

2. **Seats and Layout**
    - 40 seats arranged in 10 rows × 4 seats per row (1A, 1B, …, 10D)
    - Each seat is unique and can be booked only once per direction per date

3. **Direction Handling**
    - Two journey directions exist:
        - **OUTBOUND:** A → D
        - **RETURN:** D → A
    - Seat bookings are **direction-specific**: seats reserved for OUTBOUND do not block RETURN trips
    - `JourneyDirection` enum is used in code to store and manage direction

4. **Seat Reservation Rules**
    - A seat can be booked only if it is **completely free** for the requested date and direction
    - Partial segment bookings block the seat for the **entire direction**
        - Example: Seat 1A booked for A → B cannot be booked for A → C or B → C on the same date
    - Seat availability checks return only fully free seats
    - Reservations are **atomic**: if any seat is unavailable, the entire request fails

5. **Pricing**  
   Ticket prices are fixed per segment:
    - A → B: Rs. 50
    - A → C: Rs. 100
    - A → D: Rs. 150
    - B → C: Rs. 50
    - B → D: Rs. 100
    - C → D: Rs. 50
    - Return journeys follow the same pricing rules
    - **Total price = price per seat × number of seats**

6. **Reservation Handling**
    - Reservations reserve only **available seats**
    - Requested seats already booked are excluded from available seat list
    - **Synchronized blocks** prevent race conditions during simultaneous reservations
    - Queue processor can handle asynchronous requests with callbacks for success or failure
    - `Status` field tracks reservation state: HELD, CONFIRMED, CANCELLED

7. **Simplifications**
    - No partial refunds or cancellations are handled
    - Seat selection is **first-come-first-serve**
    - No external frameworks (like Spring) are used; only servlets
    - All operations handle **transactional integrity**, and **Serializable isolation** prevents double booking

---

## Prerequisites

- Java JDK 17+ installed and configured in PATH
- Apache Tomcat 11.x installed  
  [Download Tomcat 11](https://tomcat.apache.org/download-11.cgi)
- Maven 3.8+ installed

---

## Build Instructions

1. Open terminal/command prompt.
2. Navigate to the project root where `pom.xml` is located:
3. Build the WAR file using Maven:
```
mvn clean package  
```
5. The WAR file will be generated at:
```
bus-reservation/target/bus-reservation.war
```

---

## Deployment on Apache Tomcat 11

1. Copy the WAR file to Tomcat’s `webapps` folder:
```
cp target/bus-reservation.war $TOMCAT_HOME/webapps/
```
Replace $TOMCAT_HOME with the path to your Tomcat installation directory.

2. Navigate to Tomcat `bin` directory:

cd /path/to/tomcat/bin


3. Start Tomcat:
- On Linux/macOS:
  ```
  ./startup.sh
  ```
- On Windows:
  ```
  startup.bat
  ```

4. Tomcat will deploy the WAR automatically.

5. Access the application in your browser:


6. To stop Tomcat:
- On Linux/macOS:
  ```
  ./shutdown.sh
  ```
- On Windows:
  ```
  shutdown.bat
  ```

---


---

## Configuration Details

- **Server:** localhost
- **Port:** 8080
- **Context Path:** bus-reservation

### H2 Database Settings (Embedded)

**H2 Console (JDBC Connection for application use)**

- **URL:** [http://localhost:8082](http://localhost:8082)
- **Driver Class:** `org.h2.Driver`
- **JDBC URL:** `jdbc:h2:mem:busdb`
- **Username:** `sa`
- **Password:** *(leave blank)*

> ⚠️ Note: The H2 in-memory database resets on application shutdown, so all data will be lost when the app stops. Use the console for inspection or testing only while the application is running.

---

## Swagger UI for API testing can be accessed at:

http://localhost:8080/bus-reservation/swagger-ui


---

## License

This project is provided as-is for demonstration purposes.

---

For any issues or contributions, please open an issue or pull request in the repository.
