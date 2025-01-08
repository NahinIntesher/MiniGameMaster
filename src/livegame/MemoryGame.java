package livegame;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MemoryGame extends Application {
    private List<MemoryCard> cards = new ArrayList<>();
    private MemoryCard firstCard = null;
    private boolean canClick = true;

    @Override
    public void start(Stage primaryStage) {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(10);

        // Create pairs of shapes
        List<Shape> shapes = new ArrayList<>();
        // Circle
        for (int i = 0; i < 2; i++) {
            Circle circle = new Circle(20, Color.YELLOW);
            shapes.add(circle);
        }
        // Square
        for (int i = 0; i < 2; i++) {
            Rectangle square = new Rectangle(40, 40, Color.RED);
            shapes.add(square);
        }
        // Triangle
        for (int i = 0; i < 2; i++) {
            Polygon triangle = createTriangle(40, Color.BLUE);
            shapes.add(triangle);
        }

        // Shuffle the shapes
        Collections.shuffle(shapes);

        // Create memory cards
        int index = 0;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                if (index < shapes.size()) {
                    MemoryCard card = new MemoryCard(shapes.get(index));
                    cards.add(card);
                    grid.add(card, col, row);
                    
                    final int cardIndex = index;
                    // Add event handler to the button inside MemoryCard
                    card.getButton().setOnAction(e -> handleCardClick(cardIndex));
                    index++;
                }
            }
        }

        Scene scene = new Scene(grid, 400, 300);
        primaryStage.setTitle("Memory Game");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Polygon createTriangle(double size, Color color) {
        Polygon triangle = new Polygon();
        triangle.getPoints().addAll(
            0.0, size,    // Point 1: bottom-left
            size/2, 0.0,  // Point 2: top-middle
            size, size    // Point 3: bottom-right
        );
        triangle.setFill(color);
        return triangle;
    }

    private void handleCardClick(int index) {
        if (!canClick) return;
        
        MemoryCard card = cards.get(index);
        if (card.isMatched() || card.isRevealed()) return;

        card.reveal();

        if (firstCard == null) {
            firstCard = card;
        } else {
            canClick = false;
            
            if (shapesMatch(firstCard.getCardShape(), card.getCardShape())) {
                firstCard.setMatched(true);
                card.setMatched(true);
                firstCard = null;
                canClick = true;
            } else {
                // Hide cards after delay
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    javafx.application.Platform.runLater(() -> {
                        firstCard.hide();
                        card.hide();
                        firstCard = null;
                        canClick = true;
                    });
                }).start();
            }
        }
    }

    private boolean shapesMatch(Shape shape1, Shape shape2) {
        // Compare shape types and properties
        return shape1.getClass().equals(shape2.getClass()) &&
               shape1.getFill().equals(shape2.getFill());
    }

    public static void main(String[] args) {
        launch(args);
    }
}

class MemoryCard extends StackPane {
    private Shape cardShape;
    private Button cover;
    private boolean matched = false;
    private boolean revealed = false;

    public MemoryCard(Shape shape) {
        this.cardShape = shape;
        this.cardShape.setVisible(false);
        
        cover = new Button("?");  // Add text to make it more visible
        cover.setPrefSize(60, 60);
        cover.setStyle("-fx-background-color: lightgreen; -fx-border-color: darkgreen; -fx-font-size: 20px;");
        cover.setFocusTraversable(true);  // Enable focus for keyboard navigation

        getChildren().addAll(cardShape, cover);
    }

    public Button getButton() {
        return cover;
    }

    public void reveal() {
        cover.setVisible(false);
        cardShape.setVisible(true);
        revealed = true;
    }

    public void hide() {
        if (!matched) {
            cover.setVisible(true);
            cardShape.setVisible(false);
            revealed = false;
        }
    }

    public Shape getCardShape() {
        return cardShape;
    }

    public boolean isMatched() {
        return matched;
    }

    public void setMatched(boolean matched) {
        this.matched = matched;
    }

    public boolean isRevealed() {
        return revealed;
    }
}