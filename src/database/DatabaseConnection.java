package database;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnection {  
//    public static String serverAddress = "10.15.51.48";
    public static String serverAddress = "192.168.10.100";


    public Connection databaseLink;

    public Connection getConnection() {
        String databaseName = "minigamemaster";
        String databaseUser = "root";
        String databasePassword = "";
        String url = "jdbc:mysql://"+serverAddress+"/" + databaseName;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            databaseLink = DriverManager.getConnection(url, databaseUser, databasePassword);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    
        return databaseLink;
    }
}
