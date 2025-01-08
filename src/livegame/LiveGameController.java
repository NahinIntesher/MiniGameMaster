package livegame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;
import java.util.prefs.Preferences;

import org.json.JSONArray;
import org.json.JSONObject;

import database.DatabaseConnection;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class LiveGameController {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    String playerToken = UUID.randomUUID().toString();
    String roomId;

    String[] games = new String[3];
    JSONObject[] gamesInitInfo = new JSONObject[3];

    boolean battleRunning = false;

    int minutes = 0;
    int seconds = 0;

    int startCount;
    String messageType;
    JSONObject messageObject;

    Preferences preferences = Preferences.userRoot().node("authData");
    
    private String userid = preferences.get("userid", "default");

    private String playerBlueNameData = preferences.get("username", "default");
    private String playerBlueTrophiesData = preferences.get("trophies", "default");;
    private String playerRedNameData;
    private String playerRedTrophiesData;

    @FXML
    private StackPane findingPlayerContainer;

    @FXML
    private StackPane runningGameContainer;

    @FXML
    private StackPane playerBluePlayground;

    @FXML
    private StackPane playerRedPlayground;

    @FXML
    private StackPane startingInContainer;

    @FXML
    private Text startingInCount;

    @FXML
    private Label battleTimer;

    @FXML
    private StackPane victoryContainer;

    @FXML
    private Label victoryLabel;
    @FXML
    private Label victoryAddedTrophy;

    @FXML
    private Label playerBlueName;
    @FXML
    private Label playerBlueTrophies;
    @FXML
    private Label startingInScreenPlayerBlueName;
    @FXML
    private Label startingInScreenPlayerBlueTrophy;

    @FXML
    private Pane playerBlueProgressPoint1;
    @FXML
    private Pane playerBlueProgressPoint2;
    @FXML
    private Pane playerBlueProgressPoint3;
    @FXML
    private Pane playerBlueProgressPoint4;

    @FXML
    private Pane playerBlueProgressStick1;
    @FXML
    private Pane playerBlueProgressStick2;
    @FXML
    private Pane playerBlueProgressStick3;

    @FXML
    private Label playerBlueProgressLabel1;
    @FXML
    private Label playerBlueProgressLabel2;
    @FXML
    private Label playerBlueProgressLabel3;
    @FXML
    private Label playerBlueProgressLabel4;

    @FXML
    private Label playerRedName;
    @FXML
    private Label playerRedTrophies;
    @FXML
    private Label startingInScreenPlayerRedName;
    @FXML
    private Label startingInScreenPlayerRedTrophy;

    @FXML
    private Pane playerRedProgressPoint1;
    @FXML
    private Pane playerRedProgressPoint2;
    @FXML
    private Pane playerRedProgressPoint3;
    @FXML
    private Pane playerRedProgressPoint4;

    @FXML
    private Pane playerRedProgressStick1;
    @FXML
    private Pane playerRedProgressStick2;
    @FXML
    private Pane playerRedProgressStick3;

    @FXML
    private Label playerRedProgressLabel1;
    @FXML
    private Label playerRedProgressLabel2;
    @FXML
    private Label playerRedProgressLabel3;
    @FXML
    private Label playerRedProgressLabel4;

    private String playerBlueGameTime1 = null;
    private String playerBlueGameTime2 = null;
    private String playerBlueGameTime3 = null;

    private String playerRedGameTime1 = null;
    private String playerRedGameTime2 = null;
    private String playerRedGameTime3 = null;

    @FXML
    private Label victoryScreenGameName1;
    @FXML
    private Label victoryScreenGameName2;
    @FXML
    private Label victoryScreenGameName3;

    @FXML
    private Label victoryScreenPlayerBlueName;
    @FXML
    private Label victoryScreenPlayerBlueTrophy;
    @FXML
    private Label victoryScreenPlayerBlueGame1Time;
    @FXML
    private Label victoryScreenPlayerBlueGame2Time;
    @FXML
    private Label victoryScreenPlayerBlueGame3Time;

    @FXML
    private Label victoryScreenPlayerRedName; 
    @FXML
    private Label victoryScreenPlayerRedTrophy;
    @FXML
    private Label victoryScreenPlayerRedGame1Time;
    @FXML
    private Label victoryScreenPlayerRedGame2Time;
    @FXML
    private Label victoryScreenPlayerRedGame3Time;

    @FXML
    public void initialize() {
        Platform.runLater(()->{
            Stage stage = (Stage) ((Node) playerRedPlayground).getScene().getWindow();

            stage.setOnCloseRequest((event) -> {
                if(socket != null) {
                    out.println("matchEnd:"+playerToken);
                    out.println("playerLeft:"+playerToken);
                }
            });
        });

        try {
            String serverAddress = DatabaseConnection.serverAddress;
            socket = new Socket(serverAddress, 12345); // Connect to the server
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println("findRoom");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // System.out.println(playerToken);
        try {
            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        System.out.println("Server says: " + message);
                        if (message.startsWith("{")) {
                            messageObject = new JSONObject(message);

                            if (messageObject.get("type").equals("gameInitialize")) {
                                JSONArray games = messageObject.getJSONArray("games");
                                JSONArray gamesInitInfo = messageObject.getJSONArray("gamesInitInfo");

                                for (int i = 0; i < games.length(); i++) {
                                    this.games[i] = games.getString(i);
                                }

                                for (int i = 0; i < gamesInitInfo.length(); i++) {
                                    this.gamesInitInfo[i] = gamesInitInfo.getJSONObject(i);
                                }
                            }
                        } else {
                            messageType = message.split(":")[0];
                            if (messageType.equals("setRoomId")) {
                                roomId = message.split(":")[1];
                            } else if (messageType.equals("startGame")) {
                                out.println("playerDetails:" + playerToken + ":" + playerBlueNameData + ":"
                                        + playerBlueTrophiesData);
                            } else if (messageType.equals("playerDetails")) {
                                String getPlayerToken = message.split(":")[1];

                                if (!getPlayerToken.equals(playerToken)) {
                                    playerRedNameData = message.split(":")[2];
                                    playerRedTrophiesData = message.split(":")[3];

                                    Platform.runLater(() -> {
                                        playerBlueName.setText(playerBlueNameData);
                                        playerBlueTrophies.setText(playerBlueTrophiesData);
                                        startingInScreenPlayerBlueName.setText(playerBlueNameData);
                                        startingInScreenPlayerBlueTrophy.setText(playerBlueTrophiesData);

                                        playerRedName.setText(playerRedNameData);
                                        playerRedTrophies.setText(playerRedTrophiesData);
                                        startingInScreenPlayerRedName.setText(playerRedNameData);
                                        startingInScreenPlayerRedTrophy.setText(playerRedTrophiesData);
                                    });

                                    startCountdown();
                                    startGame();
                                }
                            } else if (messageType.equals("gameComplete")) {
                                String getPlayerToken = message.split(":")[1];

                                if (getPlayerToken.equals(playerToken)) {
                                    out.println("startNextGame:" + playerToken + ":"
                                            + String.format("%02d:%02d", minutes, seconds));
                                }
                            } else if (messageType.equals("startNextGame")) {
                                String getPlayerToken = message.split(":")[1];
                                String completeTime = message.split(":")[2] + ":" + message.split(":")[3];

                                if (getPlayerToken.equals(playerToken)) {
                                    startNextGame("blue", completeTime);
                                } else {
                                    startNextGame("red", completeTime);
                                }
                            } else if (messageType.equals("playerLeft")) {
                                String getPlayerToken = message.split(":")[1];

                                if (!getPlayerToken.equals(playerToken)) {
                                    Platform.runLater(()->{
                                        setVictoryScreenData();
                                        victoryContainer.setManaged(true);
                                        victoryContainer.setVisible(true);
                                    });
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startCountdown() {
        Platform.runLater(() -> {
            findingPlayerContainer.setVisible(false);
            findingPlayerContainer.setManaged(false);
            runningGameContainer.setVisible(true);
            runningGameContainer.setManaged(true);
            startingInContainer.setVisible(true);
            startingInContainer.setManaged(true);
        });

        try {
            for (startCount = 5; startCount >= 1; startCount--) {
                Platform.runLater(() -> {
                    startingInCount.setText(String.valueOf(startCount));
                });
                Thread.sleep(1000);
            }
            Thread.sleep(1000);
            Platform.runLater(() -> {
                startingInContainer.setVisible(false);
                startingInContainer.setManaged(false);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startGame() {
        battleRunning = true;

        new Thread(() -> {
            try {
                while (battleRunning) {
                    for (seconds = 0; seconds < 60 && battleRunning; seconds++) {
                        Platform.runLater(() -> {
                            battleTimer.setText(String.format("%02d:%02d", minutes, seconds));
                        });
                        Thread.sleep(1000);
                    }
                    minutes++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        Platform.runLater(() -> {
            Scene primaryScene = runningGameContainer.getScene();

            playerBlueProgressLabel1.setText("Running");
            playerBlueProgressPoint1
                    .setBackground(new Background(new BackgroundFill(Color.web("#0000cc"), new CornerRadii(15), null)));

            playerRedProgressLabel1.setText("Running");
            playerRedProgressPoint1
                    .setBackground(new Background(new BackgroundFill(Color.web("#cc0000"), new CornerRadii(15), null)));

            LiveGame playerBlueGame1 = LiveGame.getGameInstance(games[0], gamesInitInfo[0], roomId, playerToken, true);
            playerBluePlayground.getChildren().add(playerBlueGame1);

            primaryScene.setOnKeyPressed(event -> {
                playerBlueGame1.actionOnKeyPressed(event.getCode().toString());
            });

            primaryScene.setOnKeyReleased(event -> {
                playerBlueGame1.actionOnKeyReleased(event.getCode().toString());
            });

            LiveGame playerRedGame1 = LiveGame.getGameInstance(games[0], gamesInitInfo[0], roomId, playerToken, false);
            playerRedPlayground.getChildren().add(playerRedGame1);
        });
    }

    private void setVictoryScreenData() {
        victoryScreenGameName1.setText(LiveGame.getGameTitle(games[0]));
        victoryScreenGameName2.setText(LiveGame.getGameTitle(games[1]));
        victoryScreenGameName3.setText(LiveGame.getGameTitle(games[2]));

        victoryScreenPlayerBlueName.setText(playerBlueNameData);
        victoryScreenPlayerBlueTrophy.setText(playerBlueTrophiesData);
        if(playerBlueGameTime1 != null) {
            victoryScreenPlayerBlueGame1Time.setText(playerBlueGameTime1);
            victoryScreenPlayerBlueGame1Time.getStyleClass().remove("victory-screen-games-not-complete");
            victoryScreenPlayerBlueGame1Time.getStyleClass().add("victory-screen-games-complete-time");
        }
        if(playerBlueGameTime2 != null) {
            victoryScreenPlayerBlueGame2Time.setText(playerBlueGameTime2);
            victoryScreenPlayerBlueGame2Time.getStyleClass().remove("victory-screen-games-not-complete");
            victoryScreenPlayerBlueGame2Time.getStyleClass().add("victory-screen-games-complete-time");
        }
        if(playerBlueGameTime3 != null) {
            victoryScreenPlayerBlueGame3Time.setText(playerBlueGameTime3);
            victoryScreenPlayerBlueGame3Time.getStyleClass().remove("victory-screen-games-not-complete");
            victoryScreenPlayerBlueGame3Time.getStyleClass().add("victory-screen-games-complete-time");
        }

        victoryScreenPlayerRedName.setText(playerRedNameData);
        victoryScreenPlayerRedTrophy.setText(playerRedTrophiesData);
        if(playerRedGameTime1 != null) {
            victoryScreenPlayerRedGame1Time.setText(playerRedGameTime1);
            victoryScreenPlayerRedGame1Time.getStyleClass().remove("victory-screen-games-not-complete");
            victoryScreenPlayerRedGame1Time.getStyleClass().add("victory-screen-games-complete-time");
        }
        if(playerRedGameTime2 != null) {
            victoryScreenPlayerRedGame2Time.setText(playerRedGameTime2);
            victoryScreenPlayerRedGame2Time.getStyleClass().remove("victory-screen-games-not-complete");
            victoryScreenPlayerRedGame2Time.getStyleClass().add("victory-screen-games-complete-time");
        }
        if(playerRedGameTime3 != null) {
            victoryScreenPlayerRedGame3Time.setText(playerRedGameTime3);
            victoryScreenPlayerRedGame3Time.getStyleClass().remove("victory-screen-games-not-complete");
            victoryScreenPlayerRedGame3Time.getStyleClass().add("victory-screen-games-complete-time");
        }
    }

    public void startNextGame(String player, String completeTime) {
        if (player.equals("blue")) {
            if (playerBlueGameTime1 == null) {
                playerBlueGameTime1 = completeTime;
                Platform.runLater(() -> {
                    playerBlueProgressLabel1.setText(completeTime);
                    playerBlueProgressLabel1.setStyle("-fx-font-weight: bold;");
                    playerBlueProgressStick1
                            .setBackground(new Background(new BackgroundFill(Color.web("#0000cc"), null, null)));
                    playerBlueProgressLabel2.setText("Running");
                    playerBlueProgressPoint2.setBackground(
                            new Background(new BackgroundFill(Color.web("#0000cc"), new CornerRadii(15), null)));

                    playerBluePlayground.getChildren().clear();

                    Scene primaryScene = runningGameContainer.getScene();

                    LiveGame playerBlueGame2 = LiveGame.getGameInstance(games[1], gamesInitInfo[1], roomId, playerToken, true);
                    playerBluePlayground.getChildren().add(playerBlueGame2);

                    primaryScene.setOnKeyPressed(event -> {
                        playerBlueGame2.actionOnKeyPressed(event.getCode().toString());
                    });

                    primaryScene.setOnKeyReleased(event -> {
                        playerBlueGame2.actionOnKeyReleased(event.getCode().toString());
                    });
                });
            } else if (playerBlueGameTime2 == null) {
                playerBlueGameTime2 = completeTime;
                Platform.runLater(() -> {
                    playerBlueProgressLabel2.setText(completeTime);
                    playerBlueProgressLabel2.setStyle("-fx-font-weight: bold;");
                    playerBlueProgressStick2
                            .setBackground(new Background(new BackgroundFill(Color.web("#0000cc"), null, null)));
                    playerBlueProgressLabel3.setText("Running");
                    playerBlueProgressPoint3.setBackground(
                            new Background(new BackgroundFill(Color.web("#0000cc"), new CornerRadii(15), null)));

                    playerBluePlayground.getChildren().clear();

                    Scene primaryScene = runningGameContainer.getScene();

                    LiveGame playerBlueGame3 = LiveGame.getGameInstance(games[2], gamesInitInfo[2], roomId, playerToken, true);
                    playerBluePlayground.getChildren().add(playerBlueGame3);

                    primaryScene.setOnKeyPressed(event -> {
                        playerBlueGame3.actionOnKeyPressed(event.getCode().toString());
                    });

                    primaryScene.setOnKeyReleased(event -> {
                        playerBlueGame3.actionOnKeyReleased(event.getCode().toString());
                    });
                });
            } else {
                playerBlueGameTime3 = completeTime;
                battleRunning = false;
                Platform.runLater(() -> {
                    playerBlueProgressLabel3.setText(completeTime);
                    playerBlueProgressLabel3.setStyle("-fx-font-weight: bold;");
                    playerBlueProgressStick3
                            .setBackground(new Background(new BackgroundFill(Color.web("#0000cc"), null, null)));
                    playerBlueProgressLabel4.setStyle("-fx-font-weight: bold;");
                    playerBlueProgressPoint4.setBackground(
                            new Background(new BackgroundFill(Color.web("#0000cc"), new CornerRadii(15), null)));
                    
                    out.println("matchEnd:"+playerToken);
                    setVictoryScreenData();
                    victoryContainer.setManaged(true);
                    victoryContainer.setVisible(true);
                });
            }
        } else if (player.equals("red")) {
            if (playerRedGameTime1 == null) {
                playerRedGameTime1 = completeTime;
                Platform.runLater(() -> {
                    playerRedProgressLabel1.setText(completeTime);
                    playerRedProgressLabel1.setStyle("-fx-font-weight: bold;");
                    playerRedProgressStick1
                            .setBackground(new Background(new BackgroundFill(Color.web("#cc0000"), null, null)));
                    playerRedProgressLabel2.setText("Running");
                    playerRedProgressPoint2.setBackground(
                            new Background(new BackgroundFill(Color.web("#cc0000"), new CornerRadii(15), null)));

                    playerRedPlayground.getChildren().clear();

                    LiveGame playerRedGame2 = LiveGame.getGameInstance(games[1], gamesInitInfo[1], roomId, playerToken, false);
                    playerRedPlayground.getChildren().add(playerRedGame2);
                });
            } else if (playerRedGameTime2 == null) {
                playerRedGameTime2 = completeTime;
                Platform.runLater(() -> {
                    playerRedProgressLabel2.setText(completeTime);
                    playerRedProgressLabel2.setStyle("-fx-font-weight: bold;");
                    playerRedProgressStick2
                            .setBackground(new Background(new BackgroundFill(Color.web("#cc0000"), null, null)));
                    playerRedProgressLabel3.setText("Running");
                    playerRedProgressPoint3.setBackground(
                            new Background(new BackgroundFill(Color.web("#cc0000"), new CornerRadii(15), null)));

                    playerRedPlayground.getChildren().clear();

                    LiveGame playerRedGame3 = LiveGame.getGameInstance(games[2], gamesInitInfo[2], roomId, playerToken, false);
                    playerRedPlayground.getChildren().add(playerRedGame3);
                });
            } else {
                playerRedGameTime3 = completeTime;
                battleRunning = false;
                Platform.runLater(() -> {
                    playerRedProgressLabel3.setText(completeTime);
                    playerRedProgressLabel3.setStyle("-fx-font-weight: bold;");
                    playerRedProgressStick3
                            .setBackground(new Background(new BackgroundFill(Color.web("#cc0000"), null, null)));
                    playerRedProgressLabel4.setStyle("-fx-font-weight: bold;");
                    playerRedProgressPoint4.setBackground(
                            new Background(new BackgroundFill(Color.web("#cc0000"), new CornerRadii(15), null)));

                    victoryLabel.setText("Defeat");
                    victoryAddedTrophy.setText("-5");
                    setVictoryScreenData();
                    out.println("matchEnd:"+playerToken);
                    victoryContainer.setManaged(true);
                    victoryContainer.setVisible(true);
                });
            }
        }
    }

    @FXML
    private void backToHomeButtonOnAction(ActionEvent e){
        if(socket != null) {
            out.println("playerLeft:"+playerToken);
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../Home/home.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("Mini Game Master");
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}