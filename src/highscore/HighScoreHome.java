// File: HighScoreHome.java
package highscore;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HighScoreHome extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // Load FXML file from the same package
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/highscore/HighScore.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            
            primaryStage.setTitle("Game High Scores");
            primaryStage.setScene(scene);
            primaryStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}