package auth;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.control.PasswordField;
import javafx.scene.Node;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.prefs.Preferences;

import database.DatabaseConnection;

public class LoginController {
    @FXML
    public Button loginButton;

    @FXML
    public TextField usernameField;

    @FXML
    public PasswordField passwordField;

    @FXML
    private Label loginMessageLabel;

    @FXML
    private StackPane loadingScreen;

    @FXML
    private void gotoSignupButtonAction(ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("./signup.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();

            stage.setScene(new Scene(root));
            root.requestFocus();
            stage.setTitle("Signup");
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void loginButtonOnAction(ActionEvent e) {
        if (usernameField.getText().isBlank()) {
            loginMessageLabel.setVisible(true);
            loginMessageLabel.setManaged(true);
            loginMessageLabel.setText("Please enter username!");
        } else if (passwordField.getText().isBlank()) {
            loginMessageLabel.setVisible(true);
            loginMessageLabel.setManaged(true);
            loginMessageLabel.setText("Please enter password!");
        } else {
            loadingScreen.setManaged(true);
            loadingScreen.setVisible(true);

            new Thread(() -> {
                DatabaseConnection databaseConnection = new DatabaseConnection();
                Connection connection = null;
                Statement statement = null;
                ResultSet result = null;

                try {
                    connection = databaseConnection.getConnection();
                    statement = connection.createStatement();

                    String query = "SELECT * FROM users WHERE username = '" + usernameField.getText() + "'";

                    result = statement.executeQuery(query);

                    if (result.next()) {
                        String userid = result.getString("id");
                        String password = result.getString("password");

                        if (password.equals(passwordField.getText())) {
                            Platform.runLater(() -> {
                                try {

                                    Preferences preferences = Preferences.userRoot().node("authData");
                                    preferences.put("userid", userid);

                                    FXMLLoader loader = new FXMLLoader(getClass().getResource("../Home/home.fxml"));
                                    Parent root = loader.load();

                                    Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();

                                    stage.setScene(new Scene(root));
                                    stage.setTitle("Dashboard");
                                    stage.show();
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            });
                        } else {
                            Platform.runLater(() -> {
                                loginMessageLabel.setVisible(true);
                                loginMessageLabel.setManaged(true);
                                loginMessageLabel.setText("Password does not found!");
                            });
                        }
                    } else {
                        Platform.runLater(() -> {
                            loginMessageLabel.setVisible(true);
                            loginMessageLabel.setManaged(true);
                            loginMessageLabel.setText("User not found!");
                        });
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
                    Platform.runLater(() -> {
                        loadingScreen.setManaged(false);
                        loadingScreen.setVisible(false);
                    });
                }
            }).start();
        }
    }
}