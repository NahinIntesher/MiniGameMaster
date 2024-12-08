package livegame;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class SocketController {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    @FXML
    private TextField textbox;

    @FXML
    private Button sendButton;

    @FXML
    private VBox messageCont;

    @FXML
    private void sendMessage(ActionEvent e) {
        String message = textbox.getText();

        out.println(message);
    }

    @FXML
    public void initialize() {
        try {
            socket = new Socket("localhost", 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            new Thread(()->{         
                try{
                    String message;
                    while ((message = in.readLine()) != null) {
                        System.out.println(message);
                    }
                }   
                catch(Exception e) {
                    e.printStackTrace();
                }
            }).start();

            

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
