package livegame;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Random;

public class BrickBreakerGame extends Application {
    private static final int WIDTH = 595;
    private static final int HEIGHT = 500;
    private static final int BRICK_ROWS = 6;
    private static final int BRICK_COLUMNS = 8;
    private static final int BRICK_WIDTH = 60;
    private static final int BRICK_HEIGHT = 25;
    private static final int PADDLE_WIDTH = 100;
    private static final int PADDLE_HEIGHT = 20;
    private static final int BALL_SIZE = 20;
    private static final int NEW_BRICK_COUNT = 4;

    private double paddleX = WIDTH / 2.0 - PADDLE_WIDTH / 2.0;
    private double ballX = WIDTH / 2.0 - BALL_SIZE / 2.0;
    private double ballY = HEIGHT / 2.0;
    private double ballSpeedX = 4;
    private double ballSpeedY = 4;
    private final int[][] bricks = new int[BRICK_ROWS][BRICK_COLUMNS];
    private String[] map = {
        "10011001",
        "01100110",
        "11011011",
        "02200220",
        "20000002",
    };
    private final Random random = new Random();

    private boolean moveLeft = false;
    private boolean moveRight = false;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        initializeBricks();

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(16), e -> {
            update();
            render(gc);
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        Scene scene = new Scene(new StackPane(canvas));
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.LEFT) moveLeft = true;
            if (e.getCode() == KeyCode.RIGHT) moveRight = true;
        });

        scene.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.LEFT) moveLeft = false;
            if (e.getCode() == KeyCode.RIGHT) moveRight = false;
        });

        primaryStage.setScene(scene);
        primaryStage.setTitle("Brick Breaker Game");
        primaryStage.show();
    }

    private void initializeBricks() {
        for (int row = 0; row < map.length; row++) {
            for (int col = 0; col < BRICK_COLUMNS; col++) {
                bricks[row][col] = map[row].charAt(col)-'0';
            }
        }
    }

    private void update() {
        // Move paddle
        if (moveLeft) paddleX = Math.max(0, paddleX - 5);
        if (moveRight) paddleX = Math.min(WIDTH - PADDLE_WIDTH, paddleX + 5);

        // Move ball
        ballX += ballSpeedX;
        ballY += ballSpeedY;

        // Ball collision with walls
        if (ballX <= 0 || ballX >= WIDTH - BALL_SIZE) ballSpeedX *= -1;
        if (ballY <= 0) ballSpeedY *= -1;

        // Ball collision with paddle
        if (ballY + BALL_SIZE >= HEIGHT - PADDLE_HEIGHT &&
                ballX + BALL_SIZE >= paddleX &&
                ballX <= paddleX + PADDLE_WIDTH) {
            ballY = HEIGHT - PADDLE_HEIGHT - BALL_SIZE; // Avoid overlap
            ballSpeedY *= -1;
        }

        // Ball collision with bricks
        for (int row = 0; row < BRICK_ROWS; row++) {
            for (int col = 0; col < BRICK_COLUMNS; col++) {
                if (bricks[row][col] != 0) {
                    double brickX = col * (BRICK_WIDTH + 5) + 40;
                    double brickY = row * (BRICK_HEIGHT + 5) + 40;
                    if (ballX + BALL_SIZE >= brickX && ballX <= brickX + BRICK_WIDTH &&
                            ballY + BALL_SIZE >= brickY && ballY <= brickY + BRICK_HEIGHT) {
                        bricks[row][col]--; // Remove the brick
                        ballSpeedY *= -1;
                        break;
                    }
                }
            }
        }

        // Ball falls down
        if (ballY >= HEIGHT) {
            resetBallAndPaddle();
            spawnNewBricks();
        }
    }

    private void resetBallAndPaddle() {
        ballX = WIDTH / 2.0 - BALL_SIZE / 2.0;
        ballY = HEIGHT-PADDLE_HEIGHT;
        ballSpeedX = random.nextBoolean() ? 4 : -4;
        ballSpeedY = -4;
        paddleX = WIDTH / 2.0 - PADDLE_WIDTH / 2.0;
    }

    private void spawnNewBricks() {
        int count = 0;

        for (int row = 0; row < BRICK_ROWS; row++) {
            for (int col = 0; col < BRICK_COLUMNS; col++) {
                if(bricks[row][col] == 0) {
                    bricks[row][col] = 1; 
                    count++;
                }
                if(count >= NEW_BRICK_COUNT) break;
            }
            if(count >= NEW_BRICK_COUNT) break;
        }
    }

    private void render(GraphicsContext gc) {
        // Clear canvas
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        // Draw paddle
        gc.setFill(Color.BLUE);
        gc.fillRect(paddleX, HEIGHT - PADDLE_HEIGHT, PADDLE_WIDTH, PADDLE_HEIGHT);

        // Draw ball
        gc.setFill(Color.RED);
        gc.fillOval(ballX, ballY, BALL_SIZE, BALL_SIZE);

        // Draw bricks
        gc.setFill(Color.GREEN);
        for (int row = 0; row < BRICK_ROWS; row++) {
            for (int col = 0; col < BRICK_COLUMNS; col++) {
                if (bricks[row][col] == 1) {
                    double x = col * (BRICK_WIDTH + 5) + 40;
                    double y = row * (BRICK_HEIGHT + 5) + 40;
                    gc.setFill(Color.GREEN);
                    gc.fillRect(x, y, BRICK_WIDTH, BRICK_HEIGHT);
                } else if (bricks[row][col] == 2) {
                    double x = col * (BRICK_WIDTH + 5) + 40;
                    double y = row * (BRICK_HEIGHT + 5) + 40;
                    gc.setFill(Color.GRAY);
                    gc.fillRect(x, y, BRICK_WIDTH, BRICK_HEIGHT);
                }
            }
        }
    }
}
