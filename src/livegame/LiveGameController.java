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

import database.DatabaseConnection;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
    public void initialize() {
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
                                String getPlayerToken = message.split(":")[1];
                                
                                if(getPlayerToken.equals(playerToken)) {
                                    out.println("startNextGame:"+playerToken+":"+String.format("%02d:%02d", minutes, seconds));
                                }
                                // Platform.runLater(()->{
                                //     if(!getPlayerToken.equals(playerToken)) {
                                //         victoryLabel.setText("Defeat");
                                //     }
                                //     victoryContainer.setManaged(true);
                                //     victoryContainer.setVisible(true);
                                // });
                            }
                            else if (messageType.equals("startNextGame")) {
                                String getPlayerToken = message.split(":")[1];
                                String completeTime = message.split(":")[2]+":"+message.split(":")[3];

                                if(getPlayerToken.equals(playerToken)) {
                                    startNextGame("blue", completeTime);
                                }
                                else {
                                    startNextGame("red", completeTime);
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

            
            playerBlueProgressLabel1.setText("Running"); 
            playerBlueProgressPoint1.setBackground(new Background(new BackgroundFill(Color.web("#0000cc"), new CornerRadii(15), null)));
            
            playerRedProgressLabel1.setText("Running"); 
            playerRedProgressPoint1.setBackground(new Background(new BackgroundFill(Color.web("#cc0000"), new CornerRadii(15), null)));

            LiveGame snakeGameBlue = new Snake(roomId, playerToken, true);
            playerBluePlayground.getChildren().add(snakeGameBlue);

            primaryScene.setOnKeyPressed(event -> {
                snakeGameBlue.actionOnKeyPressed(event.getCode().toString());
            });

            LiveGame snakeGameRed = new Snake(roomId, playerToken, false);
            playerRedPlayground.getChildren().add(snakeGameRed);
        });
    }

    public void startNextGame(String player, String completeTime) {
        if(player.equals("blue")) {
            if(playerBlueGameTime1 == null) {
                playerBlueGameTime1 = completeTime;
                Platform.runLater(() -> {
                    playerBlueProgressLabel1.setText(completeTime);
                    playerBlueProgressLabel1.setStyle("-fx-font-weight: bold;");   
                    playerBlueProgressStick1.setBackground(new Background(new BackgroundFill(Color.web("#0000cc"), null, null)));
                    playerBlueProgressLabel2.setText("Running");
                    playerBlueProgressPoint2.setBackground(new Background(new BackgroundFill(Color.web("#0000cc"), new CornerRadii(15), null)));

                    playerBluePlayground.getChildren().clear();

                    Scene primaryScene = runningGameContainer.getScene();

                    LiveGame playerBlueGame2 = new Tetris(roomId, playerToken, true);
                    playerBluePlayground.getChildren().add(playerBlueGame2);
        
                    primaryScene.setOnKeyPressed(event -> {
                        playerBlueGame2.actionOnKeyPressed(event.getCode().toString());
                    });
                    
                    primaryScene.setOnKeyReleased(event -> {
                        playerBlueGame2.actionOnKeyReleased(event.getCode().toString());
                    });
                });
            }
            else if(playerBlueGameTime2 == null) {
                playerBlueGameTime2 = completeTime;
                Platform.runLater(() -> {
                    playerBlueProgressLabel2.setText(completeTime);
                    playerBlueProgressLabel2.setStyle("-fx-font-weight: bold;");   
                    playerBlueProgressStick2.setBackground(new Background(new BackgroundFill(Color.web("#0000cc"), null, null)));
                    playerBlueProgressLabel3.setText("Running");
                    playerBlueProgressPoint3.setBackground(new Background(new BackgroundFill(Color.web("#0000cc"), new CornerRadii(15), null)));

                    playerBluePlayground.getChildren().clear();
                    
                    Scene primaryScene = runningGameContainer.getScene();

                    LiveGame playerBlueGame3 = new MiniGolf(roomId, playerToken, true);
                    playerBluePlayground.getChildren().add(playerBlueGame3);
        
                    primaryScene.setOnKeyPressed(event -> {
                        playerBlueGame3.actionOnKeyPressed(event.getCode().toString());
                    });
                    
                    primaryScene.setOnKeyReleased(event -> {
                        playerBlueGame3.actionOnKeyReleased(event.getCode().toString());
                    });
                });
            }
            else {
                playerBlueGameTime3 = completeTime;
                Platform.runLater(() -> {
                    playerBlueProgressLabel3.setText(completeTime);
                    playerBlueProgressLabel3.setStyle("-fx-font-weight: bold;");   
                    playerBlueProgressStick3.setBackground(new Background(new BackgroundFill(Color.web("#0000cc"), null, null)));
                    playerBlueProgressLabel4.setStyle("-fx-font-weight: bold;");
                    playerBlueProgressPoint4.setBackground(new Background(new BackgroundFill(Color.web("#0000cc"), new CornerRadii(15), null)));
    
                });
            }
        }       
        else if(player.equals("red")) {
            if(playerRedGameTime1 == null) {
                playerRedGameTime1 = completeTime;         
                Platform.runLater(() -> {
                    playerRedProgressLabel1.setText(completeTime); 
                    playerRedProgressLabel1.setStyle("-fx-font-weight: bold;"); 
                    playerRedProgressStick1.setBackground(new Background(new BackgroundFill(Color.web("#cc0000"), null, null)));
                    playerRedProgressLabel2.setText("Running");
                    playerRedProgressPoint2.setBackground(new Background(new BackgroundFill(Color.web("#cc0000"), new CornerRadii(15), null))); 

                    playerRedPlayground.getChildren().clear();

                    LiveGame playerRedGame2 = new Tetris(roomId, playerToken, false);
                    playerRedPlayground.getChildren().add(playerRedGame2);
                });
            }
            else if(playerRedGameTime2 == null) {
                playerRedGameTime2 = completeTime;         
                Platform.runLater(() -> {
                    playerRedProgressLabel2.setText(completeTime); 
                    playerRedProgressLabel2.setStyle("-fx-font-weight: bold;"); 
                    playerRedProgressStick2.setBackground(new Background(new BackgroundFill(Color.web("#cc0000"), null, null)));
                    playerRedProgressLabel3.setText("Running");
                    playerRedProgressPoint3.setBackground(new Background(new BackgroundFill(Color.web("#cc0000"), new CornerRadii(15), null))); 

                    playerRedPlayground.getChildren().clear();
                    
                    LiveGame playerRedGame3 = new MiniGolf(roomId, playerToken, false);
                    playerRedPlayground.getChildren().add(playerRedGame3);
                });
            }
            else {
                playerRedGameTime3 = completeTime;         
                Platform.runLater(() -> {
                    playerRedProgressLabel3.setText(completeTime); 
                    playerRedProgressLabel3.setStyle("-fx-font-weight: bold;"); 
                    playerRedProgressStick3.setBackground(new Background(new BackgroundFill(Color.web("#cc0000"), null, null)));
                    playerRedProgressLabel4.setStyle("-fx-font-weight: bold;");
                    playerRedProgressPoint4.setBackground(new Background(new BackgroundFill(Color.web("#cc0000"), new CornerRadii(15), null))); 
                });                
            }
        }       
    }
}