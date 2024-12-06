package livegame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

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

    @FXML
    private StackPane findingPlayerContainer;

    @FXML
    private StackPane runningGameContainer;

    @FXML
    private StackPane playerBluePlayground;

    @FXML
    public void initialize() {
        try {
            socket = new Socket("localhost", 12345); // Connect to the server
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

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

                                Label newLabel = new Label("New Label at Runtime");
                                playerBluePlayground.getChildren().add(newLabel);
                            });
                        } else if (message.startsWith("Waiting for another player")) {

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