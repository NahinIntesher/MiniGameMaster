// File: HighScoreController.java
package highscore;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.collections.ObservableList;

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
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import highscore.games.HighScoreGame;

public class HighScoreGameController {
    Preferences preferences = Preferences.userRoot().node("authData");
    String userid = preferences.get("userid", "default");

    @FXML
    private Text scoreText;
    @FXML
    private StackPane victoryScreen;
    @FXML
    private Label victoryScreenTitle;
    @FXML
    private Text victoryScreenSemiTitle;
    @FXML
    private Button victoryScreenBackButton;
    @FXML
    private Button playAgainButton;
    @FXML
    private Button monthlyButton;
    @FXML
    private Button allTimeButton;
    @FXML
    Button backbutton;
    @FXML
    StackPane playground;
    @FXML
    BorderPane parentPane;
    @FXML
    private VBox leaderboard;
    @FXML
    private Label headerText;

    private HighScoreGame gameInstance;

    public int gameId = 1;

    public void setGameId(int gameId) {
        this.gameId = gameId;
    };

    @FXML
    public void initialize() {
        allTimeButton.setFocusTraversable(false);
        backbutton.setFocusTraversable(false);
        monthlyButton.setFocusTraversable(false);
        victoryScreenBackButton.setFocusTraversable(false);
        playAgainButton.setFocusTraversable(false);

        Platform.runLater(() -> {
            headerText.setText(HighScoreGame.getGameTitle(gameId));
            Scene primaryScene = parentPane.getScene();
            System.out.println(gameId);
            gameInstance = HighScoreGame.getGameInstance(gameId, this);

            playground.getChildren().add(gameInstance);
            playground.requestFocus();

            primaryScene.setOnKeyPressed(event -> {
                gameInstance.actionOnKeyPressed(event.getCode().toString());
                event.consume();
            });

            primaryScene.setOnKeyReleased(event -> {
                gameInstance.actionOnKeyReleased(event.getCode().toString());
                event.consume();
            });

            showAllTimeScore();
        });
    }

    @FXML
    private void playAgain() {
        System.out.println("choling");
        victoryScreen.setManaged(false);
        victoryScreen.setVisible(false);
        Platform.runLater(() -> {
            gameInstance.restartGame();
        });
    }

    public void gameOver(int score) {
        scoreText.setText(String.valueOf(score));
        victoryScreen.setManaged(true);
        victoryScreen.setVisible(true);

        new Thread(()->{
            DatabaseConnection databaseConnection = new DatabaseConnection();
            try (Connection connection = databaseConnection.getConnection()) {
                String query = "INSERT INTO game_scores(game_id, player_id, score) VALUES (?,?,?)";
    
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setInt(1, gameId);                    
                    statement.setInt(2, Integer.parseInt(userid));
                    statement.setInt(3, score); // Use parameterized query to prevent SQL injection
                    try {
                        statement.executeUpdate();
                    } catch (Exception ex) {
                        //ex.printStackTrace();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    public void gameOver(String time) {
        scoreText.setText(time);
        victoryScreenTitle.setText("Game Complete");
        victoryScreenSemiTitle.setText("Taken Time");
        victoryScreen.setManaged(true);
        victoryScreen.setVisible(true);
        new Thread(()->{
            int score = 1000-convertToSeconds(time);
            DatabaseConnection databaseConnection = new DatabaseConnection();
            try (Connection connection = databaseConnection.getConnection()) {
                String query = "INSERT INTO game_scores(game_id, player_id, score, time) VALUES (?,?,?,?)";
    
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setInt(1, gameId);                    
                    statement.setInt(2, Integer.parseInt(userid));
                    statement.setInt(3, score); 
                    statement.setString(4, time);// Use parameterized query to prevent SQL injection
                    try {
                        statement.executeUpdate();
                    } catch (Exception ex) {
                        //ex.printStackTrace();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void gotoBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../highscore/HighScore.fxml"));
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

    public int convertToSeconds(String time) {
        // Split the input string into minutes and seconds
        String[] parts = time.split(":");
        int minutes = Integer.parseInt(parts[0]);
        int seconds = Integer.parseInt(parts[1]);

        // Convert the time to total seconds
        return (minutes * 60) + seconds;
    }

    @FXML
    private void gotoHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../highscore/HighScore.fxml"));
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
    private void showAllTimeScore() {
        monthlyButton.getStyleClass().remove("tab-active");
        allTimeButton.getStyleClass().remove("tab-active");
        allTimeButton.getStyleClass().add("tab-active");

        new Thread(() -> {
            DatabaseConnection databaseConnection = new DatabaseConnection();
            try (Connection connection = databaseConnection.getConnection()) {
                String query = "SELECT gs.score, gs.time, u.username, u.trophies " +
                        "FROM game_scores gs " +
                        "JOIN users u ON gs.player_id = u.id " +
                        "WHERE gs.game_id = ? " +
                        "GROUP BY u.username " +
                        "ORDER BY gs.score DESC";

                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setInt(1, gameId); // Use parameterized query to prevent SQL injection
                    try (ResultSet result = statement.executeQuery()) {
                        // Platform.runLater(() -> leaderboardContainer.getChildren().clear()); // Clear
                        // previous data
                        int rank = 1;
                        Platform.runLater(() -> {
                            leaderboard.getChildren().clear();
                        });

                        System.out.println("outer game id: " + gameId);

                        while (result.next()) {
                            String score = result.getString("score");
                            String time = result.getString("time");
                            String username = result.getString("username");
                            String trophies = result.getString("trophies");

                            GridPane scoreBox = new GridPane();
                            scoreBox.getStyleClass().add("score-entry");

                            HBox scoreHBox = new HBox();

                            scoreHBox.getStyleClass().add("score-entry-hbox");

                            Label rankLabel = new Label(String.valueOf(rank));
                            rankLabel.getStyleClass().add("score-entry-rank");

                            HBox scoreVbox = new HBox();
                            scoreVbox.getStyleClass().add("score-entry-player-container");

                            Label usernameLabel = new Label(username);
                            usernameLabel.getStyleClass().add("score-entry-player-name");

                            HBox trophyHBox = new HBox();
                            trophyHBox.getStyleClass().add("score-entry-player-trophy");
                            Text trophyText = new Text("");
                            trophyText.getStyleClass().add("score-entry-player-trophy-icon");
                            Label trophyLabel = new Label(trophies);
                            trophyLabel.getStyleClass().add("score-entry-player-trophy-text");
                            trophyHBox.getChildren().addAll(trophyText, trophyLabel);
                            scoreVbox.getChildren().addAll(usernameLabel, trophyHBox);

                            scoreHBox.getChildren().addAll(rankLabel, scoreVbox);

                            HBox scoreLabelContainer = new HBox();
                            scoreLabelContainer.getStyleClass().add("score-entry-scores-container");

                            Label scoreLabel = new Label();
                            if (time != null) {
                                scoreLabel.setText(time);
                            } else {
                                scoreLabel.setText(score);
                            }

                            scoreLabel.getStyleClass().add("score-entry-scores");
                            scoreLabelContainer.getChildren().addAll(scoreLabel);

                            GridPane.setColumnIndex(scoreHBox, 0);
                            GridPane.setColumnIndex(scoreLabelContainer, 1);

                            ColumnConstraints colCons = new ColumnConstraints();
                            colCons.setHgrow(Priority.SOMETIMES);
                            colCons.setMinWidth(Double.NEGATIVE_INFINITY);

                            scoreBox.getColumnConstraints().add(colCons);
                            scoreBox.getChildren().addAll(scoreHBox, scoreLabelContainer);

                            Platform.runLater(() -> {
                                leaderboard.getChildren().add(scoreBox);
                            });

                            rank++;
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void showMonthlyScore() {
        monthlyButton.getStyleClass().remove("tab-active");
        allTimeButton.getStyleClass().remove("tab-active");
        monthlyButton.getStyleClass().add("tab-active");

        new Thread(() -> {
            DatabaseConnection databaseConnection = new DatabaseConnection();
            try (Connection connection = databaseConnection.getConnection()) {
                String query = "SELECT gs.score, gs.time, u.username, u.trophies " +
                        "FROM game_scores gs " +
                        "JOIN users u ON gs.player_id = u.id " +
                        "WHERE gs.game_id = ? " +
                        "AND gs.created_at >= DATE_SUB(NOW(), INTERVAL 1 MONTH) " +
                        "GROUP BY u.username " +
                        "ORDER BY gs.score DESC";

                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setInt(1, gameId); // Use parameterized query to prevent SQL injection
                    try (ResultSet result = statement.executeQuery()) {
                        // Platform.runLater(() -> leaderboardContainer.getChildren().clear()); // Clear
                        // previous data
                        int rank = 1;
                        Platform.runLater(() -> {
                            leaderboard.getChildren().clear();
                        });

                        System.out.println("outer game id: " + gameId);

                        while (result.next()) {
                            String score = result.getString("score");
                            String time = result.getString("time");
                            String username = result.getString("username");
                            String trophies = result.getString("trophies");

                            GridPane scoreBox = new GridPane();
                            scoreBox.getStyleClass().add("score-entry");

                            HBox scoreHBox = new HBox();

                            scoreHBox.getStyleClass().add("score-entry-hbox");

                            Label rankLabel = new Label(String.valueOf(rank));
                            rankLabel.getStyleClass().add("score-entry-rank");

                            HBox scoreVbox = new HBox();
                            scoreVbox.getStyleClass().add("score-entry-player-container");

                            Label usernameLabel = new Label(username);
                            usernameLabel.getStyleClass().add("score-entry-player-name");

                            HBox trophyHBox = new HBox();
                            trophyHBox.getStyleClass().add("score-entry-player-trophy");
                            Text trophyText = new Text("");
                            trophyText.getStyleClass().add("score-entry-player-trophy-icon");
                            Label trophyLabel = new Label(trophies);
                            trophyLabel.getStyleClass().add("score-entry-player-trophy-text");
                            trophyHBox.getChildren().addAll(trophyText, trophyLabel);
                            scoreVbox.getChildren().addAll(usernameLabel, trophyHBox);

                            scoreHBox.getChildren().addAll(rankLabel, scoreVbox);

                            HBox scoreLabelContainer = new HBox();
                            scoreLabelContainer.getStyleClass().add("score-entry-scores-container");
                            Label scoreLabel = new Label();
                            if (time != null) {
                                scoreLabel.setText(time);
                            } else {
                                scoreLabel.setText(score);
                            }

                            scoreLabel.getStyleClass().add("score-entry-scores");
                            scoreLabelContainer.getChildren().addAll(scoreLabel);

                            GridPane.setColumnIndex(scoreHBox, 0);
                            GridPane.setColumnIndex(scoreLabelContainer, 1);

                            ColumnConstraints colCons = new ColumnConstraints();
                            colCons.setHgrow(Priority.SOMETIMES);
                            colCons.setMinWidth(Double.NEGATIVE_INFINITY);

                            scoreBox.getColumnConstraints().add(colCons);
                            scoreBox.getChildren().addAll(scoreHBox, scoreLabelContainer);

                            Platform.runLater(() -> {
                                leaderboard.getChildren().add(scoreBox);
                            });

                            rank++;
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }
}