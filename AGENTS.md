# AGENTS.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Maven plugin that wraps Docker Compose commands to integrate them into the Maven build lifecycle. The plugin provides goals for standard docker-compose operations: up, down, build, push, pull, stop, and restart.

## Build Commands

### Standard Build
```bash
mvn clean install
```

### Run Integration Tests
Integration tests use the Maven Invoker Plugin and require the `integration-tests` profile:
```bash
mvn clean install -Pintegration-tests
```

Note: Integration tests are located in `src/it/` and each test case has its own subdirectory with a `pom.xml` and `verify` script.

### Skip Integration Tests
```bash
mvn clean install
```
(Integration tests only run with the `-Pintegration-tests` profile)

### Release Build
```bash
mvn clean install -Prelease
```

This activates source/javadoc generation and GPG signing for deployment to Maven Central.

## Architecture

### Core Components

**AbstractDockerComposeMojo** (`src/main/java/com/dkanejs/maven/plugins/docker/compose/AbstractDockerComposeMojo.java`)
- Base class for all plugin goals
- Handles common configuration parameters (composeFile, composeFiles, services, projectName, host, etc.)
- Builds the `docker compose` command with appropriate flags
- Manages environment variables from `envFile` and `envVars` configuration
- Uses new `docker compose` command (not legacy `docker-compose`)

**Goal Mojos**
Each docker-compose command has its own Mojo class extending AbstractDockerComposeMojo:
- `DockerComposeUpMojo` - Starts containers
- `DockerComposeDownMojo` - Stops and removes containers
- `DockerComposeBuildMojo` - Builds images
- `DockerComposePushMojo` - Pushes images to registry
- `DockerComposePullMojo` - Pulls images from registry
- `DockerComposeStopMojo` - Stops containers
- `DockerComposeRestartMojo` - Restarts containers

**BuildArguments** (`src/main/java/com/dkanejs/maven/plugins/docker/compose/BuildArguments.java`)
- Configuration class for build-specific arguments (forceRm, noCache, alwaysPull, args)

**DockerComposeException** (`src/main/java/com/dkanejs/maven/plugins/docker/compose/DockerComposeException.java`)
- Custom exception for docker-compose errors

### Command Execution Flow

1. User invokes a Maven goal (e.g., `mvn docker-compose:up`)
2. Corresponding Mojo's `execute()` method is called
3. Mojo builds argument list specific to the docker-compose command
4. AbstractDockerComposeMojo's `execute(List<String>)` method:
   - Calls `buildProcess()` to create ProcessBuilder
   - Constructs command: `docker compose -f <file> [options] <command> [args]`
   - Sets environment variables from envFile and envVars
   - Executes process and streams output to Maven log
   - Throws exception if exit code is non-zero

### Integration Tests

Integration tests use Maven Invoker Plugin to run the plugin against test projects in `src/it/`:
- Each test directory contains a `pom.xml` configuring the plugin
- A `docker-compose.yml` file for the test scenario
- A `verify` Groovy script that validates the test results
- Tests cover various configuration combinations (multiple files, services, environment variables, build args, etc.)

## Configuration Patterns

### Single Compose File
Default location: `${project.basedir}/src/main/resources/docker-compose.yml`
Can be overridden with `<composeFile>` configuration parameter

### Multiple Compose Files
Use `<composeFiles>` parameter with multiple `<composeFile>` entries
When set, the single `composeFile` parameter is ignored

### Environment Variables
Two ways to set environment variables:
1. `<envFile>` - Properties file with key=value pairs
2. `<envVars>` - Direct XML configuration (overrides envFile values)

### Service Filtering
Use `<services>` parameter to operate on specific services only
When set, commands only affect the specified services

### Profiles
Use `<profiles>` parameter to specify which Docker Compose profiles to enable:
```xml
<configuration>
    <profiles>
        <profile>dev</profile>
        <profile>debug</profile>
    </profiles>
</configuration>
```

### Additional Global Options
- `<parallel>` - Control max parallelism (-1 for unlimited)
- `<progress>` - Set type of progress output (auto, tty, plain, json, quiet)
- `<projectDirectory>` - Specify alternate working directory
- `<dryRun>` - Execute command in dry run mode (boolean, default: false)

## Development Notes

- Plugin uses modern `docker compose` command (not legacy `docker-compose` with hyphen)
- Minimal dependencies: only Maven API, plugin annotations, and plexus-utils
- Java 8 compatible (source/target 1.8)
- EditorConfig plugin enforces code formatting standards
- No unit tests in src/test - testing is done via integration tests only
