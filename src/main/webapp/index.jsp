<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Bus Reservation Service</title>
    <style>
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background-color: #f4f4f9;
            color: #333;
            margin: 0;
            padding: 0;
        }
        header {
            background-color: #4CAF50;
            color: white;
            padding: 20px 0;
            text-align: center;
        }
        main {
            max-width: 900px;
            margin: 30px auto;
            padding: 20px;
            background-color: white;
            border-radius: 10px;
            box-shadow: 0px 0px 15px rgba(0,0,0,0.1);
        }
        h2 {
            color: #4CAF50;
        }
        ul {
            list-style: none;
            padding: 0;
        }
        li {
            margin: 10px 0;
            padding: 10px;
        }
        a.swagger-link {
            display: inline-block;
            margin: 15px 0;
            color: white;
            background-color: #4CAF50;
            padding: 10px 15px;
            border-radius: 5px;
            text-decoration: none;
        }
        a.swagger-link:hover {
            background-color: #45a049;
        }
    </style>
</head>
<body>
    <header>
        <h1>Bus Reservation Service</h1>
        <p>Simple Servlet-based system for bus reservations</p>
    </header>
    <main>
        <a class="swagger-link" href="http://localhost:8080/bus-reservation/swagger-ui" target="_blank">
            Open Swagger UI
        </a>

        <h2>Assumptions and Requirements</h2>
        <ul>
            <li><strong>Bus Route and Schedule:</strong> Single round-trip per day
                <ul>
                    <li>Outbound: A → D (stops at B and C)</li>
                    <li>Return: D → A (stops at C and B)</li>
                </ul>
            </li>
            <li><strong>Seats and Layout:</strong>
                <ul>
                    <li>40 seats arranged in 10 rows × 4 seats per row (1A, 1B, …, 10D)</li>
                    <li>Each seat is unique and can be booked only once per direction per date</li>
                </ul>
            </li>
            <li><strong>Direction Handling:</strong>
                <ul>
                    <li>Two journey directions: OUTBOUND (A → D), RETURN (D → A)</li>
                    <li>Seat bookings are direction-specific</li>
                    <li>`JourneyDirection` enum is used in code</li>
                </ul>
            </li>
            <li><strong>Seat Reservation Rules:</strong>
                <ul>
                    <li>Seats can be booked only if completely free for requested date and direction</li>
                    <li>Partial segment bookings block the seat for the entire direction</li>
                    <li>Seat availability checks return only fully free seats</li>
                    <li>Reservations are atomic: if any seat is unavailable, the entire request fails</li>
                </ul>
            </li>
            <li><strong>Pricing:</strong>
                <ul>
                    <li>A → B: Rs. 50</li>
                    <li>A → C: Rs. 100</li>
                    <li>A → D: Rs. 150</li>
                    <li>B → C: Rs. 50</li>
                    <li>B → D: Rs. 100</li>
                    <li>C → D: Rs. 50</li>
                    <li>Return journeys follow same pricing rules</li>
                    <li>Total price = price per seat × number of seats</li>
                </ul>
            </li>
            <li><strong>Reservation Handling:</strong>
                <ul>
                    <li>Reservations reserve only available seats</li>
                    <li>Requested seats already booked are excluded</li>
                    <li>Synchronized blocks prevent race conditions during simultaneous reservations</li>
                    <li>Queue processor handles async requests with callbacks</li>
                    <li>Status field tracks reservation state: HELD, CONFIRMED, CANCELLED</li>
                </ul>
            </li>
            <li><strong>Simplifications:</strong>
                <ul>
                    <li>No partial refunds or cancellations</li>
                    <li>Seat selection is first-come-first-serve</li>
                    <li>No external frameworks; only servlets</li>
                    <li>All operations handle transactional integrity with Serializable isolation</li>
                </ul>
            </li>
        </ul>
    </main>
</body>
</html>
