package home;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.prefs.Preferences;

import database.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class HomeController {
    @FXML
    private Label playerUsername;

    @FXML
    private Label playerTrophies;

    @FXML
    private Label playerGolds;

    @FXML
    public void initialize() {
        Preferences preferences = Preferences.userRoot().node("authData");
        String userid = preferences.get("userid", "default");

        DatabaseConnection databaseConnection = new DatabaseConnection();
        Connection connection = null;
        Statement statement = null;
        ResultSet result = null;

        try {
            connection = databaseConnection.getConnection();
            statement = connection.createStatement();

            String query = "SELECT * FROM users WHERE id = " + userid;

            System.out.println(query);

            result = statement.executeQuery(query);

            if (result.next()) {
                String username = result.getString("username");
                String trophies = result.getString("trophies");
                String golds = result.getString("golds");

                playerUsername.setText(username);
                playerTrophies.setText(trophies);
                playerGolds.setText(golds);
            } else {
                System.out.println("Error while getting player data!");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (result != null)
                    result.close();
                if (statement != null)
                    statement.close();
                if (connection != null)
                    connection.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}