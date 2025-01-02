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
    private static final int TILE_SIZE = 25;

    int[] randomFoodX = new int[100];
    int[] randomFoodY = new int[100];

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
    private boolean directionChanged = false;

    private Timeline timeline;

    private Label currentScoreValue = new Label("0");
    private Text gameOverText = new Text("-2");

    public Snake(JSONObject gameInitializeInfo, String roomId, String playerToken, boolean self) {
        this.playerToken = playerToken;
        this.self = self;

        JSONArray randomFoodXJsonArray = gameInitializeInfo.getJSONArray("randomFoodX");
        JSONArray randomFoodYJsonArray = gameInitializeInfo.getJSONArray("randomFoodY");

        for(int i=0; i< 100; i++) {
            randomFoodX[i] = randomFoodXJsonArray.getInt(i);
            randomFoodY[i] = randomFoodYJsonArray.getInt(i);
        }

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
                    
                    if (messageType.equals("matchEnd")) {
                        System.out.println("test end");
                        terminateGame();
                        break;
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
        if(self){
            sendGameState();
        }
    }

    private void spawnFood() {
        // food[0] = (int) (Math.random() * (WIDTH / TILE_SIZE));
        // food[1] = (int) (Math.random() * (HEIGHT / TILE_SIZE));

        food[0] = randomFoodX[iFood];
        food[1] = randomFoodY[iFood];

        if (iFood < 100) {
            iFood++;
        } else {
            iFood = 0;
        }

        if(self) {
            sendGameState();
        }
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

        directionChanged = false;

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
            if(score-2 >= 0) {
                gameOverText.setText("-2");
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
                    if(score-2 >= 0) {
                        initGame(score - 2);
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
        if(directionChanged) return;

        if (input.equals("UP")) {
            setDirection("UP");
            directionChanged = true;
        }
        if (input.equals("DOWN")) {
            setDirection("DOWN");
            directionChanged = true;
        }
        if (input.equals("LEFT")) {
            setDirection("LEFT");
            directionChanged = true;
        }
        if (input.equals("RIGHT")) {
            setDirection("RIGHT");
            directionChanged = true;
        }

        sendGameState();
    }

    public void terminateGame() {
        timeline.stop();
        running = false;
    }

    private void sendGameState() {
        JSONObject gameState = new JSONObject();
        gameState.put("type", "gameState");
        gameState.put("game", "Snake");
        gameState.put("playerToken", playerToken);
        gameState.put("snake", snake);
        gameState.put("food", food);
        gameState.put("score", score);
        gameState.put("direction", direction);
        gameState.put("running", running);

        out.println(gameState.toString());
    }

    public void updateGameState(JSONObject gameState) {
        if(gameState.getString("game").equals("Snake")){
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
}
