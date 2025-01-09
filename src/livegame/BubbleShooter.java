package livegame;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class BubbleShooter extends Application {

    private static final int WIDTH = 420;
    private static final int HEIGHT = 600;
    private static final int BUBBLE_RADIUS = 20;
    private static final int HEX_ROW_OFFSET = (int) (BUBBLE_RADIUS * Math.sqrt(3));
    private static final int NUM_ROWS = 6;
    private static final int NUM_COLS = 10;
    private static final int SHOOTER_Y = HEIGHT - 100;
    private static final double SNAP_DISTANCE = BUBBLE_RADIUS * 2.1; // Slightly larger than diameter

    private List<Bubble> bubbles = new ArrayList<>();
    private Bubble shooterBubble;
    private double shooterX = WIDTH / 2.0;
    private double shooterAngle = 90; // Facing straight up

    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean spacePressed = false;
    private boolean isShooting = false;

    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        Pane root = new Pane(canvas);
        Scene scene = new Scene(root);

        initBubbles();
        shooterBubble = new Bubble(WIDTH / 2.0, SHOOTER_Y, randomColor());

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.LEFT)
                leftPressed = true;
            if (event.getCode() == KeyCode.RIGHT)
                rightPressed = true;
            if (event.getCode() == KeyCode.SPACE)
                spacePressed = true;
        });

        scene.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.LEFT)
                leftPressed = false;
            if (event.getCode() == KeyCode.RIGHT)
                rightPressed = false;
            if (event.getCode() == KeyCode.SPACE)
                spacePressed = false;
        });

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                render(gc);
            }
        };

        timer.start();

        stage.setScene(scene);
        stage.setTitle("Bubble Shooter Game");
        stage.show();
    }

    private void initBubbles() {
        Random random = new Random();
        for (int row = 0; row < NUM_ROWS; row++) {
            for (int col = 0; col < NUM_COLS; col++) {
                double x = col * 2 * BUBBLE_RADIUS + BUBBLE_RADIUS;
                double y = row * HEX_ROW_OFFSET + BUBBLE_RADIUS;
                if (row % 2 != 0) {
                    x += BUBBLE_RADIUS; // Offset for hexagonal grid
                }
                bubbles.add(new Bubble(x, y, randomColor()));
            }
        }
    }

    private Color randomColor() {
        Color[] colors = { Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.PURPLE };
        return colors[new Random().nextInt(colors.length)];
    }

    private void update() {
        if (leftPressed) {
            shooterAngle = Math.max(10, shooterAngle - 2);
        }

        if (rightPressed) {
            shooterAngle = Math.min(170, shooterAngle + 2);
        }

        if (spacePressed && shooterBubble != null && !isShooting) {
            double radian = Math.toRadians(shooterAngle);
            double dx = Math.cos(radian) * 10;
            double dy = -Math.sin(radian) * 10;
            shooterBubble.vx = dx;
            shooterBubble.vy = dy;
            isShooting = true;
            spacePressed = false;
        }

        if (shooterBubble != null && isShooting) {
            shooterBubble.move();
            checkWallCollision(shooterBubble);
            if (checkCollision()) {
                isShooting = false;
                shooterBubble = new Bubble(WIDTH / 2.0, SHOOTER_Y, randomColor());
            }
        }
    }

    private void render(GraphicsContext gc) {
        gc.clearRect(0, 0, WIDTH, HEIGHT);

        // Draw bubbles
        for (Bubble bubble : bubbles) {
            bubble.draw(gc);
        }

        // Draw shooter bubble
        if (shooterBubble != null) {
            shooterBubble.draw(gc);
        }

        // Draw shooter guide
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(2);
        double radian = Math.toRadians(shooterAngle);
        double endX = shooterX + Math.cos(radian) * 100;
        double endY = SHOOTER_Y - Math.sin(radian) * 100;
        gc.strokeLine(shooterX, SHOOTER_Y, endX, endY);
    }

    private void checkWallCollision(Bubble bubble) {
        if (bubble.x - BUBBLE_RADIUS < 0 || bubble.x + BUBBLE_RADIUS > WIDTH) {
            bubble.vx = -bubble.vx; // Reflect horizontally
        }
    }

    private boolean checkCollision() {
        if (shooterBubble.y - BUBBLE_RADIUS <= 0) {
            snapToGrid(shooterBubble, null);
            return true;
        }

        for (Bubble bubble : bubbles) {
            if (shooterBubble.collidesWith(bubble)) {
                snapToGrid(shooterBubble, bubble);
                return true;
            }
        }
        return false;
    }

    private void findConnectedBubbles(Bubble bubble, Set<Bubble> connected, Set<Bubble> visited) {
        if (visited.contains(bubble))
            return;

        visited.add(bubble);
        connected.add(bubble);

        for (Bubble neighbor : findNeighbors(bubble)) {
            if (!visited.contains(neighbor)) {
                findConnectedBubbles(neighbor, connected, visited);
            }
        }
    }

    private List<Bubble> findCluster(Bubble bubble, List<Bubble> cluster, Set<Bubble> visited, Color targetColor) {
        if (visited.contains(bubble) || !bubbles.contains(bubble) || !bubble.color.equals(targetColor)) {
            return cluster;
        }

        visited.add(bubble);
        cluster.add(bubble);

        for (Bubble neighbor : findNeighbors(bubble)) {
            findCluster(neighbor, cluster, visited, targetColor);
        }

        return cluster;
    }

    private List<Bubble> findNeighbors(Bubble bubble) {
        List<Bubble> neighbors = new ArrayList<>();
        for (Bubble other : bubbles) {
            if (bubble != other && bubble.isNeighbor(other)) {
                neighbors.add(other);
            }
        }
        return neighbors;
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static class Bubble {
        double x, y;
        double vx = 0, vy = 0;
        Color color;

        Bubble(double x, double y, Color color) {
            this.x = x;
            this.y = y;
            this.color = color;
        }

        void move() {
            x += vx;
            y += vy;
        }

        void draw(GraphicsContext gc) {
            gc.setFill(color);
            gc.setStroke(Color.BLACK);
            gc.fillOval(x - BUBBLE_RADIUS, y - BUBBLE_RADIUS, BUBBLE_RADIUS * 2, BUBBLE_RADIUS * 2);
            gc.strokeOval(x - BUBBLE_RADIUS, y - BUBBLE_RADIUS, BUBBLE_RADIUS * 2, BUBBLE_RADIUS * 2);
        }

        boolean collidesWith(Bubble other) {
            double dx = this.x - other.x;
            double dy = this.y - other.y;
            double distance = Math.sqrt(dx * dx + dy * dy);
            return distance < BUBBLE_RADIUS * 2;
        }

        boolean isNeighbor(Bubble other) {
            double dx = this.x - other.x;
            double dy = this.y - other.y;
            double distance = Math.sqrt(dx * dx + dy * dy);
            return distance < BUBBLE_RADIUS * 2.5; // Slightly larger threshold for neighbors
        }
    }

    // Add new constant for grid snapping
    // ... (previous methods remain the same until snapToGrid)

    private void snapToGrid(Bubble movingBubble, Bubble targetBubble) {
        // Find all possible grid positions around the collision point
        List<GridPosition> possiblePositions = new ArrayList<>();

        if (targetBubble == null) {
            // Hitting the top wall
            int col = (int) Math.round((movingBubble.x - BUBBLE_RADIUS) / (2 * BUBBLE_RADIUS));
            possiblePositions.add(new GridPosition(0, col));
        } else {
            // Get all six surrounding grid positions
            int baseRow = (int) Math.round((targetBubble.y - BUBBLE_RADIUS) / HEX_ROW_OFFSET);
            int baseCol = (int) Math.round((targetBubble.x - BUBBLE_RADIUS) / (2 * BUBBLE_RADIUS));

            // Add all possible positions around the target bubble
            possiblePositions.addAll(Arrays.asList(
                    new GridPosition(baseRow - 1, baseCol), // Top
                    new GridPosition(baseRow - 1, baseCol + 1), // Top right
                    new GridPosition(baseRow, baseCol + 1), // Right
                    new GridPosition(baseRow + 1, baseCol), // Bottom
                    new GridPosition(baseRow + 1, baseCol - 1), // Bottom left
                    new GridPosition(baseRow, baseCol - 1) // Left
            ));
        }

        // Find the best valid position
        GridPosition bestPosition = findBestPosition(movingBubble, possiblePositions);

        if (bestPosition != null) {
            // Calculate the final position
            double finalX = bestPosition.col * 2 * BUBBLE_RADIUS + BUBBLE_RADIUS;
            if (bestPosition.row % 2 != 0) {
                finalX += BUBBLE_RADIUS;
            }
            double finalY = bestPosition.row * HEX_ROW_OFFSET + BUBBLE_RADIUS;

            // Create and add the new bubble
            Bubble newBubble = new Bubble(finalX, finalY, movingBubble.color);

            // Verify the position is valid before adding
            if (isValidPosition(newBubble)) {
                bubbles.add(newBubble);
                checkMatches(newBubble);
            }
        }
    }

    private GridPosition findBestPosition(Bubble movingBubble, List<GridPosition> positions) {
        GridPosition bestPos = null;
        double bestDistance = Double.MAX_VALUE;

        for (GridPosition pos : positions) {
            // Calculate actual coordinates for this grid position
            double x = pos.col * 2 * BUBBLE_RADIUS + BUBBLE_RADIUS;
            if (pos.row % 2 != 0) {
                x += BUBBLE_RADIUS;
            }
            double y = pos.row * HEX_ROW_OFFSET + BUBBLE_RADIUS;

            // Check if this position would be connected to existing bubbles or ceiling
            Bubble tempBubble = new Bubble(x, y, movingBubble.color);
            if (isValidPosition(tempBubble)) {
                double distance = getDistance(movingBubble.x, movingBubble.y, x, y);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestPos = pos;
                }
            }
        }

        return bestPos;
    }

    private boolean isValidPosition(Bubble bubble) {
        // Check if the position is within bounds
        if (bubble.x - BUBBLE_RADIUS < 0 || bubble.x + BUBBLE_RADIUS > WIDTH ||
                bubble.y - BUBBLE_RADIUS < 0) {
            return false;
        }

        // Check if the position overlaps with any existing bubble
        for (Bubble existing : bubbles) {
            if (getDistance(bubble.x, bubble.y, existing.x, existing.y) < BUBBLE_RADIUS * 1.8) {
                return false;
            }
        }

        // Position is valid if it's in the top row
        if (bubble.y <= HEX_ROW_OFFSET) {
            return true;
        }

        // Check if the bubble is connected to any existing bubble
        boolean hasNeighbor = false;
        for (Bubble existing : bubbles) {
            double distance = getDistance(bubble.x, bubble.y, existing.x, existing.y);
            if (distance <= SNAP_DISTANCE) {
                hasNeighbor = true;
                break;
            }
        }

        return hasNeighbor;
    }

    private double getDistance(double x1, double y1, double x2, double y2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        return Math.sqrt(dx * dx + dy * dy);
    }

    // Helper class for grid positions
    private static class GridPosition {
        final int row;
        final int col;

        GridPosition(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }

    // ... (rest of the code remains the same)

    private void checkMatches(Bubble startBubble) {
        Set<Bubble> visited = new HashSet<>();
        List<Bubble> cluster = findCluster(startBubble, new ArrayList<>(), visited, startBubble.color);

        if (cluster.size() >= 3) {
            // Remove matched cluster
            bubbles.removeAll(cluster);

            // Immediately after removing the cluster, check and remove floating bubbles
            Set<Bubble> floatingBubbles = findFloatingBubbles();
            bubbles.removeAll(floatingBubbles);
        }
    }

    private Set<Bubble> findFloatingBubbles() {
        Set<Bubble> connectedToCeiling = new HashSet<>();
        Set<Bubble> visited = new HashSet<>();

        // First pass: find all bubbles connected to the ceiling
        for (Bubble bubble : new ArrayList<>(bubbles)) { // Create a copy of bubbles list to avoid concurrent
                                                         // modification
            if (bubble.y <= HEX_ROW_OFFSET) { // If bubble is in the top row
                findConnectedBubbles(bubble, connectedToCeiling, visited);
            }
        }

        // All bubbles not connected to ceiling are floating
        Set<Bubble> floatingBubbles = new HashSet<>(bubbles);
        floatingBubbles.removeAll(connectedToCeiling);

        return floatingBubbles;
    }
}