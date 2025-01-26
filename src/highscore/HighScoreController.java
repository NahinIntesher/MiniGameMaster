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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class HighScoreController {

    @FXML
    private Button playButton;
    @FXML
    private Button monthlyButton;
    @FXML
    private Button allTimeButton;
    @FXML
    private Label timerLabel;
    @FXML
    private VBox leaderboard;
    @FXML
    private HBox backbutton;
    @FXML
    private GridPane gameGrid;
    @FXML
    private StackPane singleGameCoverPictureContainer;
    @FXML
    private ImageView singleGameCoverPicture;
    @FXML
    private Label singleGameName;
    @FXML
    private Label singleGamePlayedBy;
    @FXML
    private Label singleGameScore;

    private int currentGameId = 1;

    List<VBox> gameItems = new ArrayList<>();

    @FXML
    public void initialize() {
        fetchGames();
        viewGameDetail(1);
        timeLeft();
    }

    public void timeLeft() {
        new Thread(() -> {
            Timer timer = new Timer();

            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    // Get current date and time
                    LocalDateTime now = LocalDateTime.now();

                    // Calculate the start of the next month
                    YearMonth currentMonth = YearMonth.from(now);
                    YearMonth nextMonth = currentMonth.plusMonths(1);
                    LocalDateTime startOfNextMonth = nextMonth.atDay(1).atStartOfDay();

                    // Calculate the remaining time
                    Duration duration = Duration.between(now, startOfNextMonth);

                    long totalSeconds = duration.getSeconds();
      
                    // Convert remaining time into days, hours, minutes, and seconds
                    long days = totalSeconds / (24 * 3600);
                    long hours = (totalSeconds % (24 * 3600)) / 3600;
                    long minutes = (totalSeconds % 3600) / 60;
                    long seconds = totalSeconds % 60;

                    // Display the countdown
                    Platform.runLater(()->{
                        timerLabel.setText(String.format("%02dd %02dh %02dm %02ds", days, hours, minutes, seconds));
                    });
                }
            };

            // Schedule the task to run every second
            timer.scheduleAtFixedRate(task, 0, 1000);
        }).start();
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
    private void playGame() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("HighScoreGame.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backbutton.getScene().getWindow();

            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());
            stage.setMaximized(true);

            HighScoreGameController highScoreGameController = loader.getController();
            highScoreGameController.setGameId(currentGameId);
            stage.setScene(new Scene(root));
            stage.setTitle("Mini Game Master");
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

                Platform.runLater(() -> {
                    singleGameCoverPicture.fitWidthProperty().bind(singleGameCoverPictureContainer.widthProperty());
                    singleGameCoverPicture.fitHeightProperty().bind(singleGameCoverPictureContainer.heightProperty());
                    singleGameCoverPicture.setPreserveRatio(false); // Maintain aspect ratio
                    singleGameCoverPicture.setSmooth(true);

                    singleGameCoverPicture.setImage(new Image(new ByteArrayInputStream(game_cover_image)));
                    singleGameName.setText(game_name);
                    singleGamePlayedBy.setText("Played by " + played_by + " players");

                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
        }
        showAllTimeScore();
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

                Platform.runLater(() -> {
                    VBox gameItem = new VBox();
                    gameItem.getStyleClass().add("game-item");
                    if (game_id.equals("1")) {
                        gameItem.getStyleClass().add("game-item-active");
                    }
                    StackPane gameIconPane = new StackPane();
                    gameIconPane.getStyleClass().add("game-item-icon-pane");
                    ImageView gameIcon = new ImageView();

                    gameItem.setOnMouseClicked(e -> {
                        for (VBox otherGameItem : gameItems) {
                            otherGameItem.getStyleClass().remove("game-item-active");
                        }
                        // Add "active-tab" class to the clicked button
                        gameItem.getStyleClass().add("game-item-active");

                        viewGameDetail(Integer.parseInt(game_id));
                        // System.out.println("Game " + game_name + " clicked");
                    });

                    if (columnI == 2) {
                        rowI++;
                        columnI = 0;
                    }
                    GridPane.setRowIndex(gameItem, rowI);
                    GridPane.setColumnIndex(gameItem, columnI);
                    columnI++;
                    ByteArrayInputStream inputStream = new ByteArrayInputStream(game_profile_image);
                    Image image = new Image(inputStream);
                    gameIcon.setImage(image);
                    gameIcon.setFitWidth(120);
                    gameIcon.setFitHeight(120);
                    Rectangle clip = new Rectangle(120, 120);
                    clip.setArcWidth(10);
                    clip.setArcHeight(10);
                    gameIcon.setClip(clip);

                    Label gameLabel = new Label(game_name);
                    gameLabel.getStyleClass().add("game-item-label");
                    gameIconPane.getChildren().add(gameIcon);
                    gameItem.getChildren().addAll(gameIconPane, gameLabel);
                    gameItems.add(gameItem);
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
        monthlyButton.getStyleClass().remove("tab-active");
        allTimeButton.getStyleClass().remove("tab-active");
        allTimeButton.getStyleClass().add("tab-active");

        DatabaseConnection databaseConnection = new DatabaseConnection();
        try (Connection connection = databaseConnection.getConnection()) {
            String query = "SELECT gs.score, u.username, u.trophies " +
                    "FROM game_scores gs " +
                    "JOIN users u ON gs.player_id = u.id " +
                    "WHERE gs.game_id = ? " +
                    "GROUP BY u.username " +
                    "ORDER BY gs.score DESC";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, currentGameId); // Use parameterized query to prevent SQL injection
                try (ResultSet result = statement.executeQuery()) {
                    // Platform.runLater(() -> leaderboardContainer.getChildren().clear()); // Clear
                    // previous data
                    int rank = 1;
                    Platform.runLater(() -> {
                        leaderboard.getChildren().clear();
                    });

                    System.out.println("outer game id: " + currentGameId);

                    while (result.next()) {
                        String score = result.getString("score");
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

                        Label scoreLabel = new Label(score);
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
    }

    @FXML
    private void showMonthlyScore() {
        monthlyButton.getStyleClass().remove("tab-active");
        allTimeButton.getStyleClass().remove("tab-active");
        monthlyButton.getStyleClass().add("tab-active");

        DatabaseConnection databaseConnection = new DatabaseConnection();
        try (Connection connection = databaseConnection.getConnection()) {
            String query = "SELECT gs.score, u.username, u.trophies " +
                    "FROM game_scores gs " +
                    "JOIN users u ON gs.player_id = u.id " +
                    "WHERE gs.game_id = ? " +
                    "AND gs.created_at >= DATE_SUB(NOW(), INTERVAL 1 MONTH) " +
                    "GROUP BY u.username " +
                    "ORDER BY gs.score DESC";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, currentGameId); // Use parameterized query to prevent SQL injection
                try (ResultSet result = statement.executeQuery()) {
                    // Platform.runLater(() -> leaderboardContainer.getChildren().clear()); // Clear
                    // previous data
                    int rank = 1;
                    Platform.runLater(() -> {
                        leaderboard.getChildren().clear();
                    });

                    System.out.println("outer game id: " + currentGameId);

                    while (result.next()) {
                        String score = result.getString("score");
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

                        Label scoreLabel = new Label(score);
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
    }
}