package com.dat.activity_tracker.report

import com.dat.activity_tracker.domain.repository.ActivityRepository
import com.dat.activity_tracker.util.formatDuration
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartUtils
import org.jfree.chart.plot.PiePlot
import org.jfree.chart.plot.PlotOrientation
import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.data.general.DefaultPieDataset
import java.awt.Color
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Generates usage reports in HTML format
 */
class ReportGenerator(
    private val activityRepository: ActivityRepository
) {
    private val logger = Logger.getLogger("activity_tracker.report")
    private val reportsDir = File("reports")
    
    init {
        if (!reportsDir.exists()) {
            reportsDir.mkdir()
        }
    }
    
    /**
     * Generate daily usage report
     */
    suspend fun generateDailyReport(date: String? = null): String? {
        try {
            val currentDate = date ?: LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val usageData = activityRepository.getDailyUsage(currentDate)
            
            if (usageData.isEmpty()) {
                logger.warning("No data available for daily report")
                return null
            }
            
            // Get usage by category
            val categoryData = activityRepository.getUsageByCategory(currentDate, currentDate)
            
            // Create chart directory
            val chartDir = File("reports/charts")
            if (!chartDir.exists()) {
                chartDir.mkdirs()
            }
            
            // Generate app usage pie chart
            val pieDataset = DefaultPieDataset<String>()
            usageData.take(10).forEach {
                pieDataset.setValue(it.appName, it.totalDuration)
            }
            
            val pieChart = ChartFactory.createPieChart(
                "Daily App Usage",
                pieDataset,
                true,
                true,
                false
            )
            
            // Customize chart
            val plot = pieChart.plot as PiePlot<*>
            plot.labelGenerator = null // Hide labels
            plot.backgroundPaint = Color.WHITE
            
            // Save chart
            val pieChartFile = File("reports/charts/daily_app_usage_${currentDate}.png")
            ChartUtils.saveChartAsPNG(pieChartFile, pieChart, 600, 400)
            
            // Generate category usage bar chart
            val categoryDataset = DefaultCategoryDataset()
            categoryData.forEach {
                categoryDataset.addValue(it.totalDuration / 60.0, "Minutes", it.category)
            }
            
            val barChart = ChartFactory.createBarChart(
                "Usage by Category",
                "Category",
                "Minutes",
                categoryDataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
            )
            
            // Save chart
            val barChartFile = File("reports/charts/daily_category_usage_${currentDate}.png")
            ChartUtils.saveChartAsPNG(barChartFile, barChart, 600, 400)
            
            // Create HTML report
            val html = generateHtmlReport(
                "Daily Activity Report - $currentDate",
                usageData,
                categoryData,
                "charts/daily_app_usage_${currentDate}.png",
                "charts/daily_category_usage_${currentDate}.png"
            )
            
            // Save HTML report
            val reportFile = File("reports/daily_report_${currentDate}.html")
            reportFile.writeText(html)
            
            logger.info("Daily report generated: ${reportFile.absolutePath}")
            return reportFile.absolutePath
            
        } catch (e: Exception) {
            logger.severe("Error generating daily report: ${e.message}")
            return null
        }
    }
    
    /**
     * Generate weekly usage report
     */
    suspend fun generateWeeklyReport(): String? {
        try {
            val usageData = activityRepository.getWeeklyUsage()
            
            if (usageData.isEmpty()) {
                logger.warning("No data available for weekly report")
                return null
            }
            
            // Get the date range for the week
            val endDate = LocalDate.now()
            val startDate = endDate.minusDays(7)
            
            // Format dates for display
            val startDateStr = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val endDateStr = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
            
            // Get usage by category
            val categoryData = activityRepository.getUsageByCategory(startDateStr, endDateStr)
            
            // Create chart directory
            val chartDir = File("reports/charts")
            if (!chartDir.exists()) {
                chartDir.mkdirs()
            }
            
            // Generate app usage pie chart
            val pieDataset = DefaultPieDataset<String>()
            usageData.take(10).forEach {
                pieDataset.setValue(it.appName, it.totalDuration)
            }
            
            val pieChart = ChartFactory.createPieChart(
                "Weekly App Usage",
                pieDataset,
                true,
                true,
                false
            )
            
            // Customize chart
            val plot = pieChart.plot as PiePlot<*>
            plot.labelGenerator = null // Hide labels
            plot.backgroundPaint = Color.WHITE
            
            // Save chart
            val pieChartFile = File("reports/charts/weekly_app_usage.png")
            ChartUtils.saveChartAsPNG(pieChartFile, pieChart, 600, 400)
            
            // Generate category usage bar chart
            val categoryDataset = DefaultCategoryDataset()
            categoryData.forEach {
                // Convert to hours for weekly report
                categoryDataset.addValue(it.totalDuration / 3600.0, "Hours", it.category)
            }
            
            val barChart = ChartFactory.createBarChart(
                "Weekly Usage by Category",
                "Category",
                "Hours",
                categoryDataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
            )
            
            // Save chart
            val barChartFile = File("reports/charts/weekly_category_usage.png")
            ChartUtils.saveChartAsPNG(barChartFile, barChart, 600, 400)
            
            // Create HTML report
            val html = generateHtmlReport(
                "Weekly Activity Report ($startDateStr to $endDateStr)",
                usageData,
                categoryData,
                "charts/weekly_app_usage.png",
                "charts/weekly_category_usage.png"
            )
            
            // Save HTML report
            val reportFile = File("reports/weekly_report.html")
            reportFile.writeText(html)
            
            logger.info("Weekly report generated: ${reportFile.absolutePath}")
            return reportFile.absolutePath
            
        } catch (e: Exception) {
            logger.severe("Error generating weekly report: ${e.message}")
            return null
        }
    }
    
    /**
     * Generate monthly usage report
     */
    suspend fun generateMonthlyReport(): String? {
        try {
            val usageData = activityRepository.getMonthlyUsage()
            
            if (usageData.isEmpty()) {
                logger.warning("No data available for monthly report")
                return null
            }
            
            // Get the date range for the month
            val endDate = LocalDate.now()
            val startDate = endDate.minusDays(30)
            
            // Format dates for display
            val startDateStr = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val endDateStr = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
            
            // Get usage by category
            val categoryData = activityRepository.getUsageByCategory(startDateStr, endDateStr)
            
            // Create chart directory
            val chartDir = File("reports/charts")
            if (!chartDir.exists()) {
                chartDir.mkdirs()
            }
            
            // Generate app usage pie chart
            val pieDataset = DefaultPieDataset<String>()
            usageData.take(10).forEach {
                pieDataset.setValue(it.appName, it.totalDuration)
            }
            
            val pieChart = ChartFactory.createPieChart(
                "Monthly App Usage",
                pieDataset,
                true,
                true,
                false
            )
            
            // Customize chart
            val plot = pieChart.plot as PiePlot<*>
            plot.labelGenerator = null // Hide labels
            plot.backgroundPaint = Color.WHITE
            
            // Save chart
            val pieChartFile = File("reports/charts/monthly_app_usage.png")
            ChartUtils.saveChartAsPNG(pieChartFile, pieChart, 600, 400)
            
            // Generate category usage bar chart
            val categoryDataset = DefaultCategoryDataset()
            categoryData.forEach {
                // Convert to hours for monthly report
                categoryDataset.addValue(it.totalDuration / 3600.0, "Hours", it.category)
            }
            
            val barChart = ChartFactory.createBarChart(
                "Monthly Usage by Category",
                "Category",
                "Hours",
                categoryDataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
            )
            
            // Save chart
            val barChartFile = File("reports/charts/monthly_category_usage.png")
            ChartUtils.saveChartAsPNG(barChartFile, barChart, 600, 400)
            
            // Create HTML report
            val html = generateHtmlReport(
                "Monthly Activity Report ($startDateStr to $endDateStr)",
                usageData,
                categoryData,
                "charts/monthly_app_usage.png",
                "charts/monthly_category_usage.png"
            )
            
            // Save HTML report
            val reportFile = File("reports/monthly_report.html")
            reportFile.writeText(html)
            
            logger.info("Monthly report generated: ${reportFile.absolutePath}")
            return reportFile.absolutePath
            
        } catch (e: Exception) {
            logger.severe("Error generating monthly report: ${e.message}")
            return null
        }
    }
    
    /**
     * Generate HTML report content
     */
    private fun generateHtmlReport(
        title: String,
        appUsage: List<com.dat.activity_tracker.domain.model.UsageInfo>,
        categoryUsage: List<com.dat.activity_tracker.domain.model.CategoryUsageInfo>,
        appChartPath: String,
        categoryChartPath: String
    ): String {
        val sb = StringBuilder()
        
        // HTML header
        sb.append("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>$title</title>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        margin: 20px;
                        background-color: #f5f5f5;
                        color: #333;
                    }
                    .container {
                        max-width: 1000px;
                        margin: 0 auto;
                        background-color: white;
                        padding: 20px;
                        border-radius: 8px;
                        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
                    }
                    h1 {
                        color: #2c3e50;
                        text-align: center;
                        padding-bottom: 10px;
                        border-bottom: 1px solid #eee;
                    }
                    h2 {
                        color: #3498db;
                        margin-top: 30px;
                    }
                    table {
                        width: 100%;
                        border-collapse: collapse;
                        margin: 20px 0;
                    }
                    th, td {
                        padding: 12px 15px;
                        text-align: left;
                        border-bottom: 1px solid #ddd;
                    }
                    th {
                        background-color: #f2f2f2;
                        font-weight: bold;
                    }
                    tr:hover {
                        background-color: #f5f5f5;
                    }
                    .chart-container {
                        display: flex;
                        justify-content: center;
                        margin: 20px 0;
                    }
                    .summary {
                        background-color: #f8f9fa;
                        padding: 15px;
                        border-radius: 4px;
                        margin-bottom: 20px;
                    }
                    .footer {
                        text-align: center;
                        margin-top: 30px;
                        font-size: 12px;
                        color: #7f8c8d;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>$title</h1>
                    
                    <div class="summary">
                        <p><strong>Total Tracked Time:</strong> ${formatDuration(appUsage.sumOf { it.totalDuration.toLong() })}</p>
                        <p><strong>Total Applications Tracked:</strong> ${appUsage.size}</p>
                        <p><strong>Total Categories:</strong> ${categoryUsage.size}</p>
                    </div>
        """.trimIndent())
        
        // App usage chart
        sb.append("""
            <h2>Application Usage</h2>
            <div class="chart-container">
                <img src="$appChartPath" alt="Application Usage Chart">
            </div>
            
            <table>
                <tr>
                    <th>Application</th>
                    <th>Time</th>
                    <th>Percentage</th>
                </tr>
        """.trimIndent())
        
        // Calculate total time
        val totalTime = appUsage.sumOf { it.totalDuration }
        
        // Add app usage data rows
        appUsage.take(20).forEach { app ->
            val percentage = if (totalTime > 0) {
                String.format("%.1f%%", (app.totalDuration.toDouble() / totalTime) * 100)
            } else {
                "0.0%"
            }
            
            sb.append("""
                <tr>
                    <td>${app.appName}</td>
                    <td>${formatDuration(app.totalDuration.toLong())}</td>
                    <td>$percentage</td>
                </tr>
            """.trimIndent())
        }
        
        sb.append("</table>")
        
        // Category usage chart
        sb.append("""
            <h2>Category Usage</h2>
            <div class="chart-container">
                <img src="$categoryChartPath" alt="Category Usage Chart">
            </div>
            
            <table>
                <tr>
                    <th>Category</th>
                    <th>Time</th>
                    <th>Percentage</th>
                </tr>
        """.trimIndent())
        
        // Calculate total category time
        val totalCategoryTime = categoryUsage.sumOf { it.totalDuration }
        
        // Add category usage data rows
        categoryUsage.forEach { category ->
            val percentage = if (totalCategoryTime > 0) {
                String.format("%.1f%%", (category.totalDuration.toDouble() / totalCategoryTime) * 100)
            } else {
                "0.0%"
            }
            
            sb.append("""
                <tr>
                    <td>${category.category}</td>
                    <td>${formatDuration(category.totalDuration.toLong())}</td>
                    <td>$percentage</td>
                </tr>
            """.trimIndent())
        }
        
        sb.append("</table>")
        
        // HTML footer
        sb.append("""
                    <div class="footer">
                        <p>Generated by Activity Tracker on ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}</p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent())
        
        return sb.toString()
    }
}
