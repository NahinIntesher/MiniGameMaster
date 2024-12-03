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
    private Label loginMessageLabel;

    @FXML
    public void signupButtonOnAction(ActionEvent e) {
        if (usernameField.getText().isBlank()) {
            loginMessageLabel.setVisible(true);
            loginMessageLabel.setManaged(true);
            loginMessageLabel.setText("Please enter a username!");
        } else if (emailField.getText().isBlank()) {
            loginMessageLabel.setVisible(true);
            loginMessageLabel.setManaged(true);
            loginMessageLabel.setText("Please enter an email!");
        } else if (passwordField.getText().isBlank()) {
            loginMessageLabel.setVisible(true);
            loginMessageLabel.setManaged(true);
            loginMessageLabel.setText("Please enter a password!");
        } else {
            DatabaseConnection databaseConnection = new DatabaseConnection();
            Connection connection = null;
            Statement statement = null;

            try {
                connection = databaseConnection.getConnection();
                statement = connection.createStatement();

                String query = "INSERT INTO users (email, username, password, trophies) " +
                    "VALUES ('" + emailField.getText() + "', '" + usernameField.getText() + "', '" + passwordField.getText() + "', 0)";

                int rowsAffected = statement.executeUpdate(query);

                if (rowsAffected > 0) {
                    // System.out.println("register success");
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
                } else {
                    loginMessageLabel.setVisible(true);
                    loginMessageLabel.setManaged(true);
                    loginMessageLabel.setText("Failed to register user.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                loginMessageLabel.setVisible(true);
                loginMessageLabel.setManaged(true);
                loginMessageLabel.setText("An error occurred while registering.");
            } finally {
                try {
                    if (statement != null) statement.close();
                    if (connection != null) connection.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    
}