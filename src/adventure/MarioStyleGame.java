package adventure;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MarioStyleGame extends Application {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int TILE_SIZE = 40;

    private double playerX = 100;
    private double playerY = 500;
    private double playerVelocityX = 0;
    private double playerVelocityY = 0;

    private boolean onGround = true;
    private final Set<KeyCode> keysPressed = new HashSet<>();

    private final List<Key> keys = new ArrayList<>();
    private Door door;
    private int keysCollected = 0;
    private boolean gameWon = false;
    private boolean gameOver = false;

    private final List<Platform> platforms = new ArrayList<>();

    private static final int MAP_WIDTH = 2000;
    private static final int MAP_HEIGHT = 1200;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        Pane root = new Pane(canvas);
        Scene scene = new Scene(root);

        // Initialize keys, door, and platforms
        initializeGameObjects();

        // Handle keyboard input
        scene.setOnKeyPressed(event -> keysPressed.add(event.getCode()));
        scene.setOnKeyReleased(event -> keysPressed.remove(event.getCode()));

        // Game loop
        AnimationTimer gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                render(gc);
            }
        };

        gameLoop.start();

        primaryStage.setTitle("Mario-Style 2D Game");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void initializeGameObjects() {
        keys.add(new Key(400, 400));
        keys.add(new Key(800, 300));
        keys.add(new Key(1200, 200));
        door = new Door(1800, 100);

        platforms.add(new Platform(0, 560, 400, Color.DARKGRAY));
        platforms.add(new Platform(400, 480, 200, Color.BLUE));
        platforms.add(new Platform(600, 400, 200, Color.ORANGE));
        platforms.add(new Platform(1000, 350, 300, Color.PURPLE));
        platforms.add(new Platform(1400, 300, 400, Color.CYAN));
    }

    private void update() {
        if (gameWon || gameOver) return;

        // Player movement
        if (keysPressed.contains(KeyCode.LEFT)) {
            playerVelocityX = -5;
        } else if (keysPressed.contains(KeyCode.RIGHT)) {
            playerVelocityX = 5;
        } else {
            playerVelocityX = 0;
        }

        if (keysPressed.contains(KeyCode.SPACE) && onGround) {
            playerVelocityY = -12;
            onGround = false;
        }

        playerX += playerVelocityX;
        playerY += playerVelocityY;

        // Gravity
        playerVelocityY += 0.5;

        // Ground and platform collision
        onGround = false;
        for (Platform platform : platforms) {
            if (platform.isPlayerOnPlatform(playerX, playerY)) {
                onGround = true;
                playerVelocityY = 0;
                playerY = platform.getY() - TILE_SIZE;
            }
        }

        // Check if player fell below the platforms
        if (playerY > MAP_HEIGHT) {
            gameOver = true;
        }

        // Key collection
        keys.removeIf(key -> key.isCollected(playerX, playerY));
        keysCollected = 3 - keys.size();

        // Door interaction
        if (keysCollected >= 3 && door.isTouched(playerX, playerY)) {
            gameWon = true;
        }

        // Prevent player from leaving map bounds
        playerX = Math.max(0, Math.min(playerX, MAP_WIDTH - TILE_SIZE));
    }

    private void render(GraphicsContext gc) {
        // Calculate camera position
        double cameraX = Math.max(0, Math.min(playerX - WIDTH / 2.0, MAP_WIDTH - WIDTH));
        double cameraY = Math.max(0, Math.min(playerY - HEIGHT / 2.0, MAP_HEIGHT - HEIGHT));

        // Clear screen
        gc.setFill(Color.LIGHTBLUE);
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        // Draw visible part of the map
        gc.translate(-cameraX, -cameraY);

        // Draw ground
        gc.setFill(Color.GREEN);
        gc.fillRect(0, MAP_HEIGHT - TILE_SIZE, MAP_WIDTH, TILE_SIZE);

        // Draw platforms
        for (Platform platform : platforms) {
            platform.render(gc);
        }

        // Draw player
        gc.setFill(Color.RED);
        gc.fillRect(playerX, playerY, TILE_SIZE, TILE_SIZE);

        // Draw keys
        for (Key key : keys) {
            key.render(gc);
        }

        // Draw door
        door.render(gc);

        // Reset translation
        gc.translate(cameraX, cameraY);

        // Draw UI
        gc.setFill(Color.BLACK);
        gc.setFont(new Font(20));
        gc.fillText("Keys Collected: " + keysCollected + "/3", 10, 20);

        // Game won message
        if (gameWon) {
            gc.setFill(Color.YELLOW);
            gc.setFont(new Font(40));
            gc.fillText("You Win!", WIDTH / 2 - 80, HEIGHT / 2);
        }

        // Game over message
        if (gameOver) {
            gc.setFill(Color.RED);
            gc.setFont(new Font(40));
            gc.fillText("Game Over!", WIDTH / 2 - 100, HEIGHT / 2);
        }
    }

    static class Key {
        private final double x;
        private final double y;

        public Key(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public boolean isCollected(double playerX, double playerY) {
            return playerX < x + TILE_SIZE && playerX + TILE_SIZE > x && playerY < y + TILE_SIZE && playerY + TILE_SIZE > y;
        }

        public void render(GraphicsContext gc) {
            gc.setFill(Color.GOLD);
            gc.fillRect(x, y, TILE_SIZE, TILE_SIZE);
        }
    }

    static class Door {
        private final double x;
        private final double y;

        public Door(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public boolean isTouched(double playerX, double playerY) {
            return playerX < x + TILE_SIZE && playerX + TILE_SIZE > x && playerY < y + TILE_SIZE && playerY + TILE_SIZE > y;
        }

        public void render(GraphicsContext gc) {
            gc.setFill(Color.BROWN);
            gc.fillRect(x, y, TILE_SIZE, TILE_SIZE * 2);
        }
    }

    static class Platform {
        private final double x;
        private final double y;
        private final double width;
        private final Color color;

        public Platform(double x, double y, double width, Color color) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.color = color;
        }

        public boolean isPlayerOnPlatform(double playerX, double playerY) {
            return playerX + TILE_SIZE > x && playerX < x + width && playerY + TILE_SIZE >= y && playerY + TILE_SIZE <= y + 10;
        }

        public double getY() {
            return y;
        }

        public void render(GraphicsContext gc) {
            gc.setFill(color);
            gc.fillRect(x, y, width, TILE_SIZE / 4);
        }
    }
}
