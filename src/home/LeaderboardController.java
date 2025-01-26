package home;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import database.DatabaseConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class LeaderboardController {

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

    private void loadLeaderboard() {
        DatabaseConnection databaseConnection = new DatabaseConnection();
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            connection = databaseConnection.getConnection();
            statement = connection.createStatement();

            String query = "SELECT username, trophies, golds FROM users ORDER BY trophies DESC LIMIT 10";
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
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (connection != null) connection.close();
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

        public int getgolds() {
            return golds;
        }
    }
}
