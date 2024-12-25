package livegame;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.util.HashSet;
import java.util.Set;

public class CarRacingGame extends Application {
    // Window dimensions
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int CELL_SIZE = 40;
    
    // Game elements
    private static final int PLAYER_SIZE = 30;
    private static final double PLAYER_SPEED = 5.0;
    
    // Game state
    private double playerX = 100;
    private double playerY = HEIGHT - 100;
    private Set<KeyCode> activeKeys = new HashSet<>();
    
    // Game map (0: empty, 1: obstacle, 2: target)
    private int[][] gameMap = {
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 1, 1, 0, 0, 0, 1, 1, 0, 0, 0, 1, 1, 0, 0, 0, 0, 2, 0},
        {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0},
        {0, 1, 1, 0, 0, 0, 0, 1, 1, 0, 0, 0, 1, 1, 0, 0, 0, 1, 1, 0},
        {0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0},
        {0, 0, 1, 1, 0, 0, 0, 1, 1, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0},
        {0, 1, 1, 0, 0, 0, 0, 1, 1, 0, 0, 0, 1, 1, 0, 0, 0, 1, 1, 0},
        {0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0},
        {0, 0, 1, 1, 0, 0, 0, 1, 1, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0},
        {0, 1, 1, 0, 0, 0, 0, 1, 1, 0, 0, 0, 1, 1, 0, 0, 0, 1, 1, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
    };

    @Override
    public void start(Stage primaryStage) {
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        Scene scene = new Scene(new StackPane(canvas));
        
        // Handle keyboard input
        scene.setOnKeyPressed(e -> activeKeys.add(e.getCode()));
        scene.setOnKeyReleased(e -> activeKeys.remove(e.getCode()));
        
        // Game loop
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                render(gc);
            }
        }.start();
        
        primaryStage.setTitle("Car Racing Game");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private void update() {
        double newX = playerX;
        double newY = playerY;
        
        // Handle player movement
        if (activeKeys.contains(KeyCode.LEFT)) newX -= PLAYER_SPEED;
        if (activeKeys.contains(KeyCode.RIGHT)) newX += PLAYER_SPEED;
        if (activeKeys.contains(KeyCode.UP)) newY -= PLAYER_SPEED;
        if (activeKeys.contains(KeyCode.DOWN)) newY += PLAYER_SPEED;
        
        // Check collision with map boundaries
        if (newX >= 0 && newX <= WIDTH - PLAYER_SIZE) {
            playerX = newX;
        }
        if (newY >= 0 && newY <= HEIGHT - PLAYER_SIZE) {
            playerY = newY;
        }
        
        // Check collision with obstacles and target
        int cellX = (int) (playerX / CELL_SIZE);
        int cellY = (int) (playerY / CELL_SIZE);
        
        if (cellX >= 0 && cellX < gameMap[0].length && 
            cellY >= 0 && cellY < gameMap.length) {
            
            if (gameMap[cellY][cellX] == 1) {
                // Collision with obstacle - reset position
                playerX = 100;
                playerY = HEIGHT - 100;
            } else if (gameMap[cellY][cellX] == 2) {
                // Reached target - show victory message
                System.out.println("You Win!");
                System.exit(0);
            }
        }
    }
    
    private void render(GraphicsContext gc) {
        // Clear screen
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(0, 0, WIDTH, HEIGHT);
        
        // Draw map
        for (int y = 0; y < gameMap.length; y++) {
            for (int x = 0; x < gameMap[y].length; x++) {
                if (gameMap[y][x] == 1) {
                    // Draw obstacle
                    gc.setFill(Color.RED);
                    gc.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                } else if (gameMap[y][x] == 2) {
                    // Draw target
                    gc.setFill(Color.GREEN);
                    gc.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }
        }
        
        // Draw player
        gc.setFill(Color.BLUE);
        gc.fillRect(playerX, playerY, PLAYER_SIZE, PLAYER_SIZE);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}