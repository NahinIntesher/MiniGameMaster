import java.util.prefs.Preferences;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application{
    @Override
    public void start(Stage primaryStage) {
        Preferences preferences = Preferences.userRoot().node("authData");
        String userid = preferences.get("userid", "default");

        if (userid != "default") {
            try {
                // FXMLLoader loader = new FXMLLoader(getClass().getResource("livegame/livegame.fxml"));
                FXMLLoader loader = new FXMLLoader(getClass().getResource("home/home.fxml"));
                Parent root = loader.load();
    
                Scene scene = new Scene(root);
    
                primaryStage.setScene(scene);
                primaryStage.setTitle("Mini Game Master"); 
                primaryStage.show(); 
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("auth/login.fxml"));
                Parent root = loader.load();
    
                Scene scene = new Scene(root);
    
                primaryStage.setScene(scene);
                primaryStage.setTitle("Mini Game Master"); 
                primaryStage.show(); 
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        launch(args); 
    }
}