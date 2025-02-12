package home;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import database.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class LeaderboardController {
    @FXML
    private HBox backbutton;

    @FXML
    private StackPane leaderboard;

    @FXML
    private TableView<Player> leaderboardTable;

    @FXML
    private TableColumn<Player, Integer> rankColumn;

    @FXML
    private TableColumn<Player, String> usernameColumn;

    @FXML
    private TableColumn<Player, Integer> trophiesColumn;

    @FXML
    private TableColumn<Player, Integer> goldsColumn;

    private ObservableList<Player> leaderboardData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        rankColumn.setCellValueFactory(new PropertyValueFactory<>("rank"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        trophiesColumn.setCellValueFactory(new PropertyValueFactory<>("trophies"));
        goldsColumn.setCellValueFactory(new PropertyValueFactory<>("golds"));

        loadLeaderboard();
    }

    @FXML
    private void gotoHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../home/home.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backbutton.getScene().getWindow();

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

    private void loadLeaderboard() {
        DatabaseConnection databaseConnection = new DatabaseConnection();
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            connection = databaseConnection.getConnection();
            statement = connection.createStatement();

            String query = "SELECT username, trophies, golds FROM users ORDER BY trophies DESC";
            resultSet = statement.executeQuery(query);

            int rank = 1;
            while (resultSet.next()) {
                String username = resultSet.getString("username");
                int trophies = resultSet.getInt("trophies");
                int golds = resultSet.getInt("golds");

                leaderboardData.add(new Player(rank, username, trophies, golds));
                rank++;
            }

            leaderboardTable.setItems(leaderboardData);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null)
                    resultSet.close();
                if (statement != null)
                    statement.close();
                if (connection != null)
                    connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class Player {
        private int rank;
        private String username;
        private int trophies;
        private int golds;

        public Player(int rank, String username, int trophies, int golds) {
            this.rank = rank;
            this.username = username;
            this.trophies = trophies;
            this.golds = golds;
        }

        public int getRank() {
            return rank;
        }

        public String getUsername() {
            return username;
        }

        public int getTrophies() {
            return trophies;
        }

        public int getGolds() {
            return golds;
        }
    }
}
