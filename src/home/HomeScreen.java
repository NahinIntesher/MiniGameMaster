package home;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HomeScreen extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("home.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);

            scene.getStylesheets().add(getClass().getResource("homeStyle.css").toExternalForm());

            primaryStage.setScene(scene);
            primaryStage.setTitle("Mini Game Master"); 
            primaryStage.show(); 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args); 
    }
}
