package auth;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LoginScreen extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Parent root = loader.load();

            // Create and style the scene
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

            // Set stage properties
            primaryStage.setTitle("Login");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            // Handle errors gracefully
            System.err.println("Error loading FXML: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
