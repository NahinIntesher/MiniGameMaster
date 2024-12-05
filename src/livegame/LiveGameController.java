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
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class LiveGameController {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    @FXML
    private BorderPane findingPlayerContainer;

    @FXML
    public void initialize() {
        System.out.println("hullo");

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
                                try {
                                    FXMLLoader loader = new FXMLLoader(getClass().getResource("livegame.fxml"));
                                    Parent newRoot = loader.load();

                                    Scene newScene = new Scene(newRoot);
                                    Stage currentStage = (Stage) findingPlayerContainer.getScene().getWindow();
                                    currentStage.setScene(newScene);
                                    currentStage.setTitle("Game");
                                    currentStage.show();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
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