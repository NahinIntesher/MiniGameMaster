package hellofx;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class CalculatorController {
    @FXML
    private TextField display;

    private double firstOperand = 0;
    private String operator = "";
    private boolean startNewNumber = true;

    @FXML
    private void handleNumberClick(javafx.event.ActionEvent event) {
        String value = ((Button) event.getSource()).getText();
        if (startNewNumber) {
            display.setText(value);
            startNewNumber = false;
        } else {
            display.setText(display.getText() + value);
        }
    }

    @FXML
    private void handleOperatorClick(javafx.event.ActionEvent event) {
        String value = ((Button) event.getSource()).getText();
        if (!"=".equals(value)) {
            operator = value;
            firstOperand = Double.parseDouble(display.getText());
            startNewNumber = true;
        } else {
            double secondOperand = Double.parseDouble(display.getText());
            double result = calculate(firstOperand, secondOperand, operator);
            display.setText(String.valueOf(result));
            startNewNumber = true;
        }
    }

    @FXML
    private void handleClear() {
        display.setText("");
        firstOperand = 0;
        operator = "";
        startNewNumber = true;
    }

    private double calculate(double a, double b, String op) {
        return switch (op) {
            case "+" -> a + b;
            case "-" -> a - b;
            case "*" -> a * b;
            case "/" -> b != 0 ? a / b : 0; // Prevent division by zero
            default -> 0;
        };
    }
}
