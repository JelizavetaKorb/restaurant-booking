# Restaurant Booking System
Restaurant table reservation app with a visual floor plan. Built as a take-home assignment.

## How to Run
You need **Java 21**. Check with `java -version`. Install on Mac: `brew install openjdk@21`.
No need to install Maven, the project has a wrapper included.
```bash
git clone <copied-repository-url>
cd restaurant-booking
./mvnw spring-boot:run
```
Or open the project in IntelliJ and run `RestaurantBookingApplication`.
Then go to http://localhost:8080.
The database is H2 in-memory, so everything resets when you restart. Sample tables and bookings are loaded automatically on startup from `data.sql`.

## Admin View
Click the **Admin** button in the top-right corner of the main page. Password is `admin123`.
From there you can drag tables around the floor plan, set working hours, and see all reservations.

## What It Does
Customers pick a date, time, party size, and optionally a zone or seating preference. The app scores available tables and recommends the best fit. Large groups get table merge suggestions. Booking times are limited to the restaurant's working hours.
Admins can reposition tables by dragging them (zone updates automatically), configure opening/closing times, and view upcoming bookings.

## Tech
Java 21, Spring Boot 3.2, Spring Data JPA, H2, vanilla HTML/CSS/JS, Maven.

## Development
Took around **30 hours** total. I broke the work into stages and mostly followed this order:
1. Planning and setup - figured out what the app needs, picked the stack, scaffolded the Spring Boot project.
2. Data model and backend - designed the schema, wrote entities and repositories, built the service layer with availability checks and scoring.
3. REST API - wired up controllers for tables, bookings, and settings.
4. Customer frontend - SVG floor plan, filters, table rendering, booking modal.
5. Admin panel - login, drag-and-drop table management, reservations list, working hours config.
6. Testing and cleanup - wrote tests, extracted CSS, fixed edge cases.
7. Seed data and docs - populated data.sql and wrote this README.

The drag-and-drop positioning was the hardest part. Getting coordinates right across screen sizes and keeping zone detection accurate after moves took a while.

## Tests
Run with `./mvnw test`.
Tests cover booking availability, the scoring algorithm, overlap rejection, table merging for large groups, and working hours validation. AI helped write most of the test structure.

## AI Use
Used GitHub Copilot for the frontend UI work, writing tests, and documentation. Reviewed and adjusted everything manually.

## For a Real Restaurant
You'd swap H2 for PostgreSQL, add real auth, match the floor plan to the actual room layout, and probably add email confirmations. Table positions are already configurable through the admin drag interface so that part is just repositioning, not code changes.
