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

public class CarGame extends Application {
    // Window dimensions
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int CELL_SIZE = 40;
    
    // Game elements
    private static final int PLAYER_SIZE = 30;
    private static final double PLAYER_HORIZONTAL_SPEED = 5.0;
    private static final double GAME_SPEED = 3.0; // Speed of automatic forward movement
    
    // Game state
    private double playerX = WIDTH / 2;
    private double playerY = HEIGHT - 100;
    private double mapOffset = 0; // For scrolling effect
    private Set<KeyCode> activeKeys = new HashSet<>();
    
    // Game map (0: empty, 1: obstacle, 2: target)
    private int[][] gameMap = {
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0},
        {0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0},
        {0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0},
        {0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0},
        {0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0},
        {0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0},
        {0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0},
        {0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0},
        {0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0},
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
        // Handle horizontal movement
        if (activeKeys.contains(KeyCode.LEFT)) {
            playerX -= PLAYER_HORIZONTAL_SPEED;
        }
        if (activeKeys.contains(KeyCode.RIGHT)) {
            playerX += PLAYER_HORIZONTAL_SPEED;
        }
        
        // Keep player within bounds
        if (playerX < 0) playerX = 0;
        if (playerX > WIDTH - PLAYER_SIZE) playerX = WIDTH - PLAYER_SIZE;
        
        // Update map scroll position
        mapOffset += GAME_SPEED;
        
        // Check collision with obstacles and target
        int cellX = (int) (playerX / CELL_SIZE);
        int cellY = (int) ((playerY + mapOffset) / CELL_SIZE) % gameMap.length;
        
        if (cellX >= 0 && cellX < gameMap[0].length && 
            cellY >= 0 && cellY < gameMap.length) {
            
            if (gameMap[cellY][cellX] == 1) {
                // Collision with obstacle - reset position
                mapOffset = 0;
                playerX = WIDTH / 2;
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
        
        // Draw map with scrolling effect
        int startRow = (int) (mapOffset / CELL_SIZE);
        double offset = mapOffset % CELL_SIZE;
        
        for (int y = -1; y < HEIGHT / CELL_SIZE + 1; y++) {
            int mapY = (startRow + y) % gameMap.length;
            if (mapY < 0) mapY += gameMap.length;
            
            for (int x = 0; x < gameMap[0].length; x++) {
                if (gameMap[mapY][x] == 1) {
                    // Draw obstacle
                    gc.setFill(Color.RED);
                    gc.fillRect(x * CELL_SIZE, y * CELL_SIZE - offset, 
                              CELL_SIZE, CELL_SIZE);
                } else if (gameMap[mapY][x] == 2) {
                    // Draw target
                    gc.setFill(Color.GREEN);
                    gc.fillRect(x * CELL_SIZE, y * CELL_SIZE - offset, 
                              CELL_SIZE, CELL_SIZE);
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