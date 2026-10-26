# Ticket Booking System

A movie ticket booking application with a Spring Boot backend and a Streamlit user interface.

## What is included

- Spring Boot 3.5.6 REST API
- Spring Data JPA persistence
- In-memory H2 database
- Thymeleaf pages for browsing movies, shows, seats, registration, and login
- Streamlit interface for browsing movies, selecting showtimes and seats, completing a simulated payment, viewing booking history, and cancelling bookings
- Card and UPI payment gateway simulations
- Seat availability checks and concurrent booking protection
- JUnit integration tests for the API, booking flow, payment outcomes, cancellation, validation, and concurrent seat booking

The application is a demo. It does not connect to a real payment provider, and payment references entered in the UI are not real payment credentials.

## Technology

- Java 17 or newer
- Maven
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Validation
- Thymeleaf
- H2
- Python
- Streamlit
- Requests

The backend's Maven project is configured for Java 17. Python dependencies are listed in [`requirements.txt`](requirements.txt).

## Requirements

Install the following on the system where the project will run:

1. A Java Development Kit (JDK) 17 or newer.
2. Apache Maven.
3. Python and `pip`.

No external database server is required. The application uses an in-memory H2 database.

## Setup

These commands work from the project directory. On Windows, use PowerShell; on macOS or Linux, use the equivalent shell commands.

### 1. Start the Spring Boot backend

```text
mvn spring-boot:run
```

The backend starts on `http://localhost:8080`. It exposes the REST API below the `/api` path.

The default startup profile loads:

- one demo user: `demo@example.com`
- three movies
- nine shows (three shows for each movie)
- 50 seats for each show

The database is in memory, so the data and bookings are recreated when the backend restarts.

### 2. Install the Streamlit dependencies

Keep the backend running and open a second terminal in the project directory.

Windows PowerShell:

```powershell
python -m venv .venv
.\.venv\Scripts\Activate.ps1
python -m pip install -r requirements.txt
```

macOS or Linux:

```text
python3 -m venv .venv
source .venv/bin/activate
python -m pip install -r requirements.txt
```

### 3. Start the user interface

```text
streamlit run streamlit_app.py
```

Streamlit normally opens `http://localhost:8501`. The UI is configured to call the backend at `http://localhost:8080/api`.

## Using the application

1. Open the Streamlit URL.
2. Browse the seeded movies and choose a showtime.
3. Select available seats.
4. Enter a name, email address, and a demo payment reference.
5. Choose `CARD` or `UPI` and confirm the booking.
6. Use **My bookings** to view booking history or cancel a confirmed booking.

The payment gateways are simulated:

- Card references shorter than four characters are rejected.
- Card references ending in `0000` are declined.
- UPI references must contain `@`.
- UPI references beginning with `fail` are rejected.

Do not enter real card numbers or banking credentials.

## REST API

The backend provides these endpoints:

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/api/movies` | List movies |
| `GET` | `/api/movies/{id}` | Get one movie |
| `GET` | `/api/movies/{id}/shows` | List showtimes for a movie |
| `GET` | `/api/shows/{id}` | Get one show |
| `GET` | `/api/shows/{id}/seats` | Get seat availability for a show |
| `POST` | `/api/users` | Register a user |
| `GET` | `/api/users/{id}` | Get a user |
| `GET` | `/api/users/{id}/bookings` | Get a user's booking history |
| `POST` | `/api/bookings` | Create a booking |
| `GET` | `/api/bookings/{id}` | Get a booking |
| `POST` | `/api/bookings/{id}/cancel` | Cancel a confirmed booking |

### Create a booking

```http
POST /api/bookings
Content-Type: application/json
```

```json
{
  "userId": 1,
  "showId": 2,
  "seatNumbers": ["A1"],
  "paymentMethod": "UPI",
  "paymentReference": "demo@upi"
}
```

The configured maximum is six seats per booking. Seat names are normalized to uppercase, duplicate seats are rejected, and unavailable or unknown seats produce an error response.

## Backend pages

The Spring Boot application also serves these pages:

- `/` - movie list
- `/movies/{id}` - movie details and showtimes
- `/shows/{id}` - show details and seat map
- `/register` - registration form
- `/login` - login page

The full booking and simulated payment interaction is provided by the Streamlit UI.

## Testing

Run the Java test suite:

```text
mvn test
```

The tests cover the seeded REST flow, validation and error responses, booking and cancellation behavior, failed payments, seat limits, duplicate and unknown seats, and concurrent attempts to book the same seat.

To check the Streamlit file without starting the UI:

```text
python -m py_compile streamlit_app.py
```

## Configuration

The main backend settings are in [`src/main/resources/application.properties`](src/main/resources/application.properties):

- server port: `8080`
- H2 in-memory database
- schema mode: `create-drop`
- maximum seats per booking: `6`

The tests use a separate H2 configuration in [`src/test/resources/application-test.properties`](src/test/resources/application-test.properties).

## Project layout

```text
src/main/java/                 Spring Boot application, API, services, models, and repositories
src/main/resources/templates/ Thymeleaf pages
src/main/resources/static/     Static web resources
src/test/java/                 Integration and concurrency tests
src/test/resources/            Test configuration
streamlit_app.py               Streamlit frontend
requirements.txt               Python dependencies
pom.xml                        Maven build and Java dependencies
```

## Current limitations

- H2 is in memory; data is not retained between restarts.
- Payments are simulations only.
- There is no authentication or authorization mechanism.
- The Streamlit UI is configured for a backend running on the same machine at `localhost:8080`.
