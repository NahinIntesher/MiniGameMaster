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

    public static int randomI;

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
                int[] defaultBallX = { 6, 11, 3, 17, 10 };
                int[] defaultBallY = { 13, 13, 13, 2, 5 };

                randomI = random.nextInt(5);

                gameInitializeInfo.put("levelData", levelData[randomI]);
                gameInitializeInfo.put("defaultBallX", defaultBallX[randomI]);
                gameInitializeInfo.put("defaultBallY", defaultBallY[randomI]);
                return gameInitializeInfo;

            case "Tetris":
                int[] randomShape = new int[100];

                for (int i = 0; i < 100; i++) {
                    randomShape[i] = random.nextInt(7);
                }
                gameInitializeInfo.put("randomShape", randomShape);
                return gameInitializeInfo;

            case "Snake":
                int[] randomFoodX = new int[100];
                int[] randomFoodY = new int[100];

                for (int i = 0; i < 100; i++) {
                    randomFoodX[i] = random.nextInt(20);
                    randomFoodY[i] = random.nextInt(16);
                }
                gameInitializeInfo.put("randomFoodX", randomFoodX);
                gameInitializeInfo.put("randomFoodY", randomFoodY);

                return gameInitializeInfo;

            case "BrickBreaker":
                String[][] mapsData = {
                        {
                                "02000020",
                                "00011000",
                                "00033000",
                                "00000000",
                                "01100110",
                                "03300330",
                                "00011000",
                                "00033000"
                        },
                        {
                                "00011000",
                                "00033000",
                                "02000020",
                                "00011000",
                                "00033000",
                                "01100110",
                                "03300330",
                                "00000000"
                        },
                        {
                                "00000000",
                                "01100110",
                                "00011000",
                                "03300330",
                                "00000000",
                                "02011020",
                                "00300300",
                                "00000000"
                        },
                        {
                                "01000010",
                                "20011002",
                                "03300330",
                                "00000000",
                                "00011000",
                                "00000000",
                                "02033020",
                                "00000000"
                        }
                };
                randomI = random.nextInt(4);
                gameInitializeInfo.put("mapData", mapsData[randomI]);
                
                return gameInitializeInfo;

            case "RapidRoll":
                int[] randomPlatformsX = new int[500];

                for (int i = 0; i < 500; i++) {
                    randomPlatformsX[i] = random.nextInt(290);
                }
                gameInitializeInfo.put("randomPlatformsX", randomPlatformsX);

                return gameInitializeInfo;

            default:
                return gameInitializeInfo;
        }
    }

    public static String[] selectRandomGames() {
        String[] inputArray = {
                "Snake",
                "Tetris",
                "MiniGolf",
                "BrickBreaker"
        };

        List<String> stringList = new ArrayList<>();
        Collections.addAll(stringList, inputArray);
        Collections.shuffle(stringList);

        return stringList.subList(0, 3).toArray(new String[0]);
        // String[] pseudoReturn = {"RapidRoll", "RapidRoll", "RapidRoll"};
        // return pseudoReturn;
    }

    public static LiveGame getGameInstance(String gameName, JSONObject gameInitializeInfo, String roomId,
            String playerToken, boolean self) {
        switch (gameName) {
            case "Snake":
                return new Snake(gameInitializeInfo, roomId, playerToken, self);
            case "MiniGolf":
                return new MiniGolf(gameInitializeInfo, roomId, playerToken, self);
            case "Tetris":
                return new Tetris(gameInitializeInfo, roomId, playerToken, self);
            case "BrickBreaker":
                return new BrickBreaker(gameInitializeInfo, roomId, playerToken, self);
            case "RapidRoll":
                return new RapidRoll(gameInitializeInfo, roomId, playerToken, self);
            default:
                return new Snake(gameInitializeInfo, roomId, playerToken, self);
        }
    }

    public static String getGameTitle(String gameName) {
        switch (gameName) {
            case "Snake":
                return "Snake";
            case "MiniGolf":
                return "Mini Golf";
            case "Tetris":
                return "Tetris";
            case "BrickBreaker":
                return "Brick Breaker";
            default:
                return "";
        }
    }
}