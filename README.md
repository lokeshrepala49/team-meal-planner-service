# team-meal-planner-service

A Spring Boot service for planning team meals (meals, people, signups).

Quick start

1. Prerequisites

- Java 17
- Maven 3.6+

2. Build

```bash
cd /path/to/team-meal-planner-service
mvn -DskipTests package
```

3. Run

```bash
# run the app locally
mvn spring-boot:run
# or run the built jar
java -jar target/team-meal-planner-service-0.0.1-SNAPSHOT.jar
```

4. Tests

```bash
# run unit tests
mvn test
```

5. API (quick notes)

- POST /api/meals — create a meal (returns 201 + Location)
- GET /api/meals — list meals (supports date range, cuisine, tag, pagination & sorting)
- GET /api/meals/{id} — meal details (includes attendee count)
- PUT /api/meals/{id} — update meal (optimistic locking via `version`)
- DELETE /api/meals/{id} — delete meal (fails if signups exist)

- POST /api/signups — create signup (mealId, personId, note). Business rules enforced.
- GET /api/signups?personId=...&date=...&range=day|week — list a person’s signups

- POST /api/people — create a person

Notes

- Config is in `src/main/resources/application.yaml`.
- Error responses use a consistent JSON shape: `{ timestamp, status, error, message, path, details[] }`.
- Import the Postman collection from `MealPlanner API.postman_collection.json` for API testing.

