package livegame;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.util.HashSet;
import java.util.Set;

public class MiniGolf extends Application {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int CELL_SIZE = 30; // Size of each cell in the grid
    private static final int ROWS = HEIGHT / CELL_SIZE;
    private static final int COLS = WIDTH / CELL_SIZE;
    
    // Map elements
    private static final char EMPTY = '.';
    private static final char WALL = '#';
    private static final char HOLE = 'O';
    
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
    private double friction = 0.98;
    private boolean ballInHole = false;
    private int currentLevel = 0;
    private int strokeCount = 0;
    
    private Set<KeyCode> activeKeys = new HashSet<>();
    
    // Sample levels
    private static final String[][] LEVELS = {
        {
            "......##############",
            "......#............#",
            "#######............#",
            "#...............####",
            "#........####...#",
            "#...............####",
            "#......O...........#",
            "#..................#",
            "#...####...........#",
            "#..................#",
            "#..................#",
            "#..................#",
            "#..................#",
            "#..................#",
            "#..................#",
            "#..................#",
            "#..................#",
            "#..................#",
            "####################"
        },
        {
            "####################",
            "#..................#",
            "#...##..##.........#",
            "#....##............#",
            "#..................#",
            "#.....O............#",
            "#..................#",
            "#......##..........#",
            "#.......##.........#",
            "#..................#",
            "#..................#",
            "#..................#",
            "#..................#",
            "#..................#",
            "####################"
        }
    };
    
    @Override
    public void start(Stage primaryStage) {
        loadLevel(currentLevel);
        
        Pane root = new Pane();
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        root.getChildren().add(canvas);
        
        Scene scene = new Scene(root);
        
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
    
  
    private void update() {
        if (!ballInHole) {
            // Handle direction controls
            if (activeKeys.contains(KeyCode.LEFT)) {
                directionAngle -= 3;
            }
            if (activeKeys.contains(KeyCode.RIGHT)) {
                directionAngle += 3;
            }
            
            // Handle power meter
            if (activeKeys.contains(KeyCode.SPACE) && !isShooting) {
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
            
            if (isShooting) {
                // Apply friction first
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
                
                // Check surrounding cells for collision
                int currentRow = (int)(ballY / CELL_SIZE);
                int currentCol = (int)(ballX / CELL_SIZE);
                int nextRow = (int)(nextY / CELL_SIZE);
                int nextCol = (int)(nextX / CELL_SIZE);
                
                boolean collisionX = false;
                boolean collisionY = false;
                
                // Check horizontal movement
                if (isWall(nextRow, currentCol)) {
                    collisionY = true;
                }
                
                // Check vertical movement
                if (isWall(currentRow, nextCol)) {
                    collisionX = true;
                }
                
                // Check diagonal collision
                if (isWall(nextRow, nextCol) && !collisionX && !collisionY) {
                    collisionX = true;
                    collisionY = true;
                }
                
                // Handle collisions
                if (collisionX) {
                    velocityX *= -0.8; // Add some energy loss
                    nextX = ballX; // Prevent sticking by reverting X position
                }
                if (collisionY) {
                    velocityY *= -0.8; // Add some energy loss
                    nextY = ballY; // Prevent sticking by reverting Y position
                }
                
                // Check for hole
                if (nextRow >= 0 && nextRow < ROWS && nextCol >= 0 && nextCol < COLS) {
                    if (map[nextRow][nextCol] == HOLE) {
                        double holeCenterX = nextCol * CELL_SIZE + CELL_SIZE / 2;
                        double holeCenterY = nextRow * CELL_SIZE + CELL_SIZE / 2;
                        double distance = Math.sqrt(Math.pow(nextX - holeCenterX, 2) + 
                                                  Math.pow(nextY - holeCenterY, 2));
                        
                        if (distance < CELL_SIZE / 3) {
                            ballInHole = true;
                            isShooting = false;
                            return;
                        }
                    }
                }
                
                // Update ball position if within bounds
                if (isWithinBounds(nextX, nextY)) {
                    ballX = nextX;
                    ballY = nextY;
                } else {
                    // Bounce off screen edges
                    if (nextX <= ballRadius || nextX >= WIDTH - ballRadius) {
                        velocityX *= -0.8;
                    }
                    if (nextY <= ballRadius || nextY >= HEIGHT - ballRadius) {
                        velocityY *= -0.8;
                    }
                }
            }
        }
    }

    private boolean isWall(int row, int col) {
        if (row >= 0 && row < ROWS && col >= 0 && col < COLS) {
            return map[row][col] == WALL;
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
        gc.setFill(Color.LIGHTGREEN);
        gc.fillRect(0, 0, WIDTH, HEIGHT);
        
        // Draw map
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                double x = col * CELL_SIZE;
                double y = row * CELL_SIZE;
                
                switch (map[row][col]) {
                    case WALL:
                        gc.setFill(Color.DARKGRAY);
                        gc.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                        break;
                    case HOLE:
                        gc.setFill(Color.BLACK);
                        gc.fillOval(x + CELL_SIZE/4, y + CELL_SIZE/4, 
                                  CELL_SIZE/2, CELL_SIZE/2);
                        break;
                }
            }
        }
        
        // Draw direction indicator
        if (!isShooting && !ballInHole) {
            double angleRad = Math.toRadians(directionAngle);
            double lineLength = 50;
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
            gc.strokeLine(ballX, ballY,
                    ballX + Math.cos(angleRad) * lineLength,
                    ballY + Math.sin(angleRad) * lineLength);
        }
        
        // Draw power meter
        gc.setFill(Color.RED);
        gc.fillRect(20, HEIGHT - 40, power * 2, 20);
        gc.setStroke(Color.BLACK);
        gc.strokeRect(20, HEIGHT - 40, 200, 20);
        
        // Draw ball
        if (!ballInHole) {
            gc.setFill(Color.WHITE);
            gc.fillOval(ballX - ballRadius, ballY - ballRadius, 
                       ballRadius * 2, ballRadius * 2);
        }
        
        // Draw HUD
        gc.setFill(Color.BLACK);
        gc.fillText("Level: " + (currentLevel + 1) + "/" + LEVELS.length, 20, 30);
        gc.fillText("Strokes: " + strokeCount, 20, 50);
        
        if (ballInHole) {
            gc.setFill(Color.GREEN);
            gc.fillText("Hole in " + strokeCount + "! Press N for next level", 
                       WIDTH/2 - 100, HEIGHT/2);
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}

