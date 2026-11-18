import java.sql.*;
import java.io.File;

public class DBConnection {

    public static Connection connect() {
        try {
            String appData = System.getenv("APPDATA");

            if (appData == null || appData.isEmpty()) {
                appData = System.getProperty("user.home");
            }

            File folder = new File(appData, "SmartExpenseTracker");

            if (!folder.exists()) {
                folder.mkdirs();
            }

            File dbFile = new File(folder, "expenses.db");

            Connection con = DriverManager.getConnection(
                "jdbc:sqlite:" + dbFile.getAbsolutePath()
            );

            Statement st = con.createStatement();

            st.execute("CREATE TABLE IF NOT EXISTS expenses (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "title TEXT," +
                    "amount REAL," +
                    "category TEXT," +
                    "date TEXT)");

            st.execute("CREATE TABLE IF NOT EXISTS settings (" +
                    "name TEXT PRIMARY KEY," +
                    "value TEXT)");

            return con;

        } catch (Exception e) {
            throw new RuntimeException("Database connection failed", e);
        }
    }
}
