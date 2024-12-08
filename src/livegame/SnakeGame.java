package livegame;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SnakeGame extends Pane {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String playerId;

    private static final int WIDTH = 500;
    private static final int HEIGHT = 400;
    private static final int TILE_SIZE = 20;

    private GraphicsContext gc;
    private List<int[]> snake = new ArrayList<>();
    private int[] food = new int[2];
    private String direction = "RIGHT";
    private boolean running = true;
    private int score = 0; // Added score

    private Timeline timeline;

    private Label scoreLabel = new Label("Score: 0");

    public SnakeGame(Socket socket, String playerId) {
        this.playerId = playerId;

        try {
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }
        catch (Exception e) {  
            e.printStackTrace();
        }


        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        gc = canvas.getGraphicsContext2D();
        this.getChildren().add(canvas);

        scoreLabel.setFont(new Font(16));
        scoreLabel.setTextFill(Color.WHITE);
        scoreLabel.setLayoutY(HEIGHT + 5);
        this.getChildren().add(scoreLabel);

        initGame();

        timeline = new Timeline(new KeyFrame(Duration.millis(200), e -> update()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        new Thread(() -> {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    String messageType = message.split(":")[1];

                    System.out.println(messageType);

                    if(messageType.equals("move")) {
                        String keyCode = message.split(":")[2];
                        if(keyCode.equals("R")) {
                            restartGame();
                        }
                        else {
                            setDirection(message.split(":")[2]);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void initGame() {
        snake.clear();
        snake.add(new int[] { 5, 5 });
        spawnFood();
        direction = "RIGHT";
        running = true;
        score = 0;
        updateScore(); // Reset score
    }

    private void spawnFood() {
        food[0] = (int) (Math.random() * (WIDTH / TILE_SIZE));
        food[1] = (int) (Math.random() * (HEIGHT / TILE_SIZE));
    }

    private void update() {
        if (!running)
            return;

        // Move the snake
        int[] head = snake.get(0).clone();
        switch (direction) {
            case "UP" -> head[1]--;
            case "DOWN" -> head[1]++;
            case "LEFT" -> head[0]--;
            case "RIGHT" -> head[0]++;
        }
        snake.add(0, head);

        // Check collisions with walls
        if (head[0] < 0 || head[1] < 0 || head[0] >= WIDTH / TILE_SIZE || head[1] >= HEIGHT / TILE_SIZE) {
            gameOver();
            return;
        }

        // Check collisions with self
        for (int i = 1; i < snake.size(); i++) {
            if (head[0] == snake.get(i)[0] && head[1] == snake.get(i)[1]) {
                gameOver();
                return;
            }
        }

        // Check if food is eaten
        if (head[0] == food[0] && head[1] == food[1]) {
            spawnFood();
            score++;
            updateScore();
        } else {
            snake.remove(snake.size() - 1);
        }

        draw();
    }

    private void updateScore() {
        scoreLabel.setText("Score: " + score);
    }

    private void draw() {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        // Draw food
        gc.setFill(Color.RED);
        gc.fillOval(food[0] * TILE_SIZE, food[1] * TILE_SIZE, TILE_SIZE, TILE_SIZE);

        // Draw snake
        gc.setFill(Color.GREEN);
        for (int[] part : snake) {
            gc.fillRect(part[0] * TILE_SIZE, part[1] * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }
    }

    private void gameOver() {
        running = false;
        gc.setFill(Color.RED);
        gc.setFont(new Font(30));
        gc.fillText("Game Over", WIDTH / 2 - 75, HEIGHT / 2 - 10);
        gc.setFont(new Font(20));
        gc.fillText("Press 'R' to Restart", WIDTH / 2 - 90, HEIGHT / 2 + 20);

        // timeline.stop();
    }

    public void setDirection(String newDirection) {
        // Prevent reverse direction
        if ((direction.equals("UP") && newDirection.equals("DOWN")) ||
                (direction.equals("DOWN") && newDirection.equals("UP")) ||
                (direction.equals("LEFT") && newDirection.equals("RIGHT")) ||
                (direction.equals("RIGHT") && newDirection.equals("LEFT"))) {
            return;
        }
        this.direction = newDirection;
    }

    public void restartGame() {
        initGame();
        timeline.play();
    }
}
