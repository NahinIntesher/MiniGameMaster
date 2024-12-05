package livegame;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.io.*;
import java.net.*;

public class GameClient extends Application {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Text statusText;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            socket = new Socket("localhost", 12345);  // Connect to the server
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            StackPane root = new StackPane();
            statusText = new Text("Waiting for opponent...");
            root.getChildren().add(statusText);

            Scene scene = new Scene(root, 400, 400);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Snake Game");
            primaryStage.show();

            // Listen for server messages
            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        System.out.println("Server says: " + message);
                        if (message.startsWith("Game starting")) {
                            // Game starting - update UI
                            statusText.setText("Game started!");
                            startGame();
                        } else if (message.startsWith("Waiting for another player")) {
                            // Player is waiting for the opponent
                            statusText.setText("Waiting for opponent...");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Dummy method to start the game after receiving "Game started" message
    private void startGame() {
        // Implement the logic to start the game, e.g., initialize the snake, game loop, etc.
        System.out.println("Game starting...");
        // For now, we'll just print a message to the console
    }
}
