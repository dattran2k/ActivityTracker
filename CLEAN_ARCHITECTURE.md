# Clean Architecture Conversion

This document outlines the conversion of the Activity Tracker application to use Clean Architecture principles.

## Architecture Overview

The application now follows a domain-driven design approach with clear separation of concerns:

1. **Domain Layer**: Contains business logic and models independent of any frameworks
2. **Data Layer**: Implements repositories and manages data persistence
3. **UI Layer**: Handles user interface using Jetpack Compose for Desktop

## Key Components

### Domain Layer

- **Models**: Simple data classes representing core business entities
- **Repository Interfaces**: Define contracts for data operations without implementation details

### Data Layer

- **Database Manager**: Handles SQLite database operations
- **Repository Implementations**: Implement repository interfaces defined in the domain layer
- **Entity Mapping**: Translates between database entities and domain models

### UI Layer

- **ActivityTrackerApp**: Main UI component
- **Feature Components**: Specific UI components for each feature (monitoring, categories)
- **ViewModels**: Handle UI state and business logic (implemented as part of composables)

## Benefits of Clean Architecture

1. **Testability**: Each layer can be tested in isolation
2. **Maintainability**: Clear separation of concerns makes code easier to understand and modify
3. **Flexibility**: Framework-specific code is separated from business logic, making it easier to change frameworks
4. **Dependency Rule**: Dependencies only point inward, with domain layer having no dependencies on outer layers

## Implementation Notes

- Used direct SQLite JDBC for database operations instead of Room due to desktop environment constraints
- Implemented repository pattern to abstract data access
- Applied coroutines for asynchronous operations
- Maintained existing UI components while connecting them to the new architecture

## Future Improvements

1. Add proper dependency injection with a framework like Koin
2. Implement proper ViewModels for better state management
3. Add comprehensive unit tests for each layer
4. Consider using a wrapper for SQLite that is more desktop-friendly
