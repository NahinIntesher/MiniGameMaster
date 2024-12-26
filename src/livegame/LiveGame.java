package livegame;

import database.DatabaseConnection;
import javafx.scene.layout.StackPane;

class LiveGame extends StackPane {
    public String serverAddress = DatabaseConnection.serverAddress;

    public void actionOnKeyPressed(String input) {
    }

    public void actionOnKeyReleased(String input) {
    }
}