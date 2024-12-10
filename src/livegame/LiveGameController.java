package livegame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.UUID;
import java.util.prefs.Preferences;

import org.json.JSONArray;
import org.json.JSONObject;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class LiveGameController {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    String playerId = UUID.randomUUID().toString();
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
    String userid = preferences.get("userid", "default");

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
    public void initialize() {
        try {
            String IPV4Address = InetAddress.getLocalHost().getHostAddress();
            socket = new Socket(IPV4Address, 12345); // Connect to the server
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println("findRoom");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // System.out.println(playerId);
        try {
            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        // System.out.println("Server says: " + message);
                        if(message.startsWith("{")) {
                            messageObject = new JSONObject(message);

                            if(messageObject.get("type").equals("gameInitialize")) {
                                JSONArray games = messageObject.getJSONArray("games");
                                JSONArray gamesInitInfo = messageObject.getJSONArray("gamesInitInfo");

                                for (int i = 0; i < games.length(); i++) {
                                    this.games[i] = games.getString(i);
                                }

                                for (int i = 0; i < gamesInitInfo.length(); i++) {
                                    this.gamesInitInfo[i] = gamesInitInfo.getJSONObject(i);
                                }

                                System.out.println(this.games[0]);
                                System.out.println(this.gamesInitInfo[0]);
                            }
                        }
                        else {
                            messageType = message.split(":")[0];
                            if (messageType.equals("setRoomId")) {
                                roomId = message.split(":")[1];
                            } else if (messageType.equals("startGame")) {
                                startCountdown();
                                startGame();
                            }
                            else if (messageType.equals("gameComplete")) {
                                String getPlayerId = message.split(":")[1];
                                Platform.runLater(()->{
                                    if(!getPlayerId.equals(playerId)) {
                                        victoryLabel.setText("Defeat");
                                    }
                                    victoryContainer.setManaged(true);
                                    victoryContainer.setVisible(true);
                                });
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

        new Thread(()->{
            try {
                while (battleRunning) {
                    for (seconds = 0; seconds <= 59; seconds++) {
                        Platform.runLater(()->{
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

            SnakeGame snakeGameBlue = new SnakeGame(roomId, playerId, true);
            playerBluePlayground.getChildren().add(snakeGameBlue);

            primaryScene.setOnKeyPressed(event -> {
                snakeGameBlue.actionOnKeyPressed(event.getCode().toString());
            });

            SnakeGame snakeGameRed = new SnakeGame(roomId, playerId, false);
            playerRedPlayground.getChildren().add(snakeGameRed);
        });
    }
}