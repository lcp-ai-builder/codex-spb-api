# Repository Guidelines

## Project Structure & Module Organization
- `pom.xml` defines dependencies and plugins for the Spring Boot 3.3/Maven build.
- `src/main/java/com/example/demo` holds application code; `user` package exposes User domain, mapper, service, and reactive controller.
- `src/main/resources/application.properties` contains Undertow, MySQL, and MyBatis-Plus configuration; adjust before deploying.
- `src/test/java/com/example/demo` contains JUnit 5/Mockito tests; mirror package structure from `src/main`.

## Build, Test, and Development Commands
- `mvn clean package` compiles sources, runs tests, and assembles the runnable JAR.
- `mvn test` executes the JUnit 5 suite, including Reactor-based StepVerifier checks.
- `mvn spring-boot:run` launches the Undertow-backed WebFlux service; ensure MySQL is reachable.

## Coding Style & Naming Conventions
- Use Java 17 features sparingly; prefer standard Spring idioms.
- Follow Java conventions: `UpperCamelCase` for classes, `lowerCamelCase` for fields and methods, and 4-space indentation.
- Reactive endpoints should return Reactor types (`Mono`, `Flux`); block only inside bounded elastic schedulers when wrapping MyBatis-Plus calls.

## Testing Guidelines
- Tests live under `src/test/java` and use JUnit 5 with Mockito; leverage `reactor-test`’s `StepVerifier` for reactive flows.
- Name test classes `<ClassUnderTest>Test`; describe scenarios with `@Test` method names such as `saveEmitsInsertedUser`.
- Run `mvn test` before submitting any change set.

## Commit & Pull Request Guidelines
- Craft concise commit messages in imperative mood, e.g., “Add reactive user service”.
- Pull requests should summarize scope, list major changes, and mention database/config migrations.
- Include manual verification steps (commands run, endpoints hit) in PR descriptions when applicable.
