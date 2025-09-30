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
            max-width: 800px;
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
            background: #e8f5e9;
            margin: 10px 0;
            padding: 15px;
            border-radius: 5px;
        }
        code {
            background: #f0f0f0;
            padding: 2px 6px;
            border-radius: 3px;
            color: #d6336c;
        }
    </style>
</head>
<body>
    <header>
        <h1>Bus Reservation Service</h1>
        <p>Your simple API to check availability, reserve seats, manage seats, and update reservations</p>
    </header>
    <main>
        <h2>Available Endpoints</h2>
        <ul>
            <li>
                <strong>Check Availability:</strong><br>
                GET <code>/bus-reservation/api/availability?date=YYYY-MM-DD&origin=A&destination=D</code>
            </li>
            <li>
                <strong>Reserve Seats:</strong><br>
                POST <code>/bus-reservation/api/reserve</code><br>
                JSON body example:
                <pre>
{
  "date": "2025-09-28",
  "origin": "A",
  "destination": "D",
  "seats": ["S1", "S2"]
}
                </pre>
            </li>
            <li>
                <strong>Manage Seats:</strong><br>
                GET <code>/bus-reservation/api/seats</code> - List all seats<br>
                POST <code>/bus-reservation/api/seats</code> - Add a seat (JSON body: <code>{"seatId": "S1"}</code>)
            </li>
            <li>
                <strong>Update Reservation:</strong><br>
                POST <code>/bus-reservation/api/reservation/update</code><br>
                JSON body example:
                <pre>
{
  "id": "RES123",
  "status": "COMPLETED"
}
                </pre>
            </li>
        </ul>
    </main>
</body>
</html>
