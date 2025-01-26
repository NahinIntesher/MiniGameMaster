// File: HighScoreHome.java
package adventure;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Adventure extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // Load FXML file from the same package
            // FXMLLoader loader = new FXMLLoader(getClass().getResource("/adventure/adventure.fxml"));
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/adventure/adventureGame.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            
            primaryStage.setTitle("Mini Game Master");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}