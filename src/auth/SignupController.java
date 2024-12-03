package auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.PasswordField;
import javafx.scene.Node;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.prefs.Preferences;

import database.DatabaseConnection;

public class SignupController {
    @FXML
    public Button loginButton;

    @FXML
    public TextField usernameField;

    @FXML
    public TextField emailField;

    @FXML
    public PasswordField passwordField;

    @FXML
    private Label signupMessageLabel;

    @FXML
    private void gotoLoginButtonAction(ActionEvent e){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("./login.fxml"));
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
    public void signupButtonOnAction(ActionEvent e) {
        if (usernameField.getText().isBlank()) {
            signupMessageLabel.setVisible(true);
            signupMessageLabel.setManaged(true);
            signupMessageLabel.setText("Please enter a username!");
        } else if (emailField.getText().isBlank()) {
            signupMessageLabel.setVisible(true);
            signupMessageLabel.setManaged(true);
            signupMessageLabel.setText("Please enter an email!");
        } else if (passwordField.getText().isBlank()) {
            signupMessageLabel.setVisible(true);
            signupMessageLabel.setManaged(true);
            signupMessageLabel.setText("Please enter a password!");
        } else {
            DatabaseConnection databaseConnection = new DatabaseConnection();
            Connection connection = null;
            Statement statement = null;
            ResultSet result = null;

            try {
                connection = databaseConnection.getConnection();
                statement = connection.createStatement();

                String query = "INSERT INTO users (email, username, password, trophies) " +
                    "VALUES ('" + emailField.getText() + "', '" + usernameField.getText() + "', '" + passwordField.getText() + "', 0)";

                int rowsAffected = statement.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);

                if (rowsAffected > 0) {
                    result = statement.getGeneratedKeys();
                    if (result.next()) {
                        String insertId = result.getString(1); // Retrieve the first generated key
                        
                        Preferences preferences = Preferences.userRoot().node("authData");
                        preferences.put("userid", insertId);

                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("../Home/home.fxml"));
                            Parent root = loader.load();
    
                            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
    
                            stage.setScene(new Scene(root));
                            stage.setTitle("Dashboard");
                            stage.show();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                } else {
                    signupMessageLabel.setVisible(true);
                    signupMessageLabel.setManaged(true);
                    signupMessageLabel.setText("Failed to register user.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                signupMessageLabel.setVisible(true);
                signupMessageLabel.setManaged(true);
                signupMessageLabel.setText("An error occurred while registering.");
            } finally {
                try {
                    if (result != null) result.close();
                    if (statement != null) statement.close();
                    if (connection != null) connection.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    
}