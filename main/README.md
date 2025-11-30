# Media Application

Spring Boot application demonstrating Spring Batch and Spring Shell integration.

## Prerequisites

- Java 25
- Maven 3.x

## Building the Application

To build the application, run:

```bash
mvnw clean package
```

Or on Windows:

```bash
mvnw.cmd clean package
```

## Running the Application

### Run with Maven

```bash
mvnw spring-boot:run
```

Or on Windows:

```bash
mvnw.cmd spring-boot:run
```

### Run the JAR file (Recommended for better terminal support)

After building, you can run the generated JAR directly for a better interactive experience:

```bash
java -jar target/media-0.0.1-SNAPSHOT.jar
```

**Note:** Running via `mvnw spring-boot:run` may show JLine warnings. Use the JAR file directly for the best terminal experience.

## Spring Shell

The application includes an interactive Spring Shell CLI. When you run the application, you'll see a shell prompt where you can execute commands.

### Available Commands

**hello** - Displays a simple hello world message
```
shell:>hello
Hello World from Spring Shell!
```

**greet** - Greets a person by name
```
shell:>greet John
Hello, John! Welcome to Spring Shell.
```

If you don't provide a name, it defaults to "User":
```
shell:>greet
Hello, User! Welcome to Spring Shell.
```

**add** - Adds two numbers
```
shell:>add 5 3
5 + 3 = 8
```

### Using Spring Shell

1. Start the application using one of the methods above
2. You'll see a `shell:>` prompt
3. Type `help` to see all available commands
4. Type any command name to execute it
5. Type `exit` or `quit` to close the application

## Spring Batch Job

The application includes a simple "Hello World" Spring Batch job that will automatically run on startup. The job executes a single step that prints "Hello World from Spring Batch!" to the console.

### Job Details

- **Job Name**: `helloWorldJob`
- **Step Name**: `helloWorldStep`
- **Action**: Prints a hello message and completes

The job is configured to run automatically when the application starts (configured via `spring.batch.job.enabled=true` in `application.properties`).

## Spring Shell

The application also includes Spring Shell integration, allowing you to interact with the application via a command-line interface.

## Configuration

Key configuration properties in `application.properties`:

- `spring.batch.job.enabled=true` - Enables automatic job execution on startup
- `spring.batch.jdbc.initialize-schema=embedded` - Automatically initializes the batch schema in the embedded database

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── net/siudek/media/
│   │       ├── MediaApplication.java
│   │       └── batch/
│   │           └── HelloWorldJobConfig.java
│   └── resources/
│       └── application.properties
└── test/
    └── java/
        └── net/siudek/media/
            └── MediaApplicationTests.java
```
