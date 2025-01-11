// File: HighScoreController.java
package highscore;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.collections.ObservableList;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.prefs.Preferences;

import database.DatabaseConnection;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import highscore.games.HighScoreGame;

public class HighScoreGameController {

    @FXML
    Button backButton;
    @FXML
    StackPane playground;
    @FXML
    BorderPane parentPane;

    public int gameId = 1;

    public void setGameId(int gameId) {
        this.gameId = gameId;
    };

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            Scene primaryScene = parentPane.getScene();
            System.out.println(gameId);
            HighScoreGame gameInstance = HighScoreGame.getGameInstance(gameId);

            playground.getChildren().add(gameInstance);
            playground.requestFocus();

            playground.setOnKeyPressed(event -> {
                gameInstance.actionOnKeyPressed(event.getCode().toString());
                event.consume();
            });

            playground.setOnKeyReleased(event -> {
                gameInstance.actionOnKeyReleased(event.getCode().toString());
                event.consume();
            });
        });
    }

    @FXML
    private void gotoBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../home/home.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("Mini Game Master");
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}