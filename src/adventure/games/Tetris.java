package adventure.games;

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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

import adventure.AdventureGameController;
import highscore.HighScoreGameController;

public class Tetris extends AdventureMiniGame {
    private static final int BOARD_WIDTH = 10;
    private static final int BOARD_HEIGHT = 16;
    private static final int CELL_SIZE = 30;
    private static final int PREVIEW_CELL_SIZE = 20;
    private static final int BOARD_PIXEL_WIDTH = BOARD_WIDTH * CELL_SIZE;
    private static final int BOARD_PIXEL_HEIGHT = BOARD_HEIGHT * CELL_SIZE;

    private GraphicsContext gc;
    private GraphicsContext nextBlockGc;
    private int[][] board = new int[BOARD_HEIGHT][BOARD_WIDTH];
    private int[][] currentTetromino = null;
    private int[][] nextTetromino = null;
    private int currentX, currentY;
    private Label currentScoreValue;
    private Boolean running = true;
    private int score = 0;
    private int targetScore;

    AdventureGameController adventureGameController;

    Random random = new Random();

    // Tetromino shapes
    private static final List<int[][]> TETROMINO_SHAPES = Arrays.asList(
            new int[][] { { 1, 1, 1, 1 } }, // I-shape
            new int[][] { { 1, 1 }, { 1, 1 } }, // Square
            new int[][] { { 1, 1, 1 }, { 0, 1, 0 } }, // T-shape
            new int[][] { { 1, 1, 1 }, { 1, 0, 0 } }, // L-shape
            new int[][] { { 1, 1, 1 }, { 0, 0, 1 } }, // Reverse L-shape
            new int[][] { { 1, 1, 0 }, { 0, 1, 1 } }, // S-shape
            new int[][] { { 0, 1, 1 }, { 1, 1, 0 } } // Z-shape
    );

    private static final Color[] COLORS = {
            Color.CYAN, // I-shape
            Color.YELLOW, // Square
            Color.PURPLE, // T-shape
            Color.ORANGE, // L-shape
            Color.BLUE, // Reverse L-shape
            Color.GREEN, // S-shape
            Color.RED // Z-shape
    };

    int[][] createTetromino() {
        int randomShapeNo = random.nextInt(7);
        int[][] newBlock = TETROMINO_SHAPES.get(randomShapeNo);

        for (int i = 0; i < newBlock.length; i++) {
            for (int j = 0; j < newBlock[i].length; j++) {
                if (newBlock[i][j] != 0) {
                    newBlock[i][j] = randomShapeNo + 1;
                }
            }
        }

        return newBlock;
    }

    void rotate() {
        int[][] rotated = new int[currentTetromino[0].length][currentTetromino.length];
        for (int i = 0; i < currentTetromino.length; i++) {
            for (int j = 0; j < currentTetromino[i].length; j++) {
                rotated[j][currentTetromino.length - 1 - i] = currentTetromino[i][j];
            }
        }
        currentTetromino = rotated;
    }

    public Tetris(AdventureGameController adventureGameController, int target) {
        this.setBackground(new Background(new BackgroundFill(Color.web("#B3B3B3"), null, null)));
        this.setAlignment(Pos.CENTER);
        // this.setPadding(new Insets(0, 0, 60, 0));
        this.adventureGameController = adventureGameController;
        this.targetScore = target;

        HBox root = new HBox();
        root.setAlignment(javafx.geometry.Pos.CENTER);
        root.setSpacing(20);

        Canvas canvas = new Canvas(BOARD_PIXEL_WIDTH, BOARD_PIXEL_HEIGHT);

        Canvas nextBlockCanvas = new Canvas(PREVIEW_CELL_SIZE * 4 + 10, PREVIEW_CELL_SIZE * 2 + 10);
        nextBlockGc = nextBlockCanvas.getGraphicsContext2D();

        StackPane canvasContainer = new StackPane();

        canvasContainer.setPrefWidth(BOARD_PIXEL_WIDTH + 20);
        canvasContainer.setPrefHeight(BOARD_PIXEL_HEIGHT + 20);
        canvasContainer.setMaxWidth(USE_PREF_SIZE);
        canvasContainer.setMaxHeight(USE_PREF_SIZE);
        canvasContainer.setAlignment(Pos.CENTER);
        canvasContainer.setBackground(new Background(new BackgroundFill(Color.web("#808080"), null, null)));

        gc = canvas.getGraphicsContext2D();

        currentScoreValue = new Label("0");
        VBox sidePanel = new VBox(20);
        sidePanel.setAlignment(javafx.geometry.Pos.CENTER);

        canvasContainer.getChildren().add(canvas);

        VBox currentScoreBox = new VBox();
        currentScoreBox.setAlignment(javafx.geometry.Pos.CENTER);
        currentScoreBox.setPrefSize(160, 100);
        currentScoreBox.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-border-width: 2;");

        Label currentScoreLabel = new Label("Current Score");
        currentScoreLabel.setFont(Font.font("Poppins Medium", 16));
        currentScoreLabel.setTextFill(Color.BLACK);

        currentScoreValue.setFont(Font.font("Poppins Bold", 32));
        currentScoreValue.setTextFill(Color.BLACK);

        currentScoreBox.getChildren().addAll(currentScoreLabel, currentScoreValue);

        // VBox for "Target Score"
        VBox targetScoreBox = new VBox();
        targetScoreBox.setAlignment(javafx.geometry.Pos.CENTER);
        targetScoreBox.setPrefSize(160, 100);
        targetScoreBox.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-border-width: 2;");

        Label targetScoreLabel = new Label("Target Score");
        targetScoreLabel.setFont(Font.font("Poppins Medium", 16));
        targetScoreLabel.setTextFill(Color.BLACK);

        Label targetScoreValue = new Label(String.valueOf(targetScore));
        targetScoreValue.setFont(Font.font("Poppins Bold", 32));
        targetScoreValue.setTextFill(Color.BLACK);

        targetScoreBox.getChildren().addAll(targetScoreLabel, targetScoreValue);

        // VBox for "Next Block"
        VBox nextBlockBox = new VBox();
        nextBlockBox.setAlignment(javafx.geometry.Pos.CENTER);
        nextBlockBox.setPrefSize(160, 120);
        nextBlockBox.setSpacing(10);
        nextBlockBox.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-border-width: 2;");

        Label nextBlockLabel = new Label("Next Block");
        nextBlockLabel.setFont(Font.font("Poppins Medium", 16));
        nextBlockLabel.setTextFill(Color.BLACK);

        nextBlockBox.getChildren().addAll(nextBlockLabel, nextBlockCanvas);

        sidePanel.getChildren().add(currentScoreBox);
        sidePanel.getChildren().add(targetScoreBox);
        sidePanel.getChildren().add(nextBlockBox);

        root.getChildren().add(canvasContainer);
        root.getChildren().add(sidePanel);

        getChildren().add(root);

        setupGameLoop(gc);
    }

    private void setupGameLoop(GraphicsContext gc) {
        new Thread(() -> {
            while (running) {
                try {
                    Thread.sleep(500); // Drop speed
                    Platform.runLater(() -> {
                        if (running) {
                            if (currentTetromino == null) {
                                spawnTetromino();
                            }
                            moveDown();
                            drawBoard(gc);
                        }
                    });
                } catch (InterruptedException e) {
                    break;
                }
            }
        }).start();
    }

    public void actionOnKeyPressed(String key) {
        if (currentTetromino == null)
            return;

        if (!running)
            return;

        switch (key) {
            case "LEFT" -> moveLeft();
            case "RIGHT" -> moveRight();
            case "DOWN" -> moveDown();
            case "UP" -> rotateTetromino();
            case "SPACE" -> rotateTetromino();
            case "ENTER" -> rotateTetromino();
        }

        drawBoard(gc);
    }

    private void spawnTetromino() {
        if (nextTetromino == null) {
            currentTetromino = createTetromino();
            nextTetromino = createTetromino();
        } else {
            currentTetromino = nextTetromino;
            nextTetromino = createTetromino();
        }

        currentX = BOARD_WIDTH / 2 - currentTetromino.length / 2;
        currentY = 0;

        drawNextBlockPreview();

        // Game over check
        if (checkCollision()) {
            resetGame();
        }
    }

    private void moveDown() {
        currentY++;
        if (checkCollision()) {
            currentY--;
            placeTetromino();
            clearLines();

            currentTetromino = null;
        }
    }

    private void moveLeft() {
        currentX--;
        if (checkCollision()) {
            currentX++;
        }
    }

    private void moveRight() {
        currentX++;
        if (checkCollision()) {
            currentX--;
        }
    }

    private void rotateTetromino() {
        rotate();
        if (checkCollision()) {
            rotate();
            rotate();
            rotate();
        }

    }

    private boolean checkCollision() {
        for (int row = 0; row < currentTetromino.length; row++) {
            for (int col = 0; col < currentTetromino[row].length; col++) {
                if (currentTetromino[row][col] == 0)
                    continue;

                int boardX = currentX + col;
                int boardY = currentY + row;

                // Check board boundaries
                if (boardX < 0 || boardX >= BOARD_WIDTH || boardY >= BOARD_HEIGHT) {
                    return true;
                }

                // Check collision with existing blocks
                if (boardY >= 0 && board[boardY][boardX] != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private void placeTetromino() {
        for (int row = 0; row < currentTetromino.length; row++) {
            for (int col = 0; col < currentTetromino[row].length; col++) {
                if (currentTetromino[row][col] != 0) {
                    int boardX = currentX + col;
                    int boardY = currentY + row;
                    if (boardY >= 0) {
                        board[boardY][boardX] = currentTetromino[row][col];
                    }
                }
            }
        }
    }

    private void clearLines() {
        // Collect fully complete lines from bottom to top
        List<Integer> linesToClear = new ArrayList<>();
        for (int row = BOARD_HEIGHT - 1; row >= 0; row--) {
            boolean fullLine = true;
            for (int col = 0; col < BOARD_WIDTH; col++) {
                // Ensure the block exists before checking
                if (board[row][col] == 0) {
                    fullLine = false;
                    break;
                }
            }
            if (fullLine) {
                linesToClear.add(row);
            }
        }

        // If no lines to clear, return
        if (linesToClear.isEmpty()) {
            return;
        }

        // Create a deep copy of the current board state
        int[][] newBoard = new int[BOARD_HEIGHT][BOARD_WIDTH];
        for (int row = 0; row < BOARD_HEIGHT; row++) {
            for (int col = 0; col < BOARD_WIDTH; col++) {
                newBoard[row][col] = board[row][col];
            }
        }

        // Clear the original board
        for (int row = 0; row < BOARD_HEIGHT; row++) {
            for (int col = 0; col < BOARD_WIDTH; col++) {
                board[row][col] = 0;
            }
        }

        // Rebuild the board without the cleared lines
        int newRow = BOARD_HEIGHT - 1;
        for (int row = BOARD_HEIGHT - 1; row >= 0; row--) {
            // Skip rows that were completely full
            if (linesToClear.contains(row)) {
                continue;
            }

            // Copy the non-cleared line to the new position
            for (int col = 0; col < BOARD_WIDTH; col++) {
                if (newBoard[row][col] != 0) {
                    board[newRow][col] = newBoard[row][col];
                }
            }
            newRow--;
        }

        // Bonus for multiple line clear

        int linesCleared = linesToClear.size();
        score += linesCleared * 10;

        if(targetScore<= score) {
            terminateGame();
            adventureGameController.gameCompleted();
        }

        // Update score label
        Platform.runLater(() -> currentScoreValue.setText(String.valueOf(score)));
    }

    private void resetGame() {
        for (int row = 0; row < BOARD_HEIGHT; row++) {
            for (int col = 0; col < BOARD_WIDTH; col++) {
                board[row][col] = 0;
            }
        }
        score = 0;
        Platform.runLater(() -> currentScoreValue.setText("0"));
    }


    private void drawBoard(GraphicsContext gc) {
        // Clear canvas
        gc.setFill(Color.web("#CCCCCC"));
        gc.fillRect(0, 0, BOARD_PIXEL_WIDTH, BOARD_PIXEL_HEIGHT);

        // Draw grid
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(1.0);
        for (int x = 0; x <= BOARD_PIXEL_WIDTH; x += CELL_SIZE) {
            gc.strokeLine(x, 0, x, BOARD_PIXEL_HEIGHT);
        }
        for (int y = 0; y <= BOARD_PIXEL_HEIGHT; y += CELL_SIZE) {
            gc.strokeLine(0, y, BOARD_PIXEL_WIDTH, y);
        }

        // Draw placed blocks with their original colors

        gc.setLineWidth(2.0);
        gc.setStroke(Color.BLACK);

        for (int row = 0; row < BOARD_HEIGHT; row++) {
            for (int col = 0; col < BOARD_WIDTH; col++) {
                if (board[row][col] != 0) {
                    gc.setFill(COLORS[board[row][col] - 1]);
                    gc.fillRect(col * CELL_SIZE, row * CELL_SIZE, CELL_SIZE, CELL_SIZE);

                    gc.strokeRect(col * CELL_SIZE, row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }
        }

        // Draw current tetromino
        gc.setStroke(Color.BLACK);
        if (currentTetromino != null) {
            for (int row = 0; row < currentTetromino.length; row++) {
                for (int col = 0; col < currentTetromino[row].length; col++) {
                    if (currentTetromino[row][col] != 0) {
                        gc.setFill(COLORS[currentTetromino[row][col] - 1]);
                        gc.fillRect(
                                (currentX + col) * CELL_SIZE,
                                (currentY + row) * CELL_SIZE,
                                CELL_SIZE,
                                CELL_SIZE);

                        gc.strokeRect(
                                (currentX + col) * CELL_SIZE,
                                (currentY + row) * CELL_SIZE,
                                CELL_SIZE,
                                CELL_SIZE);
                    }
                }
            }
        }
    }

    private void drawNextBlockPreview() {
        // Clear previous preview
        nextBlockGc.setFill(Color.WHITE);
        nextBlockGc.fillRect(0, 0, 150, 150);

        // Draw the next tetromino

        int offsetX = ((4 - nextTetromino[0].length) * PREVIEW_CELL_SIZE) / 2;
        int offsetY = ((2 - nextTetromino.length) * PREVIEW_CELL_SIZE) / 2;
        for (int row = 0; row < nextTetromino.length; row++) {
            for (int col = 0; col < nextTetromino[row].length; col++) {
                if (nextTetromino[row][col] != 0) {
                    nextBlockGc.setFill(COLORS[nextTetromino[row][col] - 1]);
                    nextBlockGc.fillRect(
                            col * PREVIEW_CELL_SIZE + 5 + offsetX,
                            row * PREVIEW_CELL_SIZE + 5 + offsetY,
                            PREVIEW_CELL_SIZE,
                            PREVIEW_CELL_SIZE);

                    nextBlockGc.setStroke(Color.BLACK);
                    nextBlockGc.setLineWidth(2.0);
                    nextBlockGc.strokeRect(
                            col * PREVIEW_CELL_SIZE + 5 + offsetX,
                            row * PREVIEW_CELL_SIZE + 5 + offsetY,
                            PREVIEW_CELL_SIZE,
                            PREVIEW_CELL_SIZE);
                }
            }
        }
    }

    public void terminateGame() {
        running = false;
    }
}