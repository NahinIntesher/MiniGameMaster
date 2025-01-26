// File: HighScoreController.java
package adventure;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.prefs.Preferences;

import database.DatabaseConnection;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class AdventureController {
    @FXML
    private HBox backbutton;

    private Circle activeLevelDot;
    
    @FXML 
    private StackPane root;
    @FXML
    private AnchorPane levelMapAnchorPane;
    @FXML
    private Label levelCount;    
    @FXML
    private Label levelName;

    @FXML
    private Button prevLevelButton;
    @FXML
    private Button nextLevelButton;

    private int currentLevel = 1;
    private List<Level> levels = new ArrayList<>();
    private List<VBox> gameItems = new ArrayList<>();

    @FXML
    public void initialize() {
        levels.add(new Level("Welcome to Arcade World", 150, 310));
        levels.add(new Level("Explore the Arcade World", 330, 250));
        levels.add(new Level("Find Way of Scape", 530, 210));
        levels.add(new Level("Still on the Mission", 710, 105));
        levels.add(new Level("Developing Selfconfidence", 915, 150));
        levels.add(new Level("Near the Lights", 840, 310));
        levels.add(new Level("Path to the Freedom", 700, 425));

        for (Level level : levels) {
            Circle levelDot = new Circle(10);
            levelDot.getStyleClass().add("level-dot");
            levelDot.setLayoutX(level.mapPositionX);
            levelDot.setLayoutY(level.mapPositionY);
            Platform.runLater(() -> {
                levelMapAnchorPane.getChildren().add(levelDot);
            });
        }
        activeLevelDot = new Circle(18);
        activeLevelDot.getStyleClass().add("level-dot-active");
        activeLevelDot.setLayoutX(levels.get(currentLevel - 1).mapPositionX);
        activeLevelDot.setLayoutY(levels.get(currentLevel - 1).mapPositionY);
        Platform.runLater(() -> {
            levelMapAnchorPane.getChildren().add(activeLevelDot);
            levelCount.setText("Level "+currentLevel);
            levelName.setText(levels.get(currentLevel - 1).name);
        });

        if(currentLevel == 1) {
            prevLevelButton.getStyleClass().add("inactive");
        }
        else {
            prevLevelButton.getStyleClass().remove("inactive");
        }
        if(currentLevel == levels.size()) {
            nextLevelButton.getStyleClass().add("inactive");
        }
        else {
            nextLevelButton.getStyleClass().remove("inactive");
        }

        prevLevelButton.setFocusTraversable(false);
        nextLevelButton.setFocusTraversable(false);
        backbutton.setFocusTraversable(false);

        Platform.runLater(()->{
            Scene primaryScene = levelMapAnchorPane.getScene();


            root.setOnKeyPressed(event -> {
                switch (event.getCode()) {
                    case LEFT:
                        prevLevel();
                        break;
    
                    case RIGHT:
                        nextLevel();
                        break;
    
                    default:
                        break;
                }
            });

            primaryScene.setOnMouseClicked(event -> root.requestFocus());
            root.requestFocus();
        });
    }

    @FXML
    private void gotoHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../home/home.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backbutton.getScene().getWindow();

            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());
            stage.setMaximized(true);

            stage.setScene(new Scene(root));
            stage.setTitle("Mini Game Master");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    @FXML
    private void startGame() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../adventure/adventureGame.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backbutton.getScene().getWindow();

            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());
            stage.setMaximized(true);

            stage.setScene(new Scene(root));
            stage.setTitle("Mini Game Master");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void nextLevel() {
        if (currentLevel < levels.size()) {
            currentLevel++;
            activeLevelDot.setLayoutX(levels.get(currentLevel - 1).mapPositionX);
            activeLevelDot.setLayoutY(levels.get(currentLevel - 1).mapPositionY);
            Platform.runLater(() -> {
                levelCount.setText("Level "+currentLevel);
                levelName.setText(levels.get(currentLevel - 1).name);
                if(currentLevel == 1) {
                    prevLevelButton.getStyleClass().add("inactive");
                }
                else {
                    prevLevelButton.getStyleClass().remove("inactive");
                }
                if(currentLevel == levels.size()) {
                    nextLevelButton.getStyleClass().add("inactive");
                }
                else {
                    nextLevelButton.getStyleClass().remove("inactive");
                }
            });
        }
    }

    @FXML
    private void prevLevel() {
        if (currentLevel > 1) {
            currentLevel--;
            activeLevelDot.setLayoutX(levels.get(currentLevel - 1).mapPositionX);
            activeLevelDot.setLayoutY(levels.get(currentLevel - 1).mapPositionY);
            Platform.runLater(() -> {
                levelCount.setText("Level "+currentLevel);
                levelName.setText(levels.get(currentLevel - 1).name);
                if(currentLevel == 1) {
                    prevLevelButton.getStyleClass().add("inactive");
                }
                else {
                    prevLevelButton.getStyleClass().remove("inactive");
                }
                if(currentLevel == levels.size()) {
                    nextLevelButton.getStyleClass().add("inactive");
                }
                else {
                    nextLevelButton.getStyleClass().remove("inactive");
                }
            });
        }
    }

}

class Level {
    String name;
    int mapPositionX;
    int mapPositionY;

    public Level(String name, int mapPositionX, int mapPositionY) {
        this.name = name;
        this.mapPositionX = mapPositionX;
        this.mapPositionY = mapPositionY;
    }
}