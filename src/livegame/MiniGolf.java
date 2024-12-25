package livegame;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import java.util.HashSet;
import java.util.Set;

public class MiniGolf extends Application {
    private static final int WIDTH = 600;
    private static final int HEIGHT = 480;
    private static final int CELL_SIZE = 30;
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
    private static final String[][] LEVELS = {
            {
                    "####################",
                    "#..................#",
                    "#..&&&&....WWWW....#",
                    "#.........WWWW.....#",
                    "#...........&&&&...#",
                    "#.....SSSS.......###",
                    "#......O.........#",
                    "#.....SSSS.......###",
                    "#...&&&&...........#",
                    "#..............W...#",
                    "####..........WWW..#",
                    "   #...........W...#",
                    "####...............#",
                    "#..................#",
                    "#..................#",
                    "####################"
            },
            {
                    "####################",
                    "#..................#",
                    "#...&&&&&&.........#",
                    "#....&&.SSSS.......#",
                    "#.......SSSS.......#",
                    "#.....O............#",
                    "#..................#",
                    "#......&&.WWW.....W#",
                    "#.......&&&WWWW...W#",
                    "#........WWWW.....W#",
                    "#..................#",
                    "#..................#",
                    "#..................#",
                    "#..................#",
                    "#..................#",
                    "####################"
            }
    };

    private char[][] map;
    // private double ballX = WIDTH / 2;
    // private double ballY = HEIGHT - 100;
    private double ballX = 180;
    private double ballY = 140;
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
    private int currentLevel = 0;
    private int strokeCount = 0;
    private long waterTimestamp = 0;
    private double ballOpacity = 1.0;

    private Set<KeyCode> activeKeys = new HashSet<>();

    @Override
    public void start(Stage primaryStage) {
        loadLevel(currentLevel);

        StackPane thisomo = new StackPane();
        Canvas canvas = new Canvas(WIDTH+8, HEIGHT+8);

        VBox root = new VBox();
        root.setAlignment(javafx.geometry.Pos.CENTER);
        root.setSpacing(10);

        root.getChildren().add(canvas);


        HBox hBox = new HBox();
        hBox.setAlignment(javafx.geometry.Pos.CENTER);
        hBox.setSpacing(10);
        hBox.setPrefSize(500, 90);
        hBox.setMaxSize(500, 90);

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
        hBox.getChildren().add(targetScoreBox);


        root.getChildren().add(hBox);

        thisomo.getChildren().add(root);

        Scene scene = new Scene(thisomo);

        scene.setOnKeyPressed(e -> {
            activeKeys.add(e.getCode());
            if (e.getCode() == KeyCode.R) {
                resetLevel();
            }
            if (e.getCode() == KeyCode.N && ballInHole) {
                nextLevel();
            }
        });

        scene.setOnKeyReleased(e -> {
            activeKeys.remove(e.getCode());
            if (e.getCode() == KeyCode.SPACE && !isShooting && !ballInHole) {
                shoot();
                strokeCount++;
            }
        });

        GraphicsContext gc = canvas.getGraphicsContext2D();

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                render(gc);
            }
        }.start();

        primaryStage.setTitle("Mini Golf");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void loadLevel(int level) {
        map = new char[ROWS][COLS];
        String[] levelData = LEVELS[level];

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
    }

    private void resetLevel() {
        loadLevel(currentLevel);
    }

    private void nextLevel() {
        currentLevel = (currentLevel + 1) % LEVELS.length;
        loadLevel(currentLevel);
    }

    private void resetBall() {
        ballX = WIDTH / 2;
        ballY = HEIGHT - 100;
        velocityX = 0;
        velocityY = 0;
        isShooting = false;
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
            velocityX = 0;
            velocityY = 0;
        }
    }

    private void update() {
        if (!ballInHole && !inWater) {
            // Handle direction controls when not shooting
            if (!isShooting) {
                if (activeKeys.contains(KeyCode.LEFT)) {
                    directionAngle -= 3;
                }
                if (activeKeys.contains(KeyCode.RIGHT)) {
                    directionAngle += 3;
                }

                // Handle power meter
                if (activeKeys.contains(KeyCode.SPACE)) {
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
        }
        else if (inWater) {
            // Handle water animation
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - waterTimestamp;
            
            if (elapsedTime < WATER_RESET_DELAY) {
                // Calculate ball opacity based on elapsed time
                ballOpacity = Math.max(0, 1.0 - (elapsedTime / (double)WATER_RESET_DELAY));
            } else {
                // Reset after delay
                inWater = false;
                ballOpacity = 1.0;
                resetBall();
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
            power = 0;
        }
    }

    private void render(GraphicsContext gc) {
        // Clear screen
        gc.setFill(Color.web("#1e8449"));
        gc.fillRect(0, 0, WIDTH+8, HEIGHT+8);

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
        if (!isShooting && !ballInHole && !inWater) {
            double angleRad = Math.toRadians(directionAngle);
            double lineLength = 50;
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
            gc.strokeLine(ballX+4, ballY+4,
                    ballX+4 + Math.cos(angleRad) * lineLength,
                    ballY+4 + Math.sin(angleRad) * lineLength);
        }

        // Draw power meter
        gc.setFill(Color.RED);
        gc.fillRect(20, HEIGHT - 40, power * 2, 20);
        gc.setStroke(Color.BLACK);
        gc.strokeRect(20, HEIGHT - 40, 200, 20);

        // Draw ball
        if (!ballInHole && !inWater) {
            gc.setFill(Color.WHITE);
            gc.setLineWidth(4.0);
            gc.setStroke(Color.BLACK);
            gc.strokeOval(ballX+4 - ballRadius, ballY+4 - ballRadius,
                    ballRadius * 2, ballRadius * 2);
            gc.fillOval(ballX+4 - ballRadius, ballY+4 - ballRadius,
                    ballRadius * 2, ballRadius * 2);
        }
        else if (inWater) {
            // Draw fading ball
            gc.setStroke(Color.rgb(0,0,0, ballOpacity));
            gc.strokeOval(ballX+4 - ballRadius, ballY+4 - ballRadius,
                    ballRadius * 2, ballRadius * 2);

            gc.setFill(Color.rgb(255, 255, 255, ballOpacity));
            gc.fillOval(ballX+4 - ballRadius, ballY+4 - ballRadius, 
                       ballRadius * 2, ballRadius * 2);
            
            // Draw ripple effect
            double rippleRadius = ballRadius * (2.0 - ballOpacity);
            gc.setStroke(Color.rgb(255, 255, 255, ballOpacity * 0.5));
            gc.setLineWidth(8);
            gc.strokeOval(ballX - rippleRadius, ballY - rippleRadius, 
                         rippleRadius * 2, rippleRadius * 2);
        }

        // Draw HUD
        gc.setFill(Color.BLACK);
        gc.fillText("Level: " + (currentLevel + 1) + "/" + LEVELS.length, 20, 30);
        gc.fillText("Strokes: " + strokeCount, 20, 50);

        if (ballInHole) {
            gc.setFill(Color.GREEN);
            gc.fillText("Hole in " + strokeCount + "! Press N for next level",
                    WIDTH / 2 - 100, HEIGHT / 2);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
