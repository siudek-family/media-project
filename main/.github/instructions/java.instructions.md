---
description: 'Guidelines for building Java base applications'
applyTo: '**/*.java'
---

# Java Development

## General Instructions

- Address code smells proactively during development rather than accumulating technical debt.
- Focus on readability, maintainability, and performance when refactoring identified issues.
- Use IDE / Code editor reported warnings and suggestions to catch common patterns early in development.

## Best practices

- **Records**: For classes primarily intended to store data (e.g., DTOs, immutable data structures), **Java Records should be used instead of traditional classes**.
- **Pattern Matching**: Utilize pattern matching for `instanceof` and `switch` expression to simplify conditional logic and type casting.
- **Type Inference**: Use `var` for local variable declarations to improve readability, but only when the type is explicitly clear from the right-hand side of the expression.
- **Immutability**: Favor immutable objects. Make classes and fields `final` where possible. Use collections from `List.of()`/`Map.of()` for fixed data. Use `Stream.toList()` to create immutable lists.
- **Streams and Lambdas**: Use the Streams API and lambda expressions for collection processing. Employ method references (e.g., `stream.map(Foo::toBar)`).
- **Null Handling**: Avoid returning or accepting `null`. Use `Optional<T>` for possibly-absent values and `Objects` utility methods like `equals()` and `requireNonNull()`.

### Naming Conventions

- Follow Google's Java style guide:
  - `UpperCamelCase` for class and interface names.
  - `lowerCamelCase` for method and variable names.
  - `UPPER_SNAKE_CASE` for constants.
  - `lowercase` for package names.
- Use nouns for classes (`UserService`) and verbs for methods (`getUserById`).
- Avoid abbreviations and Hungarian notation.

### Common Bug Patterns

Below are concise, human-readable rules you can apply regardless of which static analysis tool you use. If you run Sonar/SonarLint, the IDE will show the matching rule and location — direct Sonar connections are preferred and should override this ruleset.

- Resource management — Always close resources (files, sockets, streams). Use try-with-resources where possible so resources are closed automatically.
- Equality checks — Compare object equality with `.equals()` or `Objects.equals(...)` rather than `==` for non-primitives; this avoids reference-equality bugs.
- Redundant casts — Remove unnecessary casts; prefer correct generic typing and let the compiler infer types where possible.
- Reachable conditions — Avoid conditional expressions that are always true or false; they indicate bugs or dead code and should be corrected.

For contributors who *do* use Sonar or SonarLint: the IDE/scan will show the specific rule key (for example, S2095 for resource leaks) and the affected file/line. Use that information to navigate to the exact location, then apply the recommended remediation.

### Common Code Smells

These patterns are phrased for humans; they map cleanly to checks in Sonar, SpotBugs, PMD, or Checkstyle but do not require those tools to be useful.

- Parameter count — Keep method parameter lists short. If a method needs many params, consider grouping into a value object or using the builder pattern.
- Method size — Keep methods focused and small. Extract helper methods to improve readability and testability.
- Cognitive complexity — Reduce nested conditionals and heavy branching by extracting methods, using polymorphism, or applying the Strategy pattern.
- Duplicated literals — Extract repeated strings and numbers into named constants or enums to reduce errors and ease changes.
- Dead code — Remove unused variables and assignments. They confuse readers and can hide bugs.
- Magic numbers — Replace numeric literals with named constants that explain intent (e.g., MAX_RETRIES).

If you run a static analyzer like Sonar or SonarLint — direct Sonar connections are preferred and should override this ruleset. Sonar rule keys are useful for automation and suppression, but they are not required in day-to-day developer guidance.

## Build and Verification

- After adding or modifying code, verify the project continues to build successfully.
- If the project uses Maven, run `mvnw clean install`.
- Ensure all tests pass as part of the build.

### Testing
- use JUnit 5 for unit testing.
- use Mockito for mocking dependencies in tests.
- use AssertJ for fluent assertions, avoid using JUnit assertions.
- use `@SpringBootTest` with mocked dependencies to capture emitted commands.
- Assert on command type and properties (e.g., rename source/target).

## Project-Specific Java Patterns

### Sealed Interfaces for Domain Models

Use sealed interfaces to define exhaustive type hierarchies for supported media types:
```java
sealed interface Source {
  sealed interface Dir extends Source {}
  sealed interface File extends Source {}
  record AmrFile(Path value) implements File {}
  // ... other file types
}
```

**Convention**: When adding new file type support, add a new record to `Source` and implement corresponding rename/validation logic.

### Pattern Matching with Sealed Types

Code consistently uses:
- **Switch expressions with sealed types** (switch on `Source` subtypes)
- **Records** for immutable data (command types, file types)
- **`var` inference** for local variables where type is clear from RHS
- **`@Slf4j`** (Lombok) for logging instead of manual logger fields

**Example**:
```java
switch (source.source()) {
  case Source.MediaDir mediaDir -> { /* pattern matched */ }
  case Source.GitDir gitDir -> { /* ... */ }
}
```

### Strategy Pattern Implementation

Rename strategies implement `RenameStrategy` interface:
- Each strategy validates file name patterns via regex
- On match, emits commands via `CommandsListener`
- Strategies are autowired into container and iterated during processing

**Adding a new rename strategy**:
1. Create class implementing `RenameStrategy`
2. Inject `CommandsListener` (required to emit commands)
3. Annotate with `@Component` for auto-discovery
4. Implement pattern matching logic in `tryRename(Path)`
5. Extract regex groups and construct appropriate `Meta` record type
6. Emit command with extracted metadata

### Command Emission over Direct Side Effects

All strategy operations emit domain commands rather than directly performing I/O:
```java
var cmd = new MediaCommands.RenameMediaItem(path, newFileName);
commandsListener.on(cmd);
```

**Benefit**: Decouples strategy logic from execution; enables testing via mock listeners and flexible command handling (logging, actual rename, audit).

### Listener/Observer for Event Handling

`CommandsListener` interface enables extensible command handling:
- Spring component that logs all commands
- Tests override with test implementation to capture emissions
- Strategies never know *how* commands are handled

**When adding new command types**: Define new records in `MediaCommands` and ensure listeners handle them appropriately.