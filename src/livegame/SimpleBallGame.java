package livegame;

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

import java.util.ArrayList;
import java.util.Random;
import java.util.HashSet;

public class SimpleBallGame extends Application {
    private static final int WIDTH = 400;
    private static final int HEIGHT = 560;
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

    
    private Timeline timeline;
    private Canvas canvas;
    private GraphicsContext gc;
    private Label currentScoreValue;
    private Text gameOverText = new Text("-10");
    private boolean isGameOver = false;
    private int score = 0;
    private int targetScore = 100;
    private HashSet<KeyCode> activeKeys;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        StackPane thisomo = new StackPane();
        thisomo.setBackground(new Background(new BackgroundFill(Color.web("#1976d2"), null, null)));
        thisomo.setAlignment(Pos.CENTER);
        thisomo.setPadding(new Insets(0, 0, 60, 0));

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
        currentScoreBox.setPrefSize(160, 100);
        currentScoreBox.setStyle("-fx-background-color: #2196f3; -fx-border-color: white; -fx-border-width: 3;");

        Label currentScoreLabel = new Label("Current Score");
        currentScoreLabel.setFont(Font.font("Poppins Medium", 16));
        currentScoreLabel.setTextFill(Color.WHITE);
        currentScoreValue = new Label("0");
        currentScoreValue.setFont(Font.font("Poppins", FontWeight.BOLD, 36));
        currentScoreValue.setTextFill(Color.WHITE);

        currentScoreBox.getChildren().addAll(currentScoreLabel, currentScoreValue);

        // VBox for "Target Score"
        VBox targetScoreBox = new VBox();
        targetScoreBox.setAlignment(javafx.geometry.Pos.CENTER);
        targetScoreBox.setPrefSize(160, 100);
        targetScoreBox.setStyle("-fx-background-color: #2196f3; -fx-border-color: white; -fx-border-width: 3;");

        Label targetScoreLabel = new Label("Target Score");
        targetScoreLabel.setFont(Font.font("Poppins Medium", 16));
        targetScoreLabel.setTextFill(Color.WHITE);

        Label targetScoreValue = new Label(String.valueOf(targetScore));
        targetScoreValue.setFont(Font.font("Poppins", FontWeight.BOLD, 36));
        targetScoreValue.setTextFill(Color.WHITE);

        targetScoreBox.getChildren().addAll(targetScoreLabel, targetScoreValue);

        sidePanel.getChildren().addAll(currentScoreBox, targetScoreBox);

        canvasContainer.getChildren().add(canvas);
        root.getChildren().addAll(canvasContainer, sidePanel);

        gameOverText.setFill(Color.RED);
        gameOverText.setStroke(Color.BLACK);
        gameOverText.setStrokeWidth(3.0);
        gameOverText.setStrokeType(StrokeType.OUTSIDE);
        gameOverText.setFont(Font.font("Poppins", FontWeight.BOLD , 100));
        gameOverText.setVisible(false);

        thisomo.getChildren().addAll(root, gameOverText);

        gc = canvas.getGraphicsContext2D();
        activeKeys = new HashSet<>();
        initializeGame();

        Scene scene = new Scene(new StackPane(thisomo));

        scene.setOnKeyPressed(e -> {
            activeKeys.add(e.getCode());

            if (e.getCode() == KeyCode.SPACE && canJump && !isGameOver) {
                jump();
            }
        });

        scene.setOnKeyReleased(e -> {
            activeKeys.remove(e.getCode());
        });

        timeline = new Timeline(new KeyFrame(Duration.millis(16), e -> {
            updateGame();
            renderGame();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);


        stage.setTitle("Simple Ball Jump");
        stage.setScene(scene);
        stage.show();

        timeline.play();
    }

    private void updateGame() {
        if (isGameOver)
            return;

        if (activeKeys.contains(KeyCode.LEFT)) {
            ballVelocityX = -MOVE_SPEED;
        } else if (activeKeys.contains(KeyCode.RIGHT)) {
            ballVelocityX = MOVE_SPEED;
        } else {
            ballVelocityX = 0;
        }
    
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
        Random rand = new Random();
        while (platformsY.size() < 10) {
            double lastPlatformY = platformsY.isEmpty() ? 0 : platformsY.get(platformsY.size() - 1);
            platformsX.add((double) rand.nextInt(WIDTH - PLATFORM_WIDTH));
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
        Random rand = new Random();

        for (int i = 0; i < 10; i++) {
            platformsX.add((double) rand.nextInt(WIDTH - PLATFORM_WIDTH));
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
        isGameOver = true;
        Platform.runLater(() -> {
            if(score-10 >= 0) {
                gameOverText.setText("-10");
                score -= 10;
            }
            else if(score>0) {
                gameOverText.setText("-"+score);
                score = 0;                        
            }
            else {
                gameOverText.setText("0");
                score = 0;                      
            }
            currentScoreValue.setText(Integer.toString(score));
            gameOverText.setVisible(true);
        });

        PauseTransition pause = new PauseTransition(Duration.seconds(1.5)); // 2-second pause
        pause.setOnFinished(e -> {
            initializeGame();
            isGameOver = false;

            Platform.runLater(() -> {
                gameOverText.setVisible(false);
            });
        });
        pause.play();
    }
}
