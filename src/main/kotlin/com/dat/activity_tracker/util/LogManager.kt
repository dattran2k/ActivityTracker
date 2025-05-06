package com.dat.activity_tracker.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.logging.*

/**
 * Utility class for managing application logging
 */
object LogManager {
    private val LOG_DIR = "logs"
    private val _logEntries = MutableStateFlow<List<LogEntry>>(emptyList())
    val logEntries: StateFlow<List<LogEntry>> = _logEntries.asStateFlow()
    
    /**
     * Setup the logging system
     */
    fun setupLogging() {
        // Create logs directory if it doesn't exist
        File(LOG_DIR).mkdirs()
        
        // Use UTF-8 for all log files
        val logFileName = "activity_tracker_${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))}.log"
        val logFile = File(LOG_DIR, logFileName)
        
        // Root logger
        val rootLogger = Logger.getLogger("")
        rootLogger.level = Level.INFO
        
        // Remove existing handlers
        for (handler in rootLogger.handlers) {
            rootLogger.removeHandler(handler)
        }
        
        // Add file handler
        val fileHandler = FileHandler(logFile.path, true)
        fileHandler.encoding = "UTF-8"
        fileHandler.formatter = SimpleFormatter()
        rootLogger.addHandler(fileHandler)
        
        // Add console handler
        val consoleHandler = ConsoleHandler()
        consoleHandler.formatter = SimpleFormatter()
        rootLogger.addHandler(consoleHandler)
        
        // Add custom handler for UI updates
        val customHandler = object : Handler() {
            override fun publish(record: LogRecord) {
                val entry = LogEntry(
                    timestamp = LocalDateTime.now(),
                    level = record.level.name,
                    message = record.message,
                    logger = record.loggerName
                )
                
                val currentList = _logEntries.value.toMutableList()
                currentList.add(0, entry) // Add to the beginning for newest first
                
                // Keep at most 1000 entries
                if (currentList.size > 1000) {
                    _logEntries.value = currentList.take(1000)
                } else {
                    _logEntries.value = currentList
                }
            }

            override fun flush() {}
            override fun close() {}
        }
        
        rootLogger.addHandler(customHandler)
        
        // Log startup
        val logger = Logger.getLogger(LogManager::class.java.name)
        logger.info("Logging system initialized")
    }
    
    /**
     * Add a log entry programmatically
     */
    fun addLogEntry(level: Level, message: String, logger: String = "activity_tracker") {
        val loggerInstance = Logger.getLogger(logger)
        loggerInstance.log(level, message)
    }
}

/**
 * Data class for a log entry
 */
data class LogEntry(
    val timestamp: LocalDateTime,
    val level: String,
    val message: String,
    val logger: String
)
