# Copilot Instructions for Media Project

## Project Overview

**Media** is a Spring Boot CLI application that processes, validates, and normalizes media file structures and naming conventions. It combines Spring Shell for CLI interaction, Spring Batch for bulk processing, and a pluggable strategy pattern for extensible file renaming operations.

### Key Architecture Components

- **Source Model**: Sealed interface hierarchy (`Source.java`) representing all supported media file types (JPG, PNG, AMR, MP4, MKIV, etc.) and directories (MediaDir, GitDir, DvdDir)
- **Rename Strategies**: Pluggable `RenameStrategy` implementations for different file naming patterns (Generic1, AMR1)
- **Media Processor**: `Media.java` component orchestrates file traversal and applies rename strategies
- **Commands Pattern**: `MediaCommands` and `CommandsListener` decouple strategy execution from side effects (logging, UI updates)

## Essential Developer Workflows

### Building & Testing

```bash
# Build with Maven
mvnw clean install

# Run tests only
mvnw test

# Run application
mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="start"
```

**Key Configuration**: 
- Java 25 (see `pom.xml`)
- Lombok for code generation (requires annotation processor configuration in Maven compiler plugin)
- Spring Shell with `org.jline.terminal.dumb=true` for non-interactive terminal support

### Testing Pattern

Tests use `@SpringBootTest` with mocked `CommandsListener` to capture command emissions. Example from [MediaTest.java](src/test/java/net/siudek/media/MediaTest.java):
- Use `TestCommandsListener` to collect emitted commands
- Assert on command type and properties (e.g., rename source/target)
- Mock Spring Shell: `spring.shell.interactive.enabled=false`

## Project-Specific Patterns & Conventions

### 1. **Sealed Interfaces for Domain Models**

`Source.java` uses sealed interfaces to define exhaustive type hierarchies for supported media types. See [java.instructions.md](instructions/java.instructions.md) for pattern details and conventions.

### 2. **Strategy Pattern for Rename Operations**

Rename strategies implement `RenameStrategy` interface (see `Generic1RenameStrategy.java`, `Amr1RenameStrategy.java`, `Amr2RenameStrategy.java`):
- Each strategy validates file name patterns via regex
- On match, emits `MediaCommands.RenameMediaItem` command via `CommandsListener`
- Strategies are autowired into container and iterated during processing
- `Media.verifyNameConvention()` iterates all strategies and ensures exactly one matches per file

For implementation details and step-by-step guide, see [java.instructions.md](instructions/java.instructions.md).

### 3. **Command Emission over Direct Side Effects**

All strategy operations emit domain commands rather than directly performing I/O. This decouples strategy logic from execution and enables testing via mock listeners. See [java.instructions.md](instructions/java.instructions.md) for implementation pattern.

### 4. **Listener/Observer for Event Handling**

`CommandsListener` interface enables extensible command handling:
- `LoggingCommandsListener`: Spring component that logs all commands
- Tests override with `TestCommandsListener` to capture emissions
- Strategies never know *how* commands are handled

**When adding new command types**: Define new records in `MediaCommands` and ensure listeners handle them appropriately.

## File Structure Reference

```
src/main/java/net/siudek/media/
├── Media.java                 # Core orchestrator (sealed type switching, strategy iteration)
├── MediaItem.java             # Domain model interface
├── MediaCommands.java         # Command record types emitted by strategies
├── CommandsListener.java      # Event sink interface + default logging implementation
├── Source.java                # Sealed hierarchy of media file/dir types
├── Sources.java               # Factory or utilities for Source objects
├── Program.java               # Spring Boot entry point
├── shell/
│   └── HelloWorldCommands.java  # Spring Shell CLI commands (@ShellMethod)
└── rename/
    ├── RenameStrategy.java          # Strategy interface
    ├── Generic1RenameStrategy.java  # Converts yyyyMMdd_hhmmss → yyyyMMdd-hhmmss
    ├── Amr1RenameStrategy.java      # AMR phone call files
    └── Amr2RenameStrategy.java      # AMR microphone recordings
```

## Important Integration PointsHelloWorldCommands.java` (shell commands annotated with `@ShellMethod`)
- **Lombok**: Generates constructors (`@RequiredArgsConstructor`), logging (`@Slf4j`), getters/setters; Maven compiler plugin must include annotation processor path
- **Java File I/O**: Uses `java.nio.file.Path` throughout (not String); enables filesystem abstraction
- **Application Properties**: Configure with `spring.shell.interactive.enabled=true` for interactive mode or `false` for batch/test modek processing workflows
- **Spring Shell**: Provides CLI interface via `MediaCommands.java` (shell commands for user interaction)
- **Lombok**: Generates constructors (`@RequiredArgsConstructor`), logging (`@Slf4j`), getters/setters; Maven compiler plugin must include annotation processor path
- **Java File I/O**: Uses `java.nio.file.Path` throughout (not String); enables filesystem abstraction

## Code Quality Guidelines

All Java code should follow the guidelines in [java.instructions.md](instructions/java.instructions.md), which includes:
- Modern Java best practices (records, pattern matching, sealed types, var inference)
- Project-specific patterns (sealed interfaces, strategy pattern, command emission)
- Code quality rules (immutability, null handling, resource management)
- Build verification and testing standards

## When Adding New Features

1. **New file type**: Add sealed record to `Source` interface
2. **New rename pattern**: Create `@Component` class implementing `RenameStrategy`; inject `CommandsListener`
3. **New command type**: Add record to `MediaCommands`; ensure listeners can handle it
4. **Core logic**: Use sealed type switching in `Media.java`; emit commands via listener
5. **Tests**: Use `@SpringBootTest` + mock `TestCommandsListener` to verify commands emitted correctly
