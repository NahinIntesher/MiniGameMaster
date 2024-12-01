package hellofx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SimpleCalculator extends Application {

    private String input = "";
    private Label display = new Label("0");

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Simple Calculator");

        VBox root = new VBox();
        root.setId("root");

        display.setId("display");

        GridPane grid = new GridPane();
        grid.setId("grid");

        String[][] buttons = {
            {"7", "8", "9", "+"},
            {"4", "5", "6", "-"},
            {"1", "2", "3", "*"},
            {"C", "0", "=", "/"}
        };

        for (int row = 0; row < buttons.length; row++) {
            for (int col = 0; col < buttons[row].length; col++) {
                String label = buttons[row][col];
                Button button = new Button(label);
                button.setId(label.matches("[+\\-*/=C]") ? "operator-button" : "button");
                button.setOnAction(_ -> handleInput(label));
                grid.add(button, col, row);
            }
        }

        root.getChildren().addAll(display, grid);

        Scene scene = new Scene(root, 400, 500);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleInput(String value) {
        switch (value) {
            case "C":
                input = "";
                display.setText("0");
                break;
            case "=":
                try {
                    double result = evaluate(input);
                    display.setText(String.valueOf(result));
                    input = String.valueOf(result);
                } catch (Exception e) {
                    display.setText("Error");
                    input = "";
                }
                break;
            default:
                if (isOperator(value) && (input.isEmpty() || isOperator(input.substring(input.length() - 1)))) {
                    // Prevent consecutive operators
                    return;
                }
                input += value;
                display.setText(input);
        }
    }

    private boolean isOperator(String value) {
        return "+-*/".contains(value);
    }

    private double evaluate(String expression) {
        // Simplified evaluation logic using Java's ScriptEngine
        try {
            return (double) new javax.script.ScriptEngineManager()
                .getEngineByName("JavaScript")
                .eval(expression);
        } catch (Exception e) {
            throw new RuntimeException("Invalid expression");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
