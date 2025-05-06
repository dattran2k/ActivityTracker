# Activity Tracker

A Kotlin desktop application for tracking computer activity using Jetpack Compose for Desktop and Room database.

## Project Structure

The application follows Clean Architecture principles:

```
com.dat.activity_tracker/
├── data/                  # Data layer
│   ├── local/             # Database implementation
│   │   ├── dao/           # Data Access Objects
│   │   ├── entity/        # Room entities
│   │   ├── util/          # Database utilities
│   │   └── ActivityTrackerDatabase.kt
│   ├── repository/        # Repository implementations
│   └── AppCategorizer.kt  # Application categorization logic
├── domain/                # Domain layer
│   ├── model/             # Domain models
│   └── repository/        # Repository interfaces
├── di/                    # Dependency Injection
│   └── DatabaseModule.kt  # Database and repository providers
├── monitor/               # Activity monitoring
│   └── ActivityMonitor.kt # System monitoring implementation
├── report/                # Reporting functionality 
│   └── ReportGenerator.kt # Report generation logic
├── ui/                    # User interface
│   ├── components/        # Reusable UI components
│   ├── feature/           # Feature-specific UI components
│   └── ActivityTrackerApp.kt # Main UI component
└── util/                  # Utility classes
    ├── DesktopContext.kt  # Desktop-specific Android Context
    ├── LogManager.kt      # Logging utilities
    └── extensions.kt      # Kotlin extensions
```

## Key Features

- **Activity Monitoring**: Tracks application usage in real-time
- **Category Management**: Automatically categorizes applications and allows manual categorization
- **Usage Reports**: Generates daily, weekly, and monthly reports with charts
- **Cross-Platform Support**: Works on Windows, macOS, and Linux

## Technical Details

- **Clean Architecture**: Separation of concerns between data, domain, and presentation layers
- **Room Database**: Android's Room ORM for local data persistence
- **Jetpack Compose for Desktop**: Modern declarative UI toolkit
- **Coroutines**: For asynchronous programming
- **JNA**: For native operating system monitoring

## Building and Running

1. Ensure you have JDK 21 or newer installed
2. Build the project with Gradle:
   ```
   ./gradlew build
   ```
3. Run the application:
   ```
   ./gradlew run
   ```

## Database Schema

The application uses Room with the following entities:

- **Activities**: Records application usage details (app name, window title, timestamps, duration)
- **AppCategories**: Maps applications to categories
- **SystemEvents**: Records system events like startup and shutdown

## Implementation Notes

- Room is used with a custom DesktopContext to make it work in a non-Android environment
- The application uses a combination of JNA and Python scripts for cross-platform monitoring
- Reports are generated as HTML with embedded charts

## Future Improvements

- Add more detailed timeline visualization
- Implement cloud sync for multi-device usage tracking
- Add productivity insights and goal tracking
- Improve categorization with machine learning
