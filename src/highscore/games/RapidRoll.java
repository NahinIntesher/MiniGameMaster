package highscore.games;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

import highscore.HighScoreGameController;
import java.util.HashSet;

public class RapidRoll extends HighScoreGame {

    private static final int WIDTH = 370;
    private static final int HEIGHT = 510;
    private static final double GRAVITY = 0.4;
    private static final double JUMP_FORCE = -9;
    private static final double MOVE_SPEED = 5;
    private static final double SCROLL_SPEED = 2;

    private double ballX;
    private double ballY;
    private double ballVelocityY;
    private double ballVelocityX;
    private static final int BALL_RADIUS = 15;
    private boolean canJump = true;

    private static final int PLATFORM_WIDTH = 80;
    private static final int PLATFORM_HEIGHT = 15;
    private ArrayList<Double> platformsX;
    private ArrayList<Double> platformsY;
    private ArrayList<Boolean> platformsVisited;
    private double[] randomPlatformsX = new double[500];
    private int randomPlatformsXI = 0;
    private Timeline timeline;
    private Canvas canvas;
    private GraphicsContext gc;
    private Label currentScoreValue;
    private Text gameOverText = new Text("-10");
    private boolean isGameOver = false;
    private int score = 0;
    private int targetScore = 100;
    private HashSet<String> activeKeys;

    private String message;
    private String messageType;
    HighScoreGameController highScoreGameController;

    Random random = new Random();

    public RapidRoll(HighScoreGameController highScoreGameController) {
        this.highScoreGameController = highScoreGameController;
        
        for (int i = 0; i < 500; i++) {
            randomPlatformsX[i] = random.nextInt(290);
        }

        this.setBackground(new Background(new BackgroundFill(Color.web("#1976d2"), null, null)));
        this.setAlignment(Pos.CENTER);


        HBox root = new HBox();
        root.setAlignment(javafx.geometry.Pos.CENTER);
        root.setSpacing(20);

        StackPane canvasContainer = new StackPane();

        canvasContainer.setPrefSize(WIDTH + 15, HEIGHT + 15);
        canvasContainer.setMaxSize(WIDTH + 15, HEIGHT + 15);
        canvasContainer.setAlignment(Pos.CENTER);
        canvasContainer.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));

        canvas = new Canvas(WIDTH, HEIGHT);

        VBox sidePanel = new VBox(20);
        sidePanel.setAlignment(javafx.geometry.Pos.CENTER);

        VBox currentScoreBox = new VBox();
        currentScoreBox.setAlignment(javafx.geometry.Pos.CENTER);
        currentScoreBox.setPrefSize(150, 100);
        currentScoreBox.setStyle("-fx-background-color: #2196f3; -fx-border-color: white; -fx-border-width: 3;");

        Label currentScoreLabel = new Label("Current Score");
        currentScoreLabel.setFont(Font.font("Poppins Medium", 16));
        currentScoreLabel.setTextFill(Color.WHITE);
        currentScoreValue = new Label("0");
        currentScoreValue.setFont(Font.font("Poppins", FontWeight.BOLD, 36));
        currentScoreValue.setTextFill(Color.WHITE);

        currentScoreBox.getChildren().addAll(currentScoreLabel, currentScoreValue);

        
        sidePanel.getChildren().addAll(currentScoreBox);

        canvasContainer.getChildren().add(canvas);
        root.getChildren().addAll(canvasContainer, sidePanel);

        gameOverText.setFill(Color.RED);
        gameOverText.setStroke(Color.BLACK);
        gameOverText.setStrokeWidth(3.0);
        gameOverText.setStrokeType(StrokeType.OUTSIDE);
        gameOverText.setFont(Font.font("Poppins", FontWeight.BOLD , 100));
        gameOverText.setVisible(false);

        this.getChildren().addAll(root, gameOverText);

        gc = canvas.getGraphicsContext2D();
        activeKeys = new HashSet<>();
        initializeGame();

        timeline = new Timeline(new KeyFrame(Duration.millis(16), e -> {
            handleInput();
            updateGame();
            renderGame();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);

        startGame();
    }

    private void handleInput() {
        if (isGameOver)
            return;

        if (activeKeys.contains("LEFT") || activeKeys.contains("A")) {
            ballVelocityX = -MOVE_SPEED;
        } else if (activeKeys.contains("RIGHT") || activeKeys.contains("D")) {
            ballVelocityX = MOVE_SPEED;
        } else {
            ballVelocityX = 0;
        }
    }

    private void updateGame() {
        if (isGameOver)
            return;
    
        // Store previous position for collision check
        double previousX = ballX;
        double previousY = ballY;
    
        // Calculate next position before applying it
        double nextX = ballX + ballVelocityX;
        double nextY = ballY + ballVelocityY;
    
        // Automatic downward scrolling
        for (int i = 0; i < platformsY.size(); i++) {
            platformsY.set(i, platformsY.get(i) + SCROLL_SPEED);
        }
    
        // Ball also moves down with scenery when falling
        if (ballVelocityY > 0) {
            nextY += SCROLL_SPEED;
        }
    
        // Screen wrapping for horizontal movement
        if (nextX < -BALL_RADIUS) {
            nextX = WIDTH + BALL_RADIUS;
        } else if (nextX > WIDTH + BALL_RADIUS) {
            nextX = -BALL_RADIUS;
        }
    
        // Apply gravity
        ballVelocityY += GRAVITY;
    
        // Check collisions before updating position
        boolean collisionHandled = false;
        
        for (int i = 0; i < platformsX.size(); i++) {
            double platformX = platformsX.get(i);
            double platformY = platformsY.get(i);
            boolean platformVisited = platformsVisited.get(i);
    
            // Check if ball will collide with platform
            if (nextX + BALL_RADIUS > platformX &&
                nextX - BALL_RADIUS < platformX + PLATFORM_WIDTH &&
                nextY + BALL_RADIUS > platformY &&
                nextY - BALL_RADIUS < platformY + PLATFORM_HEIGHT) {
                
                // Calculate overlap distances
                double overlapLeft = (nextX + BALL_RADIUS) - platformX;
                double overlapRight = (platformX + PLATFORM_WIDTH) - (nextX - BALL_RADIUS);
                double overlapTop = (nextY + BALL_RADIUS) - platformY;
                double overlapBottom = (platformY + PLATFORM_HEIGHT) - (nextY - BALL_RADIUS);
    
                // Find the smallest overlap
                double minOverlap = Math.min(Math.min(overlapLeft, overlapRight), 
                                           Math.min(overlapTop, overlapBottom));
    
                // Determine collision side based on previous position and overlap
                if (minOverlap == overlapTop && previousY + BALL_RADIUS <= platformY) {
                    // Top collision
                    nextY = platformY - BALL_RADIUS;
                    ballVelocityY = 0;
                    canJump = true;
                    if (!platformVisited) {
                        score += 5;
                        platformsVisited.set(i, true);
                        Platform.runLater(() -> currentScoreValue.setText(String.valueOf(score)));
                    }
                    collisionHandled = true;
                } else if (minOverlap == overlapBottom && previousY - BALL_RADIUS >= platformY + PLATFORM_HEIGHT) {
                    // Bottom collision
                    nextY = platformY + PLATFORM_HEIGHT + BALL_RADIUS;
                    ballVelocityY = 8; // Fixed downward velocity after bottom collision
                    collisionHandled = true;
                } else if (minOverlap == overlapLeft && !collisionHandled) {
                    // Left collision
                    nextX = platformX - BALL_RADIUS;
                    ballVelocityX = 0;
                } else if (minOverlap == overlapRight && !collisionHandled) {
                    // Right collision
                    nextX = platformX + PLATFORM_WIDTH + BALL_RADIUS;
                    ballVelocityX = 0;
                }
            }
        }
    
        // Update ball position
        ballX = nextX;
        ballY = nextY;
    
        // Remove platforms that are off screen
        for (int i = platformsY.size() - 1; i >= 0; i--) {
            if (platformsY.get(i) > HEIGHT) {
                platformsX.remove(i);
                platformsY.remove(i);
                platformsVisited.remove(i);
            }
        }
    
        // Add new platforms at top
        while (platformsY.size() < 10) {
            double lastPlatformY = platformsY.isEmpty() ? 0 : platformsY.get(platformsY.size() - 1);
            platformsX.add(randomPlatformsX[randomPlatformsXI]);
            if(randomPlatformsXI<499) {
                randomPlatformsXI++;
            }
            else {
                randomPlatformsXI = 0;
            }
            platformsY.add(lastPlatformY - 100);
            platformsVisited.add(false);
        }
    
        // Check game over
        if (ballY > HEIGHT) {
            gameOver();
        }
    }

    private void jump() {
        if (canJump) {
            ballVelocityY = JUMP_FORCE;
            canJump = false; // Disable jumping until landing again
        }
    }

    private void renderGame() {
        // Clear background
        gc.setFill(Color.web("#90caf9"));
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        gc.setFill(Color.web("#1e88e5"));
        gc.fillRect(0, 0, WIDTH, HEIGHT / 5);

        gc.setFill(Color.web("#2196f3"));
        gc.fillRect(0, HEIGHT / 5, WIDTH, HEIGHT / 5);

        gc.setFill(Color.web("#42a5f5"));
        gc.fillRect(0, (HEIGHT / 5)*2, WIDTH, HEIGHT / 5);

        gc.setFill(Color.web("#64b5f6"));
        gc.fillRect(0, (HEIGHT / 5)*3, WIDTH, HEIGHT / 5);

        // Draw platforms
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2.0);
        gc.setFill(Color.web("#33CC66"));

        for (int i = 0; i < platformsX.size(); i++) {
            gc.fillRect(platformsX.get(i), platformsY.get(i),
                    PLATFORM_WIDTH, PLATFORM_HEIGHT);
            gc.strokeRect(platformsX.get(i), platformsY.get(i),
                    PLATFORM_WIDTH, PLATFORM_HEIGHT);
        }

        // Draw ball
        gc.setFill(Color.RED);
        gc.fillOval(ballX - BALL_RADIUS, ballY - BALL_RADIUS,
                BALL_RADIUS * 2, BALL_RADIUS * 2);

        gc.strokeOval(ballX - BALL_RADIUS, ballY - BALL_RADIUS,
                BALL_RADIUS * 2, BALL_RADIUS * 2);
    }

    private void initializeGame() {
        platformsX = new ArrayList<>();
        platformsY = new ArrayList<>();
        platformsVisited = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            platformsX.add(randomPlatformsX[randomPlatformsXI]);
            if(randomPlatformsXI<499) {
                randomPlatformsXI++;
            }
            else {
                randomPlatformsXI = 0;
            }
            platformsY.add(HEIGHT - 100 - i * 100.0);
            if(i != 0) {
                platformsVisited.add(false);
            }
            else {
                platformsVisited.add(true);
            }
        }

        // Place ball on the first platform
        ballX = platformsX.get(0) + PLATFORM_WIDTH / 2;
        ballY = platformsY.get(0) - BALL_RADIUS;
        ballVelocityY = 0;
        ballVelocityX = 0;
    }

    private void gameOver() {
        highScoreGameController.gameOver(score);
        isGameOver = true;
    }

    public void restartGame() {
        score = 0;
        Platform.runLater(() -> currentScoreValue.setText(String.valueOf(score)));
        initializeGame();
        isGameOver = false;
    }

    public void startGame() {
        timeline.play();
    }

    public void terminateGame() {
        timeline.stop();
        isGameOver = true;
    }

    public void actionOnKeyPressed(String input) {
        activeKeys.add(input);

        if (input.equals("SPACE") && canJump && !isGameOver) {
            jump();
        }
    }

    public void actionOnKeyReleased(String input) {
        activeKeys.remove(input);
    }

}