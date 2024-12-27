package livegame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

import database.DatabaseConnection;
import javafx.scene.layout.StackPane;

class LiveGame extends StackPane {
    public String serverAddress = DatabaseConnection.serverAddress;

    public void actionOnKeyPressed(String input) {
    }

    public void actionOnKeyReleased(String input) {
    }

    public static JSONObject getGameInitializeInfo(String gameName) {
        JSONObject gameInitializeInfo = new JSONObject();
        Random random = new Random();

        switch (gameName) {
            case "MiniGolf":
                String[][] levelData = {
                    {
                        "####################",
                        "#..................#",
                        "#..&&&&....WWWW....#",
                        "#.........WWWW.O...#",
                        "#...........&&&&...#",
                        "#.....SSSS.......###",
                        "#.....SSSS.......#",
                        "#.....SSSS.......###",
                        "#...&&&&......WWW..#",
                        "#.............WWW..#",
                        "####...SSS....WWW..#",
                        "   #...SSS....WWW..#",
                        "####...............#",
                        "#..................#",
                        "#..................#",
                        "####################"
                    },
                    {
                        "#################   ",
                        "#...............#   ",
                        "#....&&WWW&&....####",
                        "#....&&WWWW&.......#",
                        "#.........&&&...O..#",
                        "#.....WW...........#",
                        "#.....WW.....SSSS..#",
                        "#.....WW.....SSSS..#",
                        "#.&&&......WWWW..###",
                        "#..........WWWW..#  ",
                        "#................###",
                        "#..S&&&&.......S...#",
                        "#..S&&&&.......S...#",
                        "#..................#",
                        "#..................#",
                        "####################"
                    },
                    {
                        "#############   ####",
                        "#...&&&&....#####..#",
                        "#..WWWW&&&.........#",
                        "#........&&&.......#",
                        "#........&&&&...O..#",
                        "#...SSS............#",
                        "#...SSS...WWWW.....#",
                        "###......WWWW......#",
                        "  ##.....&&&&......#",
                        "   #..&&&&.........#",
                        "####....&&&&....&&&#",
                        "#....WW........WW..#",
                        "#....WWSSSS....WW..#",
                        "#......SSSS........#",
                        "#..................#",
                        "####################"
                    },
                    {
                        "######  ############",
                        "#....####..........#",
                        "#....WWWW....&&....#",
                        "#....WWWW....&&&...#",
                        "#....WWWW..........#",
                        "#..........&&&&....#",
                        "#......SSS&&&&.....#",
                        "#......SSS.........#",
                        "#......SSS...&&....#",
                        "#............&&WWW##",
                        "#........WWWW&&WWW# ",
                        "#..SS....WWWW.....##",
                        "#..SSSSS.WWWW...O..#",
                        "#.....SS...........#",
                        "#...........####...#",
                        "#############  #####"
                    },
                    {
                        "####################",
                        "#......&&&&&&......#",
                        "#.....WWWWWWWW.....#",
                        "#.....WWWWWWWW.....#",
                        "#...&&&&....&&&&...#",
                        "#...&&........&&...#",
                        "#......SSSSSS......#",
                        "#......SSSSSS......#",
                        "#....&&&&&&&&&&....#",
                        "#.....&&&&&&&&.....#",
                        "###..............###",
                        "  #....WWWWWW....#  ",
                        "###....WWWWWW....###",
                        "#..................#",
                        "#.........O........#",
                        "####################"
                    }
                };
                int[] defaultBallX = {6,11,3,17,10};
                int[] defaultBallY = {13,13,13,2,5};

                int randomI = random.nextInt(5);
                
                gameInitializeInfo.put("levelData", levelData[randomI]);
                gameInitializeInfo.put("defaultBallX", defaultBallX[randomI]);
                gameInitializeInfo.put("defaultBallY", defaultBallY[randomI]); 
                return gameInitializeInfo;    
            default:
                return new JSONObject();
        }
    }

    public static String[] selectRandomGames() {
        String[] inputArray = {
            "Snake",
            "Tetris",
            "MiniGolf"
        };

        List<String> stringList = new ArrayList<>();
        Collections.addAll(stringList, inputArray);
        Collections.shuffle(stringList);

        return stringList.subList(0, 3).toArray(new String[0]);
    }

    public static LiveGame getGameInstance(String gameName, JSONObject gameInitializeInfo, String roomId, String playerToken, boolean self) {
        switch (gameName) {
            case "Snake":
                return new Snake(gameInitializeInfo, roomId, playerToken, self);  
            case "MiniGolf":
                return new MiniGolf(gameInitializeInfo, roomId, playerToken, self); 
            case "Tetris":
                return new Tetris(gameInitializeInfo, roomId, playerToken, self);        
            default:
                return new Snake(gameInitializeInfo, roomId, playerToken, self);
        }
    } 
}