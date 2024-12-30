package home;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.prefs.Preferences;

import database.DatabaseConnection;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class HomeController {
    @FXML
    private BorderPane borderPane;

    @FXML
    private Label playerUsername;

    @FXML
    private Label playerTrophies;

    @FXML
    private Label playerGolds;

    @FXML
    private StackPane loadingScreen;

    Preferences preferences = Preferences.userRoot().node("authData");

    @FXML
    private void logoutButtonAction(ActionEvent e) {
        preferences.remove("userid");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../auth/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();

            stage.setScene(new Scene(root));
            root.requestFocus();
            stage.setTitle("Login");
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void liveGameButtonAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../livegame/livegame.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) borderPane.getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("Mini Game Master");
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        String userid = preferences.get("userid", "default");

        if (userid != "default") {
            new Thread(()->{
                DatabaseConnection databaseConnection = new DatabaseConnection();
                Connection connection = null;
                Statement statement = null;
                ResultSet result = null;
    
                try {
                    connection = databaseConnection.getConnection();
                    statement = connection.createStatement();
    
                    String query = "SELECT * FROM users WHERE id = " + userid;
    
                    result = statement.executeQuery(query);
    
                    if (result.next()) {
                        String username = result.getString("username");
                        String trophies = result.getString("trophies");
                        String golds = result.getString("golds");
    
                        Platform.runLater(()->{
                            playerUsername.setText(username);
                            preferences.put("username", username);
                            playerTrophies.setText(trophies);
                            preferences.put("trophies", trophies);
                            playerGolds.setText(golds);
                            preferences.put("golds", golds);

                            loadingScreen.setManaged(false);
                            loadingScreen.setVisible(false);
                        });
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
            }).start();

        } else {
            Platform.runLater(() -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("../auth/login.fxml"));
                    Parent root = loader.load();
    
                    Stage stage = (Stage) borderPane.getScene().getWindow();
    
                    stage.setScene(new Scene(root));
                    root.requestFocus();
                    stage.setTitle("Login");
                    stage.show();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
        }
    }
}