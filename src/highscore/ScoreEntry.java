package highscore;

public class ScoreEntry {
  private String name;
  private int points;
  private int allTimeScore;
  private int rank;
  
  public ScoreEntry(String name, int points, int allTimeScore, int rank) {
      this.name = name;
      this.points = points;
      this.allTimeScore = allTimeScore;
      this.rank = rank;
  }
  
  // Getters
  public String getName() { return name; }
  public int getPoints() { return points; }
  public int getAllTimeScore() { return allTimeScore; }
  public int getRank() { return rank; }
}

