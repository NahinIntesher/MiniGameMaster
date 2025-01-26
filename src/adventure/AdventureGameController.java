package adventure;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AdventureGameController {
    Rectangle2D screenBounds = Screen.getPrimary().getBounds();

    @FXML
    private Button backButton;


    private  final int WIDTH = (int)screenBounds.getWidth();
    private  final int HEIGHT = 700;
    private static final int TILE_SIZE = 50;

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
    private final List<ArcadeMachine> arcadeMachines = new ArrayList<>();

    private static final int MAP_WIDTH = 2000;
    private static final int MAP_HEIGHT = 1000;

    private String lastDirection = "RIGHT";
    private Image playerStandLeft;
    private Image playerStandRight;
    private Image playerMoveLeft1;
    private Image playerMoveLeft2;
    private Image playerMoveRight1;
    private Image playerMoveRight2;
    private Image currentPlayerImage;

    @FXML
    StackPane playground;

    private int animationFrame = 0;

    void resetGame() {
        playerX = 100;
        playerY = 500;
        playerVelocityX = 0;
        playerVelocityY = 0;

        onGround = true;
    }

    @FXML
    public void initialize() {
        backButton.setFocusTraversable(false);

        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        javafx.application.Platform.runLater(()->{
            Scene primaryScene = playground.getScene();
            primaryScene.setOnKeyPressed(event -> keysPressed.add(event.getCode()));
            primaryScene.setOnKeyReleased(event -> keysPressed.remove(event.getCode()));
        });

        // Load player images
        loadPlayerImages();

        // Initialize keys, door, and platforms
        initializeGameObjects();

        // Game loop
        AnimationTimer gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                render(gc);
            }
        };

        playground.getChildren().add(canvas);
        gameLoop.start();
    }

    private void loadPlayerImages() {
        playerStandLeft = new Image(getClass().getResource("/adventure/playerStandLeft.png").toExternalForm());
        playerStandRight = new Image(getClass().getResource("/adventure/playerStandRight.png").toExternalForm());
        playerMoveLeft1 = new Image(getClass().getResource("/adventure/playerMoveLeft1.png").toExternalForm());
        playerMoveLeft2 = new Image(getClass().getResource("/adventure/playerMoveLeft2.png").toExternalForm());
        playerMoveRight1 = new Image(getClass().getResource("/adventure/playerMoveRight1.png").toExternalForm());
        playerMoveRight2 = new Image(getClass().getResource("/adventure/playerMoveRight2.png").toExternalForm());
        playerMoveRight2 = new Image(getClass().getResource("/adventure/playerMoveRight2.png").toExternalForm());

        currentPlayerImage = playerStandRight;
    }

    private void initializeGameObjects() {
        keys.add(new Key(400, 400));
        keys.add(new Key(800, 300));
        keys.add(new Key(1200, 200));
        door = new Door(1700, 250);

        platforms.add(new Platform(0, 550, 400, 1));
        platforms.add(new Platform(400, 450, 200, 0));
        platforms.add(new Platform(650, 400, 250, 1));
        platforms.add(new Platform(1000, 350, 300, 0));
        platforms.add(new Platform(1400, 300, 400, 1));

        arcadeMachines.add(new ArcadeMachine(500, 400));
        arcadeMachines.add(new ArcadeMachine(700, 350));
    }

    private void update() {
        if (gameWon || gameOver)
            return;

        // Player movement
        if (keysPressed.contains(KeyCode.LEFT)) {
            playerVelocityX = -5;
            animatePlayer("LEFT");
            lastDirection = "LEFT";
        } else if (keysPressed.contains(KeyCode.RIGHT)) {
            playerVelocityX = 5;
            animatePlayer("RIGHT");
            lastDirection = "RIGHT";
        } else {
            playerVelocityX = 0;

            if (lastDirection.equals("LEFT")) {
                currentPlayerImage = playerStandLeft;
            } else {
                currentPlayerImage = playerStandRight;
            }
            // currentPlayerImage = lastDirection.equals("LEFT") ? playerStandLeft :
            // playerStandRight;
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
            resetGame();
            // gameOver = true;
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

    private void animatePlayer(String direction) {
        animationFrame = (animationFrame + 1) % 20; // Adjust frame speed
        if (direction.equals("LEFT")) {
            currentPlayerImage = animationFrame < 10 ? playerMoveLeft1 : playerMoveLeft2;
        } else if (direction.equals("RIGHT")) {
            currentPlayerImage = animationFrame < 10 ? playerMoveRight1 : playerMoveRight2;
        }
    }

    private void render(GraphicsContext gc) {
        // Calculate camera position
        double cameraX = Math.max(0, Math.min(playerX - WIDTH / 2.0, MAP_WIDTH - WIDTH));
        double cameraY = Math.max(0, Math.min(playerY - HEIGHT / 2.0, MAP_HEIGHT - HEIGHT));

        // Clear screen
        LinearGradient gradient = new LinearGradient(
                0, 0, 0, 1, // Start and End coordinates (left to right)
                true, // Proportional
                CycleMethod.NO_CYCLE, // No cycling of colors
                new Stop(1, Color.web("#6d4c41")), // Color at end (Blue)
                new Stop(0, Color.web("#3e2723")) // Color at start (Red)
        );

        gc.setFill(gradient);
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        // Draw visible part of the map
        gc.translate(-cameraX, -cameraY);

        // Draw ground
        // gc.setFill(Color.GREEN);
        // gc.fillRect(0, MAP_HEIGHT - TILE_SIZE, MAP_WIDTH, TILE_SIZE);

        // Draw platforms
        for (Platform platform : platforms) {
            platform.render(gc);
        }

        for (ArcadeMachine arcadeMachine : arcadeMachines) {
            arcadeMachine.render(gc);
        }

        // Draw player
        gc.drawImage(currentPlayerImage, playerX, playerY, TILE_SIZE, TILE_SIZE);

        // Draw keys
        for (Key key : keys) {
            key.render(gc);
        }

        door.render(gc, (keysCollected >= 3));

        // Reset translation
        gc.translate(cameraX, cameraY);

        // Draw UI
        gc.setFill(Color.WHITE);
        gc.setFont(new Font(20));
        gc.fillText("Keys Collected: " + keysCollected + "/3", 100, 20);

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

        private Image keyImage;

        public Key(double x, double y) {
            keyImage = new Image(getClass().getResource("/adventure/key.png").toExternalForm());
            this.x = x;
            this.y = y;
        }

        public boolean isCollected(double playerX, double playerY) {
            return playerX < x + TILE_SIZE && playerX + TILE_SIZE > x && playerY < y + TILE_SIZE
                    && playerY + TILE_SIZE > y;
        }

        public void render(GraphicsContext gc) {
            gc.drawImage(keyImage, x, y, TILE_SIZE, TILE_SIZE);
        }
    }

    static class Door {
        private final double x;
        private final double y;

        private Image openDoor;
        private Image closeDoor;

        public Door(double x, double y) {
            openDoor = new Image(getClass().getResource("/adventure/doorOpen.png").toExternalForm());
            closeDoor = new Image(getClass().getResource("/adventure/doorClosed.png").toExternalForm());

            this.x = x;
            this.y = y;
        }

        public boolean isTouched(double playerX, double playerY) {
            return playerX + 10 < x + TILE_SIZE && playerX + TILE_SIZE - 10 > x && playerY < y + TILE_SIZE
                    && playerY + TILE_SIZE > y;
        }

        public void render(GraphicsContext gc, boolean isOpend) {
            if (isOpend) {
                gc.drawImage(openDoor, x, y, TILE_SIZE, TILE_SIZE);
            } else {
                gc.drawImage(closeDoor, x, y, TILE_SIZE, TILE_SIZE);
            }
        }
    }

    static class ArcadeMachine {
        private Image arcadeMachineRun;
        private Image arcadeMachineDone;
        private final double x;
        private final double y;
        boolean completed;

        public ArcadeMachine(double x, double y) {
            arcadeMachineRun = new Image(getClass().getResource("/adventure/arcadeMachineRun.png").toExternalForm());
            arcadeMachineDone = new Image(getClass().getResource("/adventure/arcadeMachineDone.png").toExternalForm());
            this.x = x;
            this.y = y;
            completed = false;
        }

        public boolean isTouched(double playerX, double playerY) {
            return playerX + 10 < x + TILE_SIZE && playerX + TILE_SIZE - 10 > x && playerY < y + TILE_SIZE
                    && playerY + TILE_SIZE > y;
        }

        public void render(GraphicsContext gc) {
            gc.drawImage(arcadeMachineRun, x, y, TILE_SIZE, TILE_SIZE);
        }
    }

    static class Platform {
        private final double x;
        private final double y;
        private final double width;
        private final int texture;

        private Image textures[] = new Image[2];

        public Platform(double x, double y, double width, int texture) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.texture = texture;

            textures[0] = new Image(getClass().getResource("/adventure/ground0.png").toExternalForm());
            textures[1] = new Image(getClass().getResource("/adventure/ground1.png").toExternalForm());
        }

        public boolean isPlayerOnPlatform(double playerX, double playerY) {
            return playerX + TILE_SIZE - 10 > x && playerX + 10 < x + width && playerY + TILE_SIZE >= y
                    && playerY + TILE_SIZE <= y + 10;
        }

        public double getY() {
            return y;
        }

        public void render(GraphicsContext gc) {
            // gc.fillRect(x, y, width, TILE_SIZE);
            int totalTiles = (int) width / TILE_SIZE;

            for (int i = 0; i < totalTiles; i++) {
                gc.drawImage(textures[texture], x + (i * TILE_SIZE), y, TILE_SIZE, TILE_SIZE);
            }
        }
    }

    @FXML
    private void gotoHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../adventure/adventure.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) playground.getScene().getWindow();

            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());
            stage.setMaximized(true);

            stage.setScene(new Scene(root));
            stage.setTitle("Mini Game Master");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
