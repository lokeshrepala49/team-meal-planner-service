[2 tools called]

## Design Notes

- **Problem/Scope**
    - Internal service to schedule team meals, manage attendees, and collect meal signups.
    - MVP targets CRUD for meals/people plus signup workflow with clear business rules (no double booking, capacity guardrails later).

- **Architecture Overview**
    - Spring Boot REST app (`TeamMealPlannerServiceApplication`) with layered structure: controller → service → repository.
    - Controllers (`MealController`, `SignupController`, `PersonController`) expose `/api/*` endpoints and delegate to services.
    - Services encapsulate rules (e.g., signup collision checks) and orchestrate persistence.
    - Repositories extend Spring Data JPA for `Meal`, `Person`, `Signup` entities; database assumed relational (H2).
    - Global `RestExceptionHandler` normalizes error responses; `OpenApiConfig` publishes Swagger docs.

- **Domain Model**
    - `Meal`: id, title, scheduledAt, cuisine, dietary tags, capacity, `version` for optimistic locking.
    - `Person`: id, name, email, dietary tags.
    - `Signup`: links `Person` ↔ `Meal`, optional note, timestamps; business rules prevent duplicates/conflicts.
    - `DietaryTag` enum ensures consistent tagging across meals and people.

- **Key Flows**
    - Meal creation (`MealCreate` DTO) → persisted meal; updates require matching `version`, deletes blocked when signups exist.
    - Signup creation (`SignupCreate`) checks meal availability, person eligibility, uniqueness before saving.
    - Listing endpoints offer filters (date, cuisine, tags) and pagination; meal detail includes attendee counts.

- **Non-Functional Considerations**
    - Error payload shape `{timestamp,status,error,message,path,details[]}` supports UI consistency.