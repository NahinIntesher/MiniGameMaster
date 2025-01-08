package livegame;

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

import org.json.JSONArray;
import org.json.JSONObject;

public class BrickBreaker extends LiveGame {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String playerToken;
    private boolean self;

    private String message;
    private String messageType;
    private String getPlayerToken;
    private String gameStateType;
    private String gameStateData;

    private static final int WIDTH = 535;
    private static final int HEIGHT = 435;
    private static final int BRICK_ROWS = 8;
    private static final int BRICK_COLUMNS = 8;
    private static final int BRICK_WIDTH = 60;
    private static final int BRICK_HEIGHT = 25;
    private static final int PADDLE_WIDTH = 100;
    private static final int PADDLE_HEIGHT = 20;
    private static final int BALL_SIZE = 20;
    private static final int NEW_BRICK_COUNT = 2;

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
    private Timeline timeline;
    private boolean running = false;
    private HBox gameOver;

    private boolean moveLeft = false;
    private boolean moveRight = false;

    public BrickBreaker(JSONObject gameInitializeInfo, String roomId, String playerToken, boolean self) {
        this.playerToken = playerToken;
        this.self = self;

        JSONArray mapDataJsonArray = gameInitializeInfo.getJSONArray("mapData");
        String [] mapInit = new String[mapDataJsonArray.length()];

        for(int i=0; i< mapDataJsonArray.length(); i++) {
            mapInit[i] = mapDataJsonArray.getString(i);
        }

        map = mapInit;

        try {
            socket = new Socket(serverAddress, 12345);
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("joinRoom:" + roomId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.setBackground(new Background(new BackgroundFill(Color.web("#424242"), null, null)));
        this.setAlignment(Pos.CENTER);
        this.setPadding(new Insets(0, 0, 55, 0));

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
        brickCountBox.setPrefSize(300, 40);
        brickCountBox.setMaxSize(300, 40);
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

        gameOver = new HBox();
        gameOver.setSpacing(20);
        gameOver.setAlignment(Pos.CENTER);
        Text gameOverText1 = new Text("+2");
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

        // Add VBoxes to HBox
        root.getChildren().addAll(canvasContainer, brickCountBox);

        this.getChildren().add(root);
        this.getChildren().add(gameOver);

        timeline = new Timeline(new KeyFrame(Duration.millis(16), e -> {
            update();
            render(gc);
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);

        new Thread(() -> {
            try {
                while ((message = in.readLine()) != null) {
                    messageType = message.split(":")[0];

                    if (!self && message.startsWith("{")) {
                        JSONObject gameState = new JSONObject(message);
                        if (!gameState.getString("playerToken").equals(playerToken)) {
                            updateGameState(gameState);
                        }
                    }

                    if (!self && messageType.equals("brickBreaker")) {
                        getPlayerToken = message.split(":")[1];
                        gameStateType = message.split(":")[2];
                        if (!getPlayerToken.equals(playerToken)) {
                            gameStateData = message.split(":")[3];
                            if (gameStateType.equals("paddleX")) {
                                paddleX = Double.parseDouble(gameStateData);
                            }
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
                        terminateGame();
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        startGame();
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
    }

    private void update() {
        // Move paddle
        if (moveLeft) {
            paddleX = Math.max(0, paddleX - 5);
            out.println("brickBreaker:" + playerToken + ":paddleX:" + paddleX);
        }
        if (moveRight) {
            paddleX = Math.min(WIDTH - PADDLE_WIDTH, paddleX + 5);
            out.println("brickBreaker:" + playerToken + ":paddleX:" + paddleX);
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
        if (self && ballY + BALL_SIZE >= HEIGHT - PADDLE_HEIGHT - 10 &&
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

            sendGameState();
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
                                    if (self) {
                                        out.println("gameComplete:" + playerToken);
                                    }
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
        if (self && ballY >= HEIGHT) {
            resetBallAndPaddle();
            spawnNewBricks();
            sendGameState();
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

    private void sendGameState() {
        JSONObject gameState = new JSONObject();
        gameState.put("type", "gameState");
        gameState.put("game", "BrickBreaker");
        gameState.put("playerToken", playerToken);
        gameState.put("ballX", ballX);
        gameState.put("ballY", ballY);
        gameState.put("ballSpeedX", ballSpeedX);
        gameState.put("ballSpeedY", ballSpeedY);
        gameState.put("bricks", bricks);
        gameState.put("totalBricks", totalBricks);
        gameState.put("running", running);
        gameState.put("paddleX", paddleX);

        out.println(gameState.toString());
    }

    public void updateGameState(JSONObject gameState) {
        if (gameState.getString("game").equals("BrickBreaker")) {
            this.ballX = gameState.getDouble("ballX");
            this.ballY = gameState.getDouble("ballY");
            this.ballSpeedX = gameState.getDouble("ballSpeedX");
            this.ballSpeedY = gameState.getDouble("ballSpeedY");
            this.paddleX = gameState.getDouble("paddleX");
            if (gameState.getInt("totalBricks") != this.totalBricks) {
                this.totalBricks = gameState.getInt("totalBricks");

                Platform.runLater(() -> {
                    brickCountBoxValue.setText(Integer.toString(totalBricks));
                });
            }

            JSONArray jsonNewBricks = gameState.getJSONArray("bricks");

            int newBricks[][] = new int[BRICK_ROWS][BRICK_COLUMNS];
            ;

            for (int i = 0; i < BRICK_ROWS; i++) {
                JSONArray innerArray = jsonNewBricks.getJSONArray(i);
                for (int j = 0; j < BRICK_COLUMNS; j++) {
                    newBricks[i][j] = innerArray.getInt(j);
                }
            }
            this.bricks = newBricks;
        }
    }
}
