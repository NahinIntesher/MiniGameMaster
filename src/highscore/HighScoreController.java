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

import database.DatabaseConnection;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class HighScoreController {


    @FXML private Button playButton;
    @FXML private Button monthlyButton;
    @FXML private Button allTimeButton;
    @FXML private Label timerLabel;
    @FXML private VBox leaderboardContainer;
    @FXML private HBox backbutton;
    @FXML private GridPane gameGrid;
    @FXML private ImageView singleGameCoverPicture;
    @FXML private Label singleGameName;
    @FXML private Label singleGamePlayedBy;
    @FXML private Label singleGameScore;
    
    @FXML
    public void initialize() {
        setupButtonListeners();
        startCountdownTimer();
        loadInitialData();
        fetchGames();
        viewGameDetail(1);
    }
    
     @FXML
    private void gotoHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../home/home.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backbutton.getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("Mini Game Master");
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void viewGameDetail(int game_id) {
        DatabaseConnection databaseConnection = new DatabaseConnection();
        Connection connection = null;
        Statement statement = null;
        ResultSet result = null;

        try {
            connection = databaseConnection.getConnection();
            statement = connection.createStatement();

            String query = "SELECT g.game_id, g.game_name, g.game_cover_image, COUNT(*) played_by\r\n" + //
                "FROM games g\r\n" + //
                "JOIN game_scores gs ON g.game_id = gs.game_id\r\n" + //
                "WHERE g.game_id = " + game_id;

            result = statement.executeQuery(query);
            
            if (result.next()) {
                String gameid = result.getString("game_id");
                String game_name = result.getString("game_name");
                int played_by = result.getInt("played_by");
                byte[] game_cover_image = result.getBytes("game_cover_image");


                Platform.runLater(()->{
                    singleGameCoverPicture.setFitWidth(500);
                    singleGameCoverPicture.setFitHeight(100);
                    singleGameCoverPicture.setImage(new Image(new ByteArrayInputStream(game_cover_image)));
                    singleGameName.setText(game_name);
                    singleGamePlayedBy.setText("Played by " + played_by + " players");

                });
            } 
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
        }

    }

    private int rowI = 0;
    private int columnI = 0;

    public void fetchGames() {
       DatabaseConnection databaseConnection = new DatabaseConnection();
        Connection connection = null;
        Statement statement = null;
        ResultSet result = null;

        try {
            connection = databaseConnection.getConnection();
            statement = connection.createStatement();

            String query = "SELECT * FROM games";

            result = statement.executeQuery(query);


            
            while (result.next()) {
                String game_id = result.getString("game_id");
                String game_name = result.getString("game_name");
                byte[] game_profile_image = result.getBytes("game_profile_image");

                Platform.runLater(()->{
                    VBox gameItem = new VBox();
                    gameItem.getStyleClass().add("game-item");
                    ImageView gameIcon = new ImageView();

                    gameItem.setOnMouseClicked(e -> {
                        viewGameDetail(Integer.parseInt(game_id));
                        // System.out.println("Game " + game_name + " clicked");
                    });

                    if(columnI == 2) {
                        rowI++;
                        columnI = 0;
                    }
                    GridPane.setRowIndex(gameItem, rowI);
                    GridPane.setColumnIndex(gameItem, columnI); 
                    columnI++;
                    ByteArrayInputStream inputStream = new ByteArrayInputStream(game_profile_image);
                    Image image = new Image(inputStream);
                    gameIcon.setImage(image);
                    gameIcon.setFitWidth(80);
                    gameIcon.setFitHeight(80);
                    gameIcon.getStyleClass().add("game-icon");
                    Label gameLabel = new Label(game_name);
                    gameLabel.getStyleClass().add("game-label");
                    gameItem.getChildren().addAll(gameIcon, gameLabel);                   
                    gameGrid.getChildren().add(gameItem);

                });
            } 
            

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (result != null)
                    result.close();
                if (statement != null)
                    statement.close();
                if (connection != null)
                    connection.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
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