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
            loginMessageLabel.setText("Please enter username!");
        } else if (emailField.getText().isBlank()) {
            loginMessageLabel.setVisible(true);
            loginMessageLabel.setManaged(true);
            loginMessageLabel.setText("Please enter email!");
        } else if (passwordField.getText().isBlank()) {
            loginMessageLabel.setVisible(true);
            loginMessageLabel.setManaged(true);
            loginMessageLabel.setText("Please enter password!");
        } else {
            DatabaseConnection databaseConnection = new DatabaseConnection();

            Connection connection = databaseConnection.getConnection();

            String query = "INSERT INTO Customers (email, ContactName, Address, City, PostalCode, Country)\r\n" + //
                                "VALUES ('Cardinal', 'Tom B. Erichsen', 'Skagen 21', 'Stavanger', '4006', 'Norway');" + usernameField.getText() + "'";
            try {
                Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery(query);

                if (result.next()) {
                    String username = result.getString("username");
                    String password = result.getString("password");

                    System.out.println(password);
                    System.out.println(passwordField.getText());

                    if (password.equals(passwordField.getText())) {
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
                        loginMessageLabel.setText("Password does not found!");
                    }
                } else {
                    loginMessageLabel.setVisible(true);
                    loginMessageLabel.setManaged(true);
                    loginMessageLabel.setText("User not found!");
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}