module com.dat.activity_tracker {
    requires kotlin.stdlib;
    requires java.desktop;
    requires java.logging;
    requires java.sql;
    requires org.json;
    requires org.jetbrains.skiko;
    requires kotlinx.coroutines.core;
    requires org.jfree.jfreechart;
    requires com.sun.jna;
    requires com.sun.jna.platform;
    requires org.jetbrains.compose.runtime;

    exports com.dat.activity_tracker;
    exports com.dat.activity_tracker.data;
    exports com.dat.activity_tracker.monitor;
    exports com.dat.activity_tracker.report;
    exports com.dat.activity_tracker.ui;
    exports com.dat.activity_tracker.util;
    
    opens com.dat.activity_tracker to org.jetbrains.compose.runtime;
}