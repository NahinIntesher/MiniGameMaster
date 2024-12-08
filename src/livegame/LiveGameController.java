package livegame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.UUID;
import java.util.prefs.Preferences;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class LiveGameController {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    String playerId = UUID.randomUUID().toString();

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
    public void initialize() {  
        try {
            String IPV4Address = InetAddress.getLocalHost().getHostAddress();
            socket = new Socket(IPV4Address, 12345); // Connect to the server
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }
        catch (Exception e) {  
            e.printStackTrace();
        }

        Platform.runLater(() -> {
            Scene primaryScene = runningGameContainer.getScene(); 

            primaryScene.setOnKeyPressed(event -> {
                out.println(playerId+":move:"+event.getCode());
            });
        });

        System.out.println(playerId);
        try {
            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        System.out.println("Server says: " + message);
                        if (message.startsWith("Game starting")) {
                            Platform.runLater(() -> {
                                findingPlayerContainer.setVisible(false);
                                findingPlayerContainer.setManaged(false);
                                runningGameContainer.setVisible(true);
                                runningGameContainer.setManaged(true);

                                SnakeGame snakeGameBlue = new SnakeGame(socket, playerId);
                                playerBluePlayground.getChildren().add(snakeGameBlue);

                                SnakeGame snakeGameRed= new SnakeGame(socket, playerId);
                                playerRedPlayground.getChildren().add(snakeGameRed);
                            });
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
}