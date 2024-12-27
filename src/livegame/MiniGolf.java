package livegame;

import javafx.animation.AnimationTimer;
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
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

public class MiniGolf extends LiveGame {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String playerToken;
    private boolean self;
    private String message;

    private AnimationTimer gameTimer;

    private static final int WIDTH = 500;
    private static final int HEIGHT = 400;
    private static final int CELL_SIZE = 25;
    private static final int ROWS = HEIGHT / CELL_SIZE;
    private static final int COLS = WIDTH / CELL_SIZE;

    // Map elements - added sand and water
    private static final char EMPTY = ' ';
    private static final char GRASS = '.';
    private static final char WALL = '#';
    private static final char OBST = '&';
    private static final char HOLE = 'O';
    private static final char SAND = 'S';
    private static final char WATER = 'W';

    // Added friction multiplier for sand
    private static final double SAND_FRICTION = 0.78;
    private static final double NORMAL_FRICTION = 0.98;

    // Previous variables remain the same, but friction is no longer final
    private double friction = NORMAL_FRICTION;

    private static final long WATER_RESET_DELAY = 1500; // 1.5 seconds in milliseconds

    // Updated sample levels with new hazards
    private String[] levelData;

    private char[][] map;
    // private double ballX = WIDTH / 2;
    // private double ballY = HEIGHT - 100;
    private double ballXDefault;
    private double ballYDefault;
    private double ballX;
    private double ballY;
    private double ballRadius = 10;
    private double directionAngle = -90;
    private double power = 0;
    private double powerIncrement = 2;
    private boolean isPowerIncreasing = true;
    private boolean isShooting = false;
    private double velocityX = 0;
    private double velocityY = 0;
    private boolean ballInHole = false;
    private boolean inWater = false;
    private int strokeCount = 0;
    private long waterTimestamp = 0;
    private double ballOpacity = 1.0;
    private boolean readyToShoot = true;

    
    private String messageType;
    private String getPlayerToken;
    private String gameStateType;
    private String gameStateData;
    private String powerData;

    Label totalShotValue = new Label(String.valueOf(strokeCount));

    private Set<String> activeKeys = new HashSet<>();

    public MiniGolf(JSONObject gameInitializeInfo, String roomId, String playerToken, boolean self) {
        this.playerToken = playerToken;
        this.self = self;

        JSONArray levelDataJsonArray = gameInitializeInfo.getJSONArray("levelData");
        String[] levelDataInit = new String[levelDataJsonArray.length()];

        for(int i=0; i< levelDataJsonArray.length(); i++) {
            levelDataInit[i] = levelDataJsonArray.getString(i);
        }

        levelData = levelDataInit;
        ballXDefault = gameInitializeInfo.getInt("defaultBallX");
        ballYDefault = gameInitializeInfo.getInt("defaultBallY");

        ballX = ballXDefault*CELL_SIZE;
        ballY = ballYDefault*CELL_SIZE;

        try {
            socket = new Socket(serverAddress, 12345);
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("joinRoom:" + roomId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        loadLevel();

        this.setBackground(new Background(new BackgroundFill(Color.web("#935116"), null, null)));
        this.setAlignment(Pos.CENTER);
        this.setPadding(new Insets(0, 0, 60, 0));

        Canvas canvas = new Canvas(WIDTH + 8, HEIGHT + 8);

        VBox root = new VBox();
        root.setAlignment(javafx.geometry.Pos.CENTER);
        root.setSpacing(15);

        root.getChildren().add(canvas);

        HBox hBox = new HBox();
        hBox.setAlignment(javafx.geometry.Pos.CENTER);
        hBox.setSpacing(15);
        hBox.setPrefSize(500, 50);
        hBox.setMaxSize(500, 50);

        // VBox for "Target Score"
        Canvas speedCanvas = new Canvas(308, 48);

        HBox totalShotBox = new HBox();
        totalShotBox.setSpacing(5);
        totalShotBox.setAlignment(javafx.geometry.Pos.CENTER);
        totalShotBox.setPrefSize(175, 50);
        totalShotBox.setMinSize(175, 50);
        totalShotBox.setStyle("-fx-background-color: #af601a; -fx-border-color: #FFFFFF; -fx-border-width: 2;");
        // HBox.setHgrow(totalShotBox, Priority.ALWAYS);

        Label totalShotLabel = new Label("Total Shots ");
        totalShotLabel.setFont(Font.font("Poppins Medium", 16));
        totalShotLabel.setTextFill(Color.WHITE);

        totalShotValue.setFont(Font.font("Poppins", FontWeight.BOLD, 20));
        totalShotValue.setTextFill(Color.WHITE);

        totalShotBox.getChildren().addAll(totalShotLabel, totalShotValue);

        // Add VBoxes to HBox
        hBox.getChildren().add(speedCanvas);
        hBox.getChildren().add(totalShotBox);

        root.getChildren().add(hBox);

        this.getChildren().add(root);

        GraphicsContext gc = canvas.getGraphicsContext2D();
        GraphicsContext speedGc = speedCanvas.getGraphicsContext2D();

        gameTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                render(gc, speedGc);
            }
        };

        gameTimer.start();

        new Thread(() -> {
            try {
                while ((message = in.readLine()) != null) {
                    messageType = message.split(":")[0];

                    if (!self && messageType.equals("gameState")) {
                        getPlayerToken = message.split(":")[1];
                        gameStateType = message.split(":")[2];
                        if (!getPlayerToken.equals(playerToken)) {
                            gameStateData = message.split(":")[3];
                            if(gameStateType.equals("directionAngle")) {
                                directionAngle = Double.parseDouble(gameStateData);
                            }
                            else if(gameStateType.equals("power")) {
                                power = Double.parseDouble(gameStateData);
                            }
                            else if(gameStateType.equals("shoot")) {
                                powerData = message.split(":")[4];
                                directionAngle = Double.parseDouble(gameStateData);
                                power = Double.parseDouble(powerData);
                                shoot();
                                strokeCount++;
                                Platform.runLater(() -> {
                                    totalShotValue.setText(String.valueOf(strokeCount));
                                });
                            }
                        }
                    }
                    else if(self && messageType.equals("gameState")) {
                        gameStateType = message.split(":")[2];
                        if(gameStateType.equals("readyToShoot")){
                            readyToShoot = true;
                        }
                    }

                    if (messageType.equals("gameComplete")) {
                        String fromPlayerToken = message.split(":")[1];
                        if (!self && !fromPlayerToken.equals(playerToken)) {
                            break;
                        }
                        if (self && fromPlayerToken.equals(playerToken)) {
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

    }

    private void loadLevel() {
        map = new char[ROWS][COLS];

        // Initialize empty map
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                map[row][col] = EMPTY;
            }
        }

        // Load level data
        for (int row = 0; row < levelData.length; row++) {
            String line = levelData[row];
            for (int col = 0; col < line.length() && col < COLS; col++) {
                map[row][col] = line.charAt(col);
            }
        }

        resetBall();
        ballInHole = false;
        strokeCount = 0;
        Platform.runLater(() -> {
            totalShotValue.setText("0");
        });
    }

    private void resetBall() {
        ballX = ballXDefault*CELL_SIZE;
        ballY = ballYDefault*CELL_SIZE;
        velocityX = 0;
        velocityY = 0;
        isShooting = false;
        if(!self) {
            out.println("gameState:"+playerToken+":readyToShoot:"+1);
            readyToShoot = true;
        }
        power = 0;
    }

    private boolean checkWallCollision(double x, double y) {
        // Check all corners of the ball's bounding box
        double[] testPoints = {
                x - ballRadius, y - ballRadius, // Top-left
                x + ballRadius, y - ballRadius, // Top-right
                x - ballRadius, y + ballRadius, // Bottom-left
                x + ballRadius, y + ballRadius // Bottom-right
        };

        // Check each corner point
        for (int i = 0; i < testPoints.length; i += 2) {
            int row = (int) (testPoints[i + 1] / CELL_SIZE);
            int col = (int) (testPoints[i] / CELL_SIZE);
            if (isWall(row, col)) {
                return true;
            }
        }
        return false;
    }

    private void handleWaterHazard() {
        if (!inWater) {
            inWater = true;
            waterTimestamp = System.currentTimeMillis();
            isShooting = false;
            if(!self) {
                out.println("gameState:"+playerToken+":readyToShoot:"+1);
                readyToShoot = true;
            }
            velocityX = 0;
            velocityY = 0;
        }
    }

    private void update() {
        if (!ballInHole && !inWater) {
            // Handle direction controls when not shooting
            if (!isShooting && readyToShoot) {
                if (activeKeys.contains("LEFT")) {
                    directionAngle -= 3;
                    out.println("gameState:"+playerToken+":directionAngle:"+directionAngle);
                }
                if (activeKeys.contains("RIGHT")) {
                    directionAngle += 3;
                    out.println("gameState:"+playerToken+":directionAngle:"+directionAngle);
                }

                // Handle power meter
                if (activeKeys.contains("SPACE")) {
                    if (isPowerIncreasing) {
                        power += powerIncrement;
                        if (power >= 100) {
                            isPowerIncreasing = false;
                        }
                    } else {
                        power -= powerIncrement;
                        if (power <= 0) {
                            isPowerIncreasing = true;
                        }
                    }
                    out.println("gameState:"+playerToken+":power:"+power);
                }
            }

            // Ball physics when shooting
            if (isShooting) {
                // Get current cell type for terrain effects
                int currentRow = (int) (ballY / CELL_SIZE);
                int currentCol = (int) (ballX / CELL_SIZE);
                char currentCell = getTileAt(currentRow, currentCol);

                // Handle water hazard
                if (currentCell == WATER) {
                    handleWaterHazard();
                    return;
                }

                // Update friction based on terrain
                friction = (currentCell == SAND) ? SAND_FRICTION : NORMAL_FRICTION;

                // Apply friction
                velocityX *= friction;
                velocityY *= friction;

                // Stop the ball if it's moving very slowly
                if (Math.abs(velocityX) < 0.1 && Math.abs(velocityY) < 0.1) {
                    isShooting = false;
                    if(!self) {
                        out.println("gameState:"+playerToken+":readyToShoot:"+1);
                        readyToShoot = true;
                    }
                    velocityX = 0;
                    velocityY = 0;
                    return;
                }

                // Calculate next position
                double nextX = ballX + velocityX;
                double nextY = ballY + velocityY;

                // Check collisions separately for X and Y movement
                boolean collisionX = false;
                boolean collisionY = false;

                // Test horizontal movement
                if (checkWallCollision(nextX, ballY)) {
                    collisionX = true;
                    nextX = ballX;
                    velocityX *= -0.8;
                }

                // Test vertical movement
                if (checkWallCollision(ballX, nextY)) {
                    collisionY = true;
                    nextY = ballY;
                    velocityY *= -0.8;
                }

                // If no collision detected yet, check diagonal movement
                if (!collisionX && !collisionY && checkWallCollision(nextX, nextY)) {
                    if (Math.abs(velocityX) > Math.abs(velocityY)) {
                        collisionX = true;
                        nextX = ballX;
                        velocityX *= -0.8;
                    } else {
                        collisionY = true;
                        nextY = ballY;
                        velocityY *= -0.8;
                    }
                }

                // Check for hole
                int nextRow = (int) (nextY / CELL_SIZE);
                int nextCol = (int) (nextX / CELL_SIZE);

                if (getTileAt(nextRow, nextCol) == HOLE) {
                    double holeCenterX = nextCol * CELL_SIZE + CELL_SIZE / 2;
                    double holeCenterY = nextRow * CELL_SIZE + CELL_SIZE / 2;
                    double distance = Math.sqrt(Math.pow(nextX - holeCenterX, 2) +
                            Math.pow(nextY - holeCenterY, 2));

                    // Calculate current ball speed
                    double currentSpeed = Math.sqrt(velocityX * velocityX + velocityY * velocityY);
                    double maxHoleSpeed = 5.0; // Adjust this value to change the maximum allowed speed

                    if (distance < CELL_SIZE / 2 && currentSpeed <= maxHoleSpeed) {
                        ballInHole = true;
                        isShooting = false;
                        return;
                    } else if (distance < CELL_SIZE / 3) {
                        // Ball hit hole but was too fast - bounce off
                        if (Math.abs(velocityX) > Math.abs(velocityY)) {
                            velocityX *= -0.8;
                        } else {
                            velocityY *= -0.8;
                        }
                    }
                }

                // Update ball position if within bounds
                if (isWithinBounds(nextX, nextY)) {
                    ballX = nextX;
                    ballY = nextY;
                } else {
                    // Handle screen edge collisions
                    if (nextX <= ballRadius || nextX >= WIDTH - ballRadius) {
                        velocityX *= -0.8;
                    }
                    if (nextY <= ballRadius || nextY >= HEIGHT - ballRadius) {
                        velocityY *= -0.8;
                    }
                }
            }
        } else if (inWater) {
            // Handle water animation
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - waterTimestamp;

            if (elapsedTime < WATER_RESET_DELAY) {
                // Calculate ball opacity based on elapsed time
                ballOpacity = Math.max(0, 1.0 - (elapsedTime / (double) WATER_RESET_DELAY));
            } else {
                // Reset after delay
                inWater = false;
                ballOpacity = 1.0;
                resetBall();
            }
        }
        else if(ballInHole) {
            terminateGame();
            if(self) {
                out.println("gameComplete:"+playerToken);
            }
        }
    }

    private char getTileAt(int row, int col) {
        if (row >= 0 && row < ROWS && col >= 0 && col < COLS) {
            return map[row][col];
        }
        return WALL;
    }

    private boolean isWall(int row, int col) {
        if (row >= 0 && row < ROWS && col >= 0 && col < COLS) {
            return map[row][col] == WALL || map[row][col] == OBST;
        }
        return true; // Treat out of bounds as walls
    }

    private boolean isWithinBounds(double x, double y) {
        return x >= ballRadius && x <= WIDTH - ballRadius &&
                y >= ballRadius && y <= HEIGHT - ballRadius;
    }

    private void shoot() {
        if (!isShooting) {
            double angleRad = Math.toRadians(directionAngle);
            double speed = power * 0.5;
            velocityX = Math.cos(angleRad) * speed;
            velocityY = Math.sin(angleRad) * speed;
            isShooting = true;
            readyToShoot = false;
            power = 0;
        }
    }

    private void render(GraphicsContext gc, GraphicsContext speedGc) {
        // Clear screen
        gc.setFill(Color.web("#935116"));
        gc.fillRect(0, 0, WIDTH + 8, HEIGHT + 8);

        // Draw map
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                double x = col * CELL_SIZE + 4;
                double y = row * CELL_SIZE + 4;

                switch (map[row][col]) {
                    case GRASS:
                        if ((row + col) % 2 == 0) {
                            gc.setFill(Color.web("#63ca00"));
                        } else {
                            gc.setFill(Color.web("#70e300"));
                        }
                        gc.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                        break;
                    case HOLE:
                        if ((row + col) % 2 == 0) {
                            gc.setFill(Color.web("#63ca00"));
                        } else {
                            gc.setFill(Color.web("#70e300"));
                        }
                        gc.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                        gc.setFill(Color.BLACK);
                        gc.fillOval(x + 2, y + 2,
                                CELL_SIZE - 4, CELL_SIZE - 4);
                        break;
                    case SAND:
                        gc.setFill(Color.web("#f4d03f"));
                        gc.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                        break;
                    case WATER:
                        gc.setFill(Color.web("#2e86c1"));
                        gc.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                        break;
                }
            }
        }

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                double x = col * CELL_SIZE + 4;
                double y = row * CELL_SIZE + 4;

                if (map[row][col] == WALL || map[row][col] == OBST) {
                    gc.setStroke(Color.BLACK);
                    gc.setLineWidth(4.0);
                    gc.strokeRect(x, y, CELL_SIZE, CELL_SIZE);
                }
            }
        }

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                double x = col * CELL_SIZE + 4;
                double y = row * CELL_SIZE + 4;

                if (map[row][col] == WALL) {
                    gc.setFill(Color.web("#e67e22"));
                    gc.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                } else if (map[row][col] == OBST) {
                    gc.setFill(Color.web("#CCCCCC"));
                    gc.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                }
            }
        }

        // Rest of the render method remains the same
        if (readyToShoot && !isShooting && !ballInHole && !inWater) {
            double angleRad = Math.toRadians(directionAngle);
            double lineLength = 50;
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(4);
            gc.strokeLine(ballX + 4, ballY + 4,
                    ballX + 4 + Math.cos(angleRad) * lineLength,
                    ballY + 4 + Math.sin(angleRad) * lineLength);
        }

        // Draw power meter
        if ((!self && !isShooting) || (readyToShoot && !inWater && !isShooting && activeKeys.contains("SPACE"))) {
            speedGc.setFill(Color.web("#935116"));
            speedGc.fillRect(0, 0, 308, 48);
            speedGc.setFill(new LinearGradient(
                    0, 0, // Start point (x1, y1)
                    1, 1, // End point (x2, y2)
                    true, // Proportional (true = relative to the shape's size)
                    CycleMethod.NO_CYCLE, // No cycling of the gradient
                    new Stop(0, Color.GREEN), // Start color
                    new Stop(0.5, Color.YELLOW), // Middle color
                    new Stop(1, Color.RED) // End color
            ));
            speedGc.fillRect(4, 4, 300, 40);
            speedGc.setFill(Color.WHITE);
            speedGc.fillRect(4 + (power * 3), 4, 300 - (power * 3), 40);
            speedGc.setLineWidth(4.0);
            speedGc.setStroke(Color.BLACK);
            speedGc.strokeRect(4, 4, 300, 40);
        } else if (self && !isShooting && !inWater && !ballInHole) {
            speedGc.setFill(Color.web("#935116"));
            speedGc.fillRect(0, 0, 308, 48);
            speedGc.setFill(Color.WHITE);
            speedGc.fillRect(4, 4, 300, 40);
            if(readyToShoot) {
                speedGc.setFill(Color.BLACK);
                speedGc.setFont(Font.font("Poppins", 20));
                speedGc.fillText("Hold", 85, 32);
                speedGc.setFont(Font.font("Poppins", FontWeight.BOLD, 28));
                speedGc.fillText("SPACE", 140, 34);   
            }
            speedGc.setLineWidth(4.0);
            speedGc.setStroke(Color.BLACK);
            speedGc.strokeRect(4, 4, 300, 40);
        }

        // Draw ball
        if (!ballInHole && !inWater) {
            gc.setFill(Color.WHITE);
            gc.setLineWidth(4.0);
            gc.setStroke(Color.BLACK);
            gc.strokeOval(ballX + 4 - ballRadius, ballY + 4 - ballRadius,
                    ballRadius * 2, ballRadius * 2);
            gc.fillOval(ballX + 4 - ballRadius, ballY + 4 - ballRadius,
                    ballRadius * 2, ballRadius * 2);
        } else if (inWater) {
            // Draw fading ball
            gc.setStroke(Color.rgb(0, 0, 0, ballOpacity));
            gc.strokeOval(ballX + 4 - ballRadius, ballY + 4 - ballRadius,
                    ballRadius * 2, ballRadius * 2);

            gc.setFill(Color.rgb(255, 255, 255, ballOpacity));
            gc.fillOval(ballX + 4 - ballRadius, ballY + 4 - ballRadius,
                    ballRadius * 2, ballRadius * 2);

            // Draw ripple effect
            double rippleRadius = ballRadius * (2.0 - ballOpacity);
            gc.setStroke(Color.rgb(255, 255, 255, ballOpacity * 0.5));
            gc.setLineWidth(8);
            gc.strokeOval(ballX - rippleRadius, ballY - rippleRadius,
                    rippleRadius * 2, rippleRadius * 2);
        }
    }

    public void actionOnKeyPressed(String input) {
        activeKeys.add(input);
    }

    public void actionOnKeyReleased(String input) {
        activeKeys.remove(input);
        
        if (input.equals(("LEFT")) || input.equals(("RIGHT"))) {
            out.println("gameState:"+playerToken+":directionAngle:"+directionAngle);
        }

        if (input.equals(("SPACE")) && !isShooting && !ballInHole && !inWater && readyToShoot) {
            out.println("gameState:"+playerToken+":shoot:"+directionAngle+":"+power);
            shoot();
            strokeCount++;
            Platform.runLater(() -> {
                totalShotValue.setText(String.valueOf(strokeCount));
            });
        }
    }

    public void terminateGame() {
        gameTimer.stop();
    }
}
