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

import java.io.IOException;

import javafx.collections.FXCollections;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.image.ImageView;

public class HighScoreController {
    @FXML private BorderPane borderPane;

    @FXML private Button playButton;
    @FXML private Button monthlyButton;
    @FXML private Button allTimeButton;
    @FXML private Label timerLabel;
    @FXML private VBox leaderboardContainer;
    
    @FXML
    public void initialize() {
        setupButtonListeners();
        startCountdownTimer();
        loadInitialData();
    }
    
     @FXML
    private void gotoHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../livegame/livegame.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) borderPane.getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("Mini Game Master");
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void setupButtonListeners() {
        if (playButton != null) {
            playButton.setOnAction(e -> handlePlayButton());
        }
        if (monthlyButton != null) {
            monthlyButton.setOnAction(e -> handleMonthlyButton());
        }
        if (allTimeButton != null) {
            allTimeButton.setOnAction(e -> handleAllTimeButton());
        }
    }
    
    private void handlePlayButton() {
       FXMLLoader loader = new FXMLLoader(getClass().getResource("/highscore/HighScore.fxml"));
    }
    
    private void handleMonthlyButton() {
        System.out.println("Monthly scores selected");
    }
    
    private void handleAllTimeButton() {
        System.out.println("All time scores selected");
    }
    
    private void startCountdownTimer() {
        if (timerLabel != null) {
            timerLabel.setText("28d 11h 32m");
        }
    }
    
    private void loadInitialData() {
        // Initialize with sample data
        System.out.println("Loading initial data");
    }
}