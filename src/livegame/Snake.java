package livegame;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class Snake extends LiveGame {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String playerToken;
    private boolean self;

    private static final int WIDTH = 500;
    private static final int HEIGHT = 400;
    private static final int TILE_SIZE = 20;

    int[] randomFoodX = {
            23, 10, 15, 7, 21, 3, 12, 5, 9, 0,
            18, 24, 4, 17, 22, 6, 8, 11, 14, 2,
            19, 16, 24, 20, 13, 1, 7, 14, 3, 18,
            9, 6, 12, 5, 11, 22, 8, 0, 23, 17,
            23, 4, 15, 20, 21, 10, 19, 13, 16, 24,
            14, 7, 9, 2, 18, 6, 11, 12, 0, 22,
            22, 21, 1, 8, 3, 13, 17, 10, 5, 19,
            4, 24, 15, 16, 20, 14, 7, 23, 2, 18,
            6, 0, 21, 11, 9, 5, 1, 3, 22, 17,
            8, 13, 10, 12, 21, 15, 20, 19, 4, 24
    };

    int[] randomFoodY = {
            10, 18, 2, 19, 7, 5, 13, 9, 15, 19,
            19, 12, 0, 9, 3, 6, 11, 8, 5, 4,
            19, 1, 17, 13, 14, 16, 18, 7, 5, 13,
            4, 0, 9, 15, 3, 12, 2, 13, 19, 10,
            10, 6, 4, 11, 18, 8, 2, 14, 16, 17,
            1, 2, 22, 5, 3, 12, 19, 7, 14, 8,
            13, 18, 14, 10, 21, 1, 11, 16, 4, 19,
            17, 6, 9, 13, 4, 2, 15, 0, 19, 8,
            14, 19, 18, 7, 11, 19, 6, 3, 9, 5,
            4, 18, 2, 13, 1, 0, 16, 4, 12, 17
    };

    int iFood = 0;

    String message;
    int i;
    JSONArray snakeArray;
    JSONArray foodArray;
    List<int[]> newSnake;
    int[] newFood;

    private GraphicsContext gc;
    private List<int[]> snake = new ArrayList<>();
    private int[] food = new int[2];
    private String direction = "RIGHT";
    private boolean running = true;
    private int score = 0; // Added score
    private int targetScore = 10; // Added score

    private Timeline timeline;

    private Label currentScoreValue = new Label("0");
    private Text gameOverText = new Text("-5");

    public Snake(String roomId, String playerToken, boolean self) {
        this.playerToken = playerToken;
        this.self = self;

        this.setBackground(new Background(new BackgroundFill(Color.web("#239b56"), null, null)));
        this.setAlignment(Pos.CENTER);
        this.setPadding(new Insets(0, 0, 60, 0));

        try {
            socket = new Socket(serverAddress, 12345);
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("joinRoom:" + roomId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        VBox root = new VBox();
        root.setAlignment(javafx.geometry.Pos.CENTER);
        root.setSpacing(10);

        HBox hBox = new HBox();
        hBox.setAlignment(javafx.geometry.Pos.CENTER);
        hBox.setSpacing(10);
        hBox.setPrefSize(500, 90);
        hBox.setMaxSize(500, 90);

        // VBox for "Current Score"
        VBox currentScoreBox = new VBox();
        currentScoreBox.setAlignment(javafx.geometry.Pos.CENTER);
        currentScoreBox.setPrefSize(100, 200);
        currentScoreBox.setStyle("-fx-background-color: #186a3b; -fx-border-color: #ffffff; -fx-border-width: 2;");
        HBox.setHgrow(currentScoreBox, Priority.ALWAYS);

        Label currentScoreLabel = new Label("Current Score");
        currentScoreLabel.setFont(Font.font("Poppins Medium", 16));
        currentScoreLabel.setTextFill(Color.WHITE);

        currentScoreValue.setFont(Font.font("Poppins Bold", 32));
        currentScoreValue.setTextFill(Color.WHITE);

        currentScoreBox.getChildren().addAll(currentScoreLabel, currentScoreValue);

        // VBox for "Target Score"
        VBox targetScoreBox = new VBox();
        targetScoreBox.setAlignment(javafx.geometry.Pos.CENTER);
        targetScoreBox.setPrefSize(100, 200);
        targetScoreBox.setStyle("-fx-background-color: #186a3b; -fx-border-color: #ffffff; -fx-border-width: 2;");
        HBox.setHgrow(targetScoreBox, Priority.ALWAYS);

        Label targetScoreLabel = new Label("Target Score");
        targetScoreLabel.setFont(Font.font("Poppins Medium", 16));
        targetScoreLabel.setTextFill(Color.WHITE);

        Label targetScoreValue = new Label("10");
        targetScoreValue.setFont(Font.font("Poppins Bold", 32));
        targetScoreValue.setTextFill(Color.WHITE);

        targetScoreBox.getChildren().addAll(targetScoreLabel, targetScoreValue);

        // Add VBoxes to HBox
        hBox.getChildren().addAll(currentScoreBox, targetScoreBox);

        gameOverText.setFill(Color.RED);
        gameOverText.setStroke(Color.BLACK);
        gameOverText.setStrokeWidth(3.0);
        gameOverText.setStrokeType(javafx.scene.shape.StrokeType.OUTSIDE);
        gameOverText.setFont(Font.font("Poppins Bold", 100));
        gameOverText.setVisible(false);

        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        gc = canvas.getGraphicsContext2D();

        root.getChildren().addAll(canvas, hBox);

        this.getChildren().add(root);
        this.getChildren().add(gameOverText);

        initGame(0);

        timeline = new Timeline(new KeyFrame(Duration.millis(200), e -> update()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        new Thread(() -> {
            try {
                while ((message = in.readLine()) != null) {
                    String messageType = message.split(":")[0];

                    if (!self && message.startsWith("{")) {
                        JSONObject gameState = new JSONObject(message);
                        if (!gameState.getString("playerToken").equals(playerToken)) {
                            updateGameState(gameState);
                        }
                    }

                    if (!self && messageType.equals("gameOver")) {
                        String fromPlayerToken = message.split(":")[1];
                        if (!fromPlayerToken.equals(playerToken)) {
                            gameOver();
                        }
                    }
                    
                    if (messageType.equals("gameComplete")) {
                        String fromPlayerToken = message.split(":")[1];
                        if (!self && !fromPlayerToken.equals(playerToken)) {
                            terminateGame();
                            break;
                        }
                        if (self && fromPlayerToken.equals(playerToken)) {
                            terminateGame();
                            break;
                        }
                    }
                    
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void initGame(int getScore) {
        snake.clear();
        snake.add(new int[] { 5, 5 });
        spawnFood();
        direction = "RIGHT";
        running = true;
        score = getScore;
        updateScore(); // Reset score
        sendGameState();
    }

    private void spawnFood() {
        // food[0] = (int) (Math.random() * (WIDTH / TILE_SIZE));
        // food[1] = (int) (Math.random() * (HEIGHT / TILE_SIZE));

        food[0] = randomFoodX[iFood];
        food[1] = randomFoodY[iFood];

        if (iFood <= 98) {
            iFood++;
        } else {
            iFood = 0;
        }

        sendGameState();
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
            if (self) {
                gameOver();
            }
            return;
        }

        // Check collisions with self
        for (i = 1; i < snake.size(); i++) {
            if (head[0] == snake.get(i)[0] && head[1] == snake.get(i)[1]) {
                if (self) {
                    gameOver();
                }
                return;
            }
        }

        // Check if food is eaten
        if (head[0] == food[0] && head[1] == food[1]) {
            score++;
            if (self) {
                spawnFood();
            }
            if(self && score >= targetScore) {
                sendGameState();
                out.println("gameComplete:"+playerToken);
            }
        } else {
            snake.remove(snake.size() - 1);
        }

        updateScore();
        draw();
    }

    private void updateScore() {
        Platform.runLater(() -> {
            currentScoreValue.setText(String.valueOf(this.score));
        });
    }

    private void draw() {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        for (int x = 0; x < WIDTH / TILE_SIZE; x++) {
            for (int y = 0; y < HEIGHT / TILE_SIZE; y++) {
                if ((x + y) % 2 == 0) {
                    gc.setFill(Color.web("#2ecc71")); // Lighter green
                } else {
                    gc.setFill(Color.web("#58d68d")); // Darker green
                }
                gc.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }

        // Draw food
        gc.setFill(Color.web("#e74c3c"));
        gc.fillOval(food[0] * TILE_SIZE, food[1] * TILE_SIZE, TILE_SIZE, TILE_SIZE);

        // Draw snake
        gc.setFill(Color.web("#2e86c1"));
        for (int[] part : snake) {
            gc.fillRect(part[0] * TILE_SIZE, part[1] * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }

        int[] head = snake.get(0);
        // Add larger eyes
        gc.setFill(Color.WHITE);
        // Left eye
        gc.fillOval(head[0] * TILE_SIZE + TILE_SIZE * 0.15, head[1] * TILE_SIZE + TILE_SIZE * 0.2, TILE_SIZE * 0.3,
                TILE_SIZE * 0.3);
        // Right eye
        gc.fillOval(head[0] * TILE_SIZE + TILE_SIZE * 0.55, head[1] * TILE_SIZE + TILE_SIZE * 0.2, TILE_SIZE * 0.3,
                TILE_SIZE * 0.3);

        // Add larger pupils
        gc.setFill(Color.BLACK);
        // Left pupil
        gc.fillOval(head[0] * TILE_SIZE + TILE_SIZE * 0.22, head[1] * TILE_SIZE + TILE_SIZE * 0.27, TILE_SIZE * 0.15,
                TILE_SIZE * 0.15);
        // Right pupil
        gc.fillOval(head[0] * TILE_SIZE + TILE_SIZE * 0.62, head[1] * TILE_SIZE + TILE_SIZE * 0.27, TILE_SIZE * 0.15,
                TILE_SIZE * 0.15);
    }

    private void gameOver() {
        if (self) {
            out.println("gameOver:" + playerToken);
        }
        running = false;
        timeline.stop();
        Platform.runLater(() -> {
            if(score-5 >= 0) {
                gameOverText.setText("-5");
            }
            else {
                gameOverText.setText("0");                        
            }
            gameOverText.setVisible(true);
        });

        new Thread(() -> {
            try {
                Thread.sleep(1500);
                Platform.runLater(() -> {
                    gameOverText.setVisible(false);
                });
                if (self) {
                    if(score-5 >= 0) {
                        initGame(score - 5);
                    }
                    else {
                        initGame(0);
                    }
                }
                timeline.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
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
        initGame(0);
        timeline.play();
    }

    public void actionOnKeyPressed(String input) {
        if (input.equals("UP")) {
            setDirection("UP");
        }
        if (input.equals("DOWN")) {
            setDirection("DOWN");
        }
        if (input.equals("LEFT")) {
            setDirection("LEFT");
        }
        if (input.equals("RIGHT")) {
            setDirection("RIGHT");
        }

        sendGameState();
    }

    public void terminateGame() {
        timeline.stop();
        running = false;
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendGameState() {
        JSONObject gameState = new JSONObject();
        gameState.put("type", "gameState");
        gameState.put("playerToken", playerToken);
        gameState.put("snake", snake);
        gameState.put("food", food);
        gameState.put("score", score);
        gameState.put("direction", direction);
        gameState.put("running", running);

        out.println(gameState.toString());
    }

    public void updateGameState(JSONObject gameState) {
        snakeArray = gameState.getJSONArray("snake");
        foodArray = gameState.getJSONArray("food");

        newSnake = new ArrayList<>();
        newFood = new int[foodArray.length()];

        for (int i = 0; i < snakeArray.length(); i++) {
            JSONArray segment = snakeArray.getJSONArray(i);
            int[] coords = new int[2];
            coords[0] = segment.getInt(0);
            coords[1] = segment.getInt(1);
            newSnake.add(coords);
        }

        for (int i = 0; i < foodArray.length(); i++) {
            newFood[i] = foodArray.getInt(i);
        }

        this.snake = newSnake;
        this.food = newFood;
        this.score = gameState.getInt("score");
        this.direction = gameState.getString("direction");
        this.running = gameState.getBoolean("running");
    }
}
