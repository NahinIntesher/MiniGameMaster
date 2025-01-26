package highscore.games;

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
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;

import javax.swing.GroupLayout.Alignment;

import org.json.JSONArray;
import org.json.JSONObject;

import highscore.HighScoreGameController;

public class BrickBreaker extends HighScoreGame {
    HighScoreGameController highScoreGameController;

    private static final int WIDTH = 535;
    private static final int HEIGHT = 435;
    private static final int BRICK_ROWS = 8;
    private static final int BRICK_COLUMNS = 8;
    private static final int BRICK_WIDTH = 60;
    private static final int BRICK_HEIGHT = 25;
    private static final int PADDLE_WIDTH = 100;
    private static final int PADDLE_HEIGHT = 20;
    private static final int BALL_SIZE = 20;
    private static final int NEW_BRICK_COUNT = 1;

    private double paddleX = WIDTH / 2.0 - PADDLE_WIDTH / 2.0;
    private double ballX = WIDTH / 2.0 - BALL_SIZE / 2.0;
    private double ballY = HEIGHT - PADDLE_HEIGHT - 10;
    private double ballSpeedX = 4;
    private double ballSpeedY = 4;
    private int[][] bricks = new int[BRICK_ROWS][BRICK_COLUMNS];
    private int totalBricks = 0;
    private String[] map;
    private final Random random = new Random();
    private Label brickCountBoxValue;
    private Label timeCountBoxValue;
    private Timeline timeline;
    private boolean running = false;
    private HBox gameOver;

    
    int minutes = 0;
    int seconds = 0;

    String[][] mapsData = {
            {
                    "02000020",
                    "00011000",
                    "00033000",
                    "00000000",
                    "01000010",
                    "03300330",
                    "00011000",
                    "00033000"
            },
            {
                    "00100100",
                    "00033000",
                    "02000020",
                    "00000000",
                    "00033000",
                    "01100110",
                    "03300330",
                    "00000000"
            },
            {
                    "00000000",
                    "01000010",
                    "00011000",
                    "03300330",
                    "00000000",
                    "02011020",
                    "00300300",
                    "00000000"
            },
            {
                    "01000010",
                    "10000001",
                    "03300330",
                    "00000000",
                    "00011000",
                    "00000000",
                    "02033020",
                    "00000000"
            }
    };

    private boolean moveLeft = false;
    private boolean moveRight = false;

    public BrickBreaker(HighScoreGameController highScoreGameController) {
        this.highScoreGameController = highScoreGameController;
        // for(int i=0; i< mapDataJsonArray.length(); i++) {
        // mapInit[i] = mapDataJsonArray.getString(i);
        // }

        map = mapsData[random.nextInt(4)];

        this.setBackground(new Background(new BackgroundFill(Color.web("#424242"), null, null)));
        this.setAlignment(Pos.CENTER);

        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        initializeBricks();

        StackPane canvasContainer = new StackPane();
        canvasContainer.setPrefSize(WIDTH + 15, HEIGHT + 15);
        canvasContainer.setMaxSize(WIDTH + 15, HEIGHT + 15);
        canvasContainer.setAlignment(Pos.CENTER);
        canvasContainer.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));

        canvasContainer.getChildren().add(canvas);

        VBox root = new VBox();
        root.setAlignment(javafx.geometry.Pos.CENTER);
        root.setSpacing(15);

        // VBox for "Target Score"
        HBox brickCountBox = new HBox();
        brickCountBox.setAlignment(javafx.geometry.Pos.CENTER);
        brickCountBox.setPrefSize(265, 40);
        brickCountBox.setMaxSize(265, 40);
        brickCountBox.setSpacing(10);
        brickCountBox.setStyle("-fx-background-color: #666666; -fx-border-color: #ffffff; -fx-border-width: 2;");
        // HBox.setHgrow(targetScoreBox, Priority.ALWAYS);

        Label brickCountBoxLabel = new Label("Brick Left");
        brickCountBoxLabel.setFont(Font.font("Poppins Medium", 16));
        brickCountBoxLabel.setTextFill(Color.WHITE);

        brickCountBoxValue = new Label(Integer.toString((totalBricks)));
        brickCountBoxValue.setFont(Font.font("Poppins", FontWeight.BOLD, 22));
        brickCountBoxValue.setTextFill(Color.WHITE);

        brickCountBox.getChildren().addAll(brickCountBoxLabel, brickCountBoxValue);

        
        HBox timeCountBox = new HBox();
        timeCountBox.setAlignment(javafx.geometry.Pos.CENTER);
        timeCountBox.setPrefSize(265, 40);
        timeCountBox.setMaxSize(265, 40);
        timeCountBox.setSpacing(10);
        timeCountBox.setStyle("-fx-background-color: #666666; -fx-border-color: #ffffff; -fx-border-width: 2;");
        // HBox.setHgrow(targetScoreBox, Priority.ALWAYS);

        Label timeCountBoxLabel = new Label("Time: ");
        timeCountBoxLabel.setFont(Font.font("Poppins Medium", 16));
        timeCountBoxLabel.setTextFill(Color.WHITE);

        timeCountBoxValue = new Label("00:00");
        timeCountBoxValue.setFont(Font.font("Poppins", FontWeight.BOLD, 22));
        timeCountBoxValue.setTextFill(Color.WHITE);

        timeCountBox.getChildren().addAll(timeCountBoxLabel, timeCountBoxValue);

        gameOver = new HBox();
        gameOver.setSpacing(20);
        gameOver.setAlignment(Pos.CENTER);
        Text gameOverText1 = new Text("+1");
        gameOverText1.setFill(Color.RED);
        gameOverText1.setStroke(Color.WHITE);
        gameOverText1.setStrokeWidth(3.0);
        gameOverText1.setStrokeType(javafx.scene.shape.StrokeType.OUTSIDE);
        gameOverText1.setFont(Font.font("Poppins", FontWeight.BOLD, 80));

        Text gameOverText2 = new Text("Bricks");
        gameOverText2.setFill(Color.RED);
        gameOverText2.setStroke(Color.WHITE);
        gameOverText2.setStrokeWidth(3.0);
        gameOverText2.setStrokeType(javafx.scene.shape.StrokeType.OUTSIDE);
        gameOverText2.setFont(Font.font("Poppins", FontWeight.BOLD, 60));

        gameOver.getChildren().addAll(gameOverText1, gameOverText2);

        gameOver.setVisible(false);

        HBox countBoxContainer = new HBox();
        countBoxContainer.setSpacing(15);
        countBoxContainer.setAlignment(Pos.CENTER);
        countBoxContainer.getChildren().addAll(brickCountBox, timeCountBox);

        // Add VBoxes to HBox
        root.getChildren().addAll(canvasContainer, countBoxContainer);

        this.getChildren().add(root);
        this.getChildren().add(gameOver);

        timeline = new Timeline(new KeyFrame(Duration.millis(16), e -> {
            update();
            render(gc);
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);

        startGame();
        startTimer();
    }

    private void startTimer() {
        minutes = 0;
        seconds = 0;
        new Thread(() -> {
            try {
                while (running) {
                    for (seconds = 0; seconds < 60 && running; seconds++) {
                        Platform.runLater(() -> {
                            timeCountBoxValue.setText(String.format("%02d:%02d", minutes, seconds));
                        });
                        Thread.sleep(1000);
                    }
                    minutes++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void initializeBricks() {
        for (int row = 0; row < map.length; row++) {
            for (int col = 0; col < BRICK_COLUMNS; col++) {
                bricks[row][col] = map[row].charAt(col) - '0';
                if (bricks[row][col] == 1 || bricks[row][col] == 2) {
                    totalBricks++;
                }
            }
        }

        Platform.runLater(() -> {
            brickCountBoxValue.setText(Integer.toString(totalBricks));
        });
    }

    private void update() {
        // Move paddle
        if (moveLeft) {
            paddleX = Math.max(0, paddleX - 5);
        }
        if (moveRight) {
            paddleX = Math.min(WIDTH - PADDLE_WIDTH, paddleX + 5);
        }

        // Move ball
        ballX += ballSpeedX;
        ballY += ballSpeedY;

        // Ball collision with walls
        if (ballX <= 0 || ballX >= WIDTH - BALL_SIZE)
            ballSpeedX *= -1;
        if (ballY <= 0)
            ballSpeedY *= -1;

        // Ball collision with paddle
        if (ballY + BALL_SIZE >= HEIGHT - PADDLE_HEIGHT - 10 &&
                ballX + BALL_SIZE >= paddleX &&
                ballX <= paddleX + PADDLE_WIDTH) {

            boolean hitFromLeftOrRight = ballX + BALL_SIZE - ballSpeedX <= paddleX ||
                    ballX - ballSpeedX >= paddleX + PADDLE_WIDTH;

            if (hitFromLeftOrRight) {
                ballSpeedX *= -1; // Reverse horizontal speed
            } else {
                ballY = HEIGHT - PADDLE_HEIGHT - 10 - BALL_SIZE; // Avoid overlap
                ballSpeedY *= -1; // Reverse vertical speed
            }
        }

        // Ball collision with bricks
        for (int row = 0; row < BRICK_ROWS; row++) {
            for (int col = 0; col < BRICK_COLUMNS; col++) {
                if (bricks[row][col] != 0) {
                    double brickX = col * (BRICK_WIDTH + 5) + 10;
                    double brickY = row * (BRICK_HEIGHT + 5) + 10;
                    if (ballX + BALL_SIZE >= brickX && ballX <= brickX + BRICK_WIDTH &&
                            ballY + BALL_SIZE >= brickY && ballY <= brickY + BRICK_HEIGHT) {

                        boolean hitFromLeftOrRight = ballX + BALL_SIZE - ballSpeedX <= brickX ||
                                ballX - ballSpeedX >= brickX + BRICK_WIDTH;

                        if (hitFromLeftOrRight) {
                            ballSpeedX *= -1; // Reverse horizontal speed
                        } else {
                            ballSpeedY *= -1; // Reverse vertical speed
                        }
                        if (bricks[row][col] != 3) {
                            bricks[row][col]--;
                            if (bricks[row][col] == 0) {
                                totalBricks--;
                                if (totalBricks == 0) {
                                    terminateGame();
                                    highScoreGameController.gameOver(String.format("%02d:%02d", minutes, seconds));
                                }
                                Platform.runLater(() -> {
                                    brickCountBoxValue.setText(Integer.toString(totalBricks));
                                });
                            }
                        }
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
        gameOver.setVisible(true);
        timeline.pause();

        PauseTransition pause = new PauseTransition(Duration.seconds(1.5)); // 2-second pause
        pause.setOnFinished(e -> {
            ballX = WIDTH / 2.0 - BALL_SIZE / 2.0;
            ballY = HEIGHT - PADDLE_HEIGHT - 10;
            ballSpeedX = random.nextBoolean() ? 4 : -4;
            ballSpeedY = -4;
            paddleX = WIDTH / 2.0 - PADDLE_WIDTH / 2.0;
            gameOver.setVisible(false);
            timeline.play();
        });
        pause.play();
    }


    private void resetBallAndPaddleInstant() {
        ballX = WIDTH / 2.0 - BALL_SIZE / 2.0;
        ballY = HEIGHT - PADDLE_HEIGHT - 10;
        ballSpeedX = random.nextBoolean() ? 4 : -4;
        ballSpeedY = -4;
        paddleX = WIDTH / 2.0 - PADDLE_WIDTH / 2.0;
        gameOver.setVisible(false);
        timeline.play();
    }

    private void spawnNewBricks() {
        int count = 0;

        for (int row = 0; row < BRICK_ROWS; row++) {
            for (int col = 0; col < BRICK_COLUMNS; col++) {
                if (bricks[row][col] == 0) {
                    bricks[row][col] = 1;
                    count++;
                    row++;
                    totalBricks++;
                }
                if (count >= NEW_BRICK_COUNT)
                    break;
            }
            if (count >= NEW_BRICK_COUNT)
                break;
        }

        Platform.runLater(() -> {
            brickCountBoxValue.setText(Integer.toString(totalBricks));
        });
    }

    private void render(GraphicsContext gc) {
        // Clear canvas
        gc.setFill(Color.web("#333333"));
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        // Draw paddle
        gc.setFill(Color.web("#BDBDBD"));
        gc.fillRect(paddleX, HEIGHT - PADDLE_HEIGHT - 10, PADDLE_WIDTH, PADDLE_HEIGHT);

        // Draw ball
        gc.setFill(Color.web("#DDDDDD"));
        gc.fillOval(ballX, ballY, BALL_SIZE, BALL_SIZE);

        // Draw bricks
        gc.setFill(Color.GREEN);
        for (int row = 0; row < BRICK_ROWS; row++) {
            for (int col = 0; col < BRICK_COLUMNS; col++) {
                if (bricks[row][col] == 1) {
                    double x = col * (BRICK_WIDTH + 5) + 10;
                    double y = row * (BRICK_HEIGHT + 5) + 10;
                    gc.setFill(Color.web("#e67e22"));
                    gc.fillRect(x, y, BRICK_WIDTH, BRICK_HEIGHT);
                } else if (bricks[row][col] == 2) {
                    double x = col * (BRICK_WIDTH + 5) + 10;
                    double y = row * (BRICK_HEIGHT + 5) + 10;
                    gc.setFill(Color.web("#cb4335"));
                    gc.fillRect(x, y, BRICK_WIDTH, BRICK_HEIGHT);
                } else if (bricks[row][col] == 3) {
                    double x = col * (BRICK_WIDTH + 5) + 10;
                    double y = row * (BRICK_HEIGHT + 5) + 10;
                    gc.setFill(Color.web("#839192"));
                    gc.fillRect(x, y, BRICK_WIDTH, BRICK_HEIGHT);
                }
            }
        }
    }

    public void startGame() {
        timeline.play();
        running = true;
    }

    public void terminateGame() {
        timeline.stop();
        running = false;
    }

    public void actionOnKeyPressed(String input) {
        if (input.equals("LEFT") || input.equals("A"))
            moveLeft = true;
        if (input.equals("RIGHT") || input.equals("D"))
            moveRight = true;
    }

    public void actionOnKeyReleased(String input) {
        if (input.equals("LEFT") || input.equals("A"))
            moveLeft = false;
        if (input.equals("RIGHT") || input.equals("D"))
            moveRight = false;
    }

    public void restartGame() {
        map = mapsData[random.nextInt(4)];
        initializeBricks();
        resetBallAndPaddleInstant();
        startGame();
        startTimer();
    }

}
