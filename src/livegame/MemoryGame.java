package livegame;

import javafx.application.Application;
import javafx.application.Platform;
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
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MemoryGame extends Application {
    private List<MemoryCard> cards = new ArrayList<>();
    private MemoryCard firstCard = null;
    private boolean canClick = true;
    private static final double SHAPE_SIZE = 45; // Increased shape size
    private int count = 0;

    private Label countLabel;

    @Override
    public void start(Stage primaryStage) {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);
        grid.setBackground(new Background(new BackgroundFill(
            Color.rgb(40, 44, 52), CornerRadii.EMPTY, Insets.EMPTY)));

        List<Shape> shapes = new ArrayList<>();
        
        // Circle (Purple)
        for (int i = 0; i < 2; i++) {
            Circle circle = new Circle(SHAPE_SIZE/2, Color.PURPLE);
            circle.setStroke(Color.BLACK);
            circle.setStrokeWidth(2);
            shapes.add(circle);
        }
        
        // Square (Red)
        for (int i = 0; i < 2; i++) {
            Rectangle square = new Rectangle(SHAPE_SIZE-4, SHAPE_SIZE-4, Color.RED);
            square.setStroke(Color.BLACK);
            square.setStrokeWidth(2);
            shapes.add(square);
        }
        
        // Triangle (Blue)
        for (int i = 0; i < 2; i++) {
            Polygon triangle = createRegularPolygon(3, SHAPE_SIZE/2, Color.BLUE);
            shapes.add(triangle);
        }
        
        // Star (Gold)
        for (int i = 0; i < 2; i++) {
            Polygon star = createStar(SHAPE_SIZE/2, Color.GOLD);
            shapes.add(star);
        }
        
        // Pentagon (Green)
        for (int i = 0; i < 2; i++) {
            Polygon pentagon = createRegularPolygon(5, SHAPE_SIZE/2, Color.GREEN);
            shapes.add(pentagon);
        }
        
        // Hexagon (Orange)
        for (int i = 0; i < 2; i++) {
            Polygon hexagon = createRegularPolygon(6, SHAPE_SIZE/2, Color.ORANGE);
            shapes.add(hexagon);
        }
        
        // Upside-down Triangle (Cyan)
        for (int i = 0; i < 2; i++) {
            Polygon invertedTriangle = createRegularPolygon(3, SHAPE_SIZE/2, Color.CYAN);
            invertedTriangle.setRotate(180);
            shapes.add(invertedTriangle);
        }
        
        // Diamond (Pink)
        for (int i = 0; i < 2; i++) {
            Polygon diamond = createDiamond(SHAPE_SIZE, Color.PINK);
            shapes.add(diamond);
        }

        Collections.shuffle(shapes);

        int index = 0;
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                if (index < shapes.size()) {
                    MemoryCard card = new MemoryCard(shapes.get(index));
                    cards.add(card);
                    grid.add(card, col, row);
                    
                    final int cardIndex = index;
                    card.getButton().setOnAction(e -> handleCardClick(cardIndex));
                    index++;
                }
            }
        }

        VBox root = new VBox();
        countLabel = new Label("Moves: " + count);
        root.getChildren().add(countLabel);
        root.getChildren().add(grid);

        Scene scene = new Scene(root);
        primaryStage.setTitle("Memory Game");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Polygon createDiamond(double size, Color color) {
        Polygon diamond = new Polygon();
        diamond.getPoints().addAll(
            size/2, 0.0,    // Top
            size, size/2,   // Right
            size/2, size,   // Bottom
            0.0, size/2     // Left
        );
        diamond.setFill(color);
        diamond.setStroke(Color.BLACK);
        diamond.setStrokeWidth(2);
        return diamond;
    }

    private Polygon createRegularPolygon(int sides, double radius, Color color) {
        Polygon polygon = new Polygon();
        polygon.setFill(color);
        polygon.setStroke(Color.BLACK);
        polygon.setStrokeWidth(2);
        
        double angleStep = 2 * Math.PI / sides;
        for (int i = 0; i < sides; i++) {
            polygon.getPoints().addAll(
                radius * Math.cos(i * angleStep),
                radius * Math.sin(i * angleStep)
            );
        }
        return polygon;
    }

    private Polygon createStar(double radius, Color color) {
        Polygon star = new Polygon();
        star.setFill(color);
        star.setStroke(Color.BLACK);
        star.setStrokeWidth(2);
        
        int points = 5;
        double innerRadius = radius * 0.5;
        
        for (int i = 0; i < points * 2; i++) {
            double r = (i % 2 == 0) ? radius : innerRadius;
            double angle = Math.PI * i / points;
            star.getPoints().addAll(
                r * Math.cos(angle),
                r * Math.sin(angle)
            );
        }
        
        return star;
    }

    private void handleCardClick(int index) {
        if (!canClick) return;
        
        MemoryCard card = cards.get(index);
        if (card.isMatched() || card.isRevealed()) return;

        card.reveal();
        count++;
        
        Platform.runLater(()->{
            countLabel.setText("Moves: " + count);
        });

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
    private Rectangle background;
    private boolean matched = false;
    private boolean revealed = false;

    public MemoryCard(Shape shape) {
        this.cardShape = shape;
        this.cardShape.setVisible(false);
        
        // Add white background rectangle for revealed state
        background = new Rectangle(80, 80, Color.WHITE);
        background.setArcHeight(20);
        background.setArcWidth(20);
        background.setStroke(Color.LIGHTGRAY);
        background.setStrokeWidth(2);
        background.setVisible(false);
        
        // Center the shape in the card
        StackPane shapeContainer = new StackPane(background, cardShape);
        shapeContainer.setAlignment(Pos.CENTER);
        
        // Add drop shadow effect
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(3.0);
        dropShadow.setOffsetY(3.0);
        dropShadow.setColor(Color.color(0, 0, 0, 0.3));
        cardShape.setEffect(dropShadow);
        
        cover = new Button("?");
        cover.setPrefSize(80, 80); // Increased card size
        cover.setStyle(
            "-fx-background-color: #3498db;" +
            "-fx-background-radius: 10;" +
            "-fx-border-radius: 10;" +
            "-fx-border-color: #2980b9;" +
            "-fx-border-width: 2;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 24px;" +
            "-fx-font-weight: bold;"
        );
        cover.setEffect(dropShadow);
        cover.setFocusTraversable(true);

        setAlignment(Pos.CENTER);
        getChildren().addAll(shapeContainer, cover);
    }

    public Button getButton() {
        return cover;
    }

    public void reveal() {
        cover.setVisible(false);
        background.setVisible(true);
        cardShape.setVisible(true);
        revealed = true;
    }

    public void hide() {
        if (!matched) {
            cover.setVisible(true);
            background.setVisible(false);
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