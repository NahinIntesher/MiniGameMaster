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
import javafx.concurrent.Task;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.prefs.Preferences;

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
    
    private int currentGameId = 1;
    
    @FXML
    public void initialize() {
        // setupButtonListeners();
        // startCountdownTimer();
        // loadInitialData();
        fetchGames();
        viewGameDetail(1);
        // showMonthlyScores();
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

    @FXML
    private void playGame() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("HighScoreGame.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backbutton.getScene().getWindow();
            
            HighScoreGameController highScoreGameController = loader.getController(); 
            highScoreGameController.setGameId(currentGameId);
            stage.setScene(new Scene(root));
            stage.setTitle("Mini Game Master");
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void viewGameDetail(int game_id) {
        currentGameId = game_id;
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


    @FXML
    private void showAllTimeScore() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                DatabaseConnection databaseConnection = new DatabaseConnection();
                try (Connection connection = databaseConnection.getConnection()) {
                    String query = "SELECT gs.score, u.username " +
                                   "FROM game_scores gs " +
                                   "JOIN users u ON gs.player_id = u.id " +
                                   "WHERE gs.game_id = ? " +
                                   "ORDER BY gs.score DESC";

                    try (PreparedStatement statement = connection.prepareStatement(query)) {
                        statement.setInt(1, currentGameId); // Use parameterized query to prevent SQL injection
                        try (ResultSet result = statement.executeQuery()) {
                            // Platform.runLater(() -> leaderboardContainer.getChildren().clear()); // Clear previous data
                            while (result.next()) {
                                String score = result.getString("score");
                                String username = result.getString("username");

                                Platform.runLater(() -> {
                                    Label scoreLabel = new Label(score);
                                    Label usernameLabel = new Label(username);
                                    scoreLabel.getStyleClass().add("score-label");
                                    usernameLabel.getStyleClass().add("username-label");
                                    // leaderboardContainer.getChildren().addAll(scoreLabel, usernameLabel);
                                });
                            }
                        }
                    }
                }
                return null;
            }
        };

        new Thread(task).start();
    }

    @FXML
    private void showMonthlyScore() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                DatabaseConnection databaseConnection = new DatabaseConnection();
                try (Connection connection = databaseConnection.getConnection()) {
                    String query = "SELECT gs.score, u.username " +
                                   "FROM game_scores gs " +
                                   "JOIN users u ON gs.player_id = u.id " +
                                   "WHERE gs.game_id = ? " +
                                   "AND gs.created_at >= DATE_SUB(NOW(), INTERVAL 1 MONTH) " +
                                   "ORDER BY gs.score DESC";

                    try (PreparedStatement statement = connection.prepareStatement(query)) {
                        statement.setInt(1, currentGameId); // Use parameterized query to prevent SQL injection
                        try (ResultSet result = statement.executeQuery()) {
                            // Platform.runLater(() -> leaderboardContainer.getChildren().clear()); // Clear previous data
                            while (result.next()) {
                                String score = result.getString("score");
                                String username = result.getString("username");

                                Platform.runLater(() -> {
                                    Label scoreLabel = new Label(score);
                                    Label usernameLabel = new Label(username);
                                    scoreLabel.getStyleClass().add("score-label");
                                    usernameLabel.getStyleClass().add("username-label");
                                    // leaderboardContainer.getChildren().addAll(scoreLabel, usernameLabel);
                                });
                            }
                        }
                    }
                }
                return null;
            }
        };

        new Thread(task).start();
    }


}