package com.leley.timesheet;

import com.leley.timesheet.cli.ConsoleApp;
import com.leley.timesheet.db.Database;

public class Main {
    public static void main(String[] args) {
        Database db = Database.fromClasspathProperties("/app.properties");
        db.initSchema();
        new ConsoleApp(db).run();
    }
}
