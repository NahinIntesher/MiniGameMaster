package adventure.games;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

import adventure.AdventureGameController;
import database.DatabaseConnection;
import highscore.HighScoreGameController;
import javafx.scene.layout.StackPane;

public class AdventureMiniGame extends StackPane {
    public String serverAddress = DatabaseConnection.serverAddress;

    public void actionOnKeyPressed(String input) {
    }

    public void actionOnKeyReleased(String input) {
    }

    public void restartGame() {

    }

    public static int randomI;

    public static JSONObject getGameInitializeInfo(int gameId) {
        JSONObject gameInitializeInfo = new JSONObject();
        Random random = new Random();

        switch (gameId) {
            case 3:
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

            case 1:
                int[] randomShape = new int[100];

                for (int i = 0; i < 100; i++) {
                    randomShape[i] = random.nextInt(7);
                }
                gameInitializeInfo.put("randomShape", randomShape);
                return gameInitializeInfo;

            case 2:
                int[] randomFoodX = new int[100];
                int[] randomFoodY = new int[100];

                for (int i = 0; i < 100; i++) {
                    randomFoodX[i] = random.nextInt(20);
                    randomFoodY[i] = random.nextInt(16);
                }
                gameInitializeInfo.put("randomFoodX", randomFoodX);
                gameInitializeInfo.put("randomFoodY", randomFoodY);

                return gameInitializeInfo;

            case 4:
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

            case 5:
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

    public static AdventureMiniGame getGameInstance(int gameId, int target, AdventureGameController adventureGameController) {
        switch (gameId) {
            case 1:
                return new Tetris(adventureGameController, target);
            case 2:
                return new MiniGolf(adventureGameController, target);
            case 3:
                return new Snake(adventureGameController, target);
            case 4:
                return new RapidRoll(adventureGameController, target);
            // case 5:
            //     return new MemoryGame(highScoreGameController);
            // case 6:
            //     return new BrickBreaker(highScoreGameController);
            case 7:
                return new BubbleShooter(adventureGameController, target);
            default:
                return new Snake(adventureGameController, target);
        }
    }

    public static String getGameTitle(int gameId) {
        switch (gameId) {
            case 1:
                return "Tetris";
            case 2:
                return "Mini Golf";
            case 3:
                return "Snake";
            case 4:
                return "Rapid Roll";
            case 5:
                return "Memory Game";
            case 6:
                return "Brick Breaker";
            case 7:
                return "Bubble Shooter";
            default:
                return "";
        }
    }
}