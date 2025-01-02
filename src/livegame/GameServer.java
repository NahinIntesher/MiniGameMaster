package livegame;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import org.json.JSONObject;

public class GameServer {
    private static final int PORT = 12345;
    private static final ConcurrentHashMap<String, List<Socket>> rooms = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Socket, String> playerRooms = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New player connected: " + socket.getInetAddress());
                new Thread(new PlayerHandler(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class PlayerHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        public PlayerHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Wait for first message to determine room joining strategy
                String firstMessage = in.readLine();
                if (firstMessage == null) {
                    throw new IOException("No initial message received");
                }

                String roomId;
                if (firstMessage.equals("findRoom")) {
                    // Original logic: find or create a room with one player
                    roomId = findOrCreateRoom();
                    System.out.println("Player added to room: " + roomId);
                } else if (firstMessage.startsWith("joinRoom:")) {
                    // New logic: join a specific room
                    roomId = firstMessage.substring("joinRoom:".length());
                    joinSpecificRoom(roomId);
                } else {
                    out.println("Invalid initial command. Use 'findRoom' or 'joinRoom:roomId'");
                    return;
                }

                // Send room ID to the client
                out.println("setRoomId:" + roomId);

                // Check if game can start
                List<Socket> roomPlayers = rooms.get(roomId);
                synchronized (roomPlayers) {
                    if (roomPlayers.size() == 1) {
                        out.println("Waiting for another player...");
                    }

                    if (roomPlayers.size() == 2) {
                        startGameForRoom(roomId);
                    }
                }

                // Handle player communication (game updates)
                String message;
                while ((message = in.readLine()) != null) {
                    broadcastToRoom(roomId, message);
                    if(message.split(":")[0].equals("playerLeft")) {
                        killRoom();
                    }
                }
            } catch (IOException e) {
                //e.printStackTrace();
                System.out.println("Player left: "+socket.getInetAddress());
            } finally {
                // Cleanup when player disconnects
                cleanupPlayer();
            }
        }

        private synchronized String findOrCreateRoom() {
            // Try to find a room with one player
            for (Map.Entry<String, List<Socket>> entry : rooms.entrySet()) {
                if (entry.getValue().size() == 1) {
                    String roomId = entry.getKey();
                    entry.getValue().add(socket);
                    playerRooms.put(socket, roomId);
                    return roomId;
                }
            }

            // Create a new room if no available rooms
            String roomId = UUID.randomUUID().toString();
            List<Socket> newRoomPlayers = new CopyOnWriteArrayList<>();
            newRoomPlayers.add(socket);
            rooms.put(roomId, newRoomPlayers);
            playerRooms.put(socket, roomId);
            return roomId;
        }

        private synchronized void joinSpecificRoom(String roomId) {
            List<Socket> roomPlayers = rooms.get(roomId);
            
            // Check if room exists and is not full
            if (roomPlayers == null) {
                roomPlayers = new CopyOnWriteArrayList<>();
                rooms.put(roomId, roomPlayers);
            }

            synchronized (roomPlayers) {
                // if (roomPlayers.size() >= 2) {
                //     out.println("Room is full. Cannot join.");
                //     return;
                // }

                roomPlayers.add(socket);
                playerRooms.put(socket, roomId);
                out.println("Joined room: " + roomId);
            }
        }

        private void broadcastToRoom(String roomId, String message) {
            List<Socket> roomPlayers = rooms.get(roomId);

            if (roomPlayers != null) {
                for (Socket player : roomPlayers) {
                    try {
                        new PrintWriter(player.getOutputStream(), true).println(message);
                        //System.out.println(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private void startGameForRoom(String roomId) {
            System.out.println("startGame: " + roomId);
            String games[] = LiveGame.selectRandomGames();

            JSONObject game1InitInfo = LiveGame.getGameInitializeInfo(games[0]);
            JSONObject game2InitInfo = LiveGame.getGameInitializeInfo(games[1]);
            JSONObject game3InitInfo = LiveGame.getGameInitializeInfo(games[2]);
            
            JSONObject gamesInitInfo[] = {game1InitInfo, game2InitInfo, game3InitInfo};

            JSONObject initializationInfo = new JSONObject();
            initializationInfo.put("type", "gameInitialize");
            initializationInfo.put("games", games);
            initializationInfo.put("gamesInitInfo", gamesInitInfo);

            
            List<Socket> roomPlayers = rooms.get(roomId);
            if (roomPlayers != null) {
                for (Socket player : roomPlayers) {
                    try {
                        new PrintWriter(player.getOutputStream(), true).println(initializationInfo);
                        new PrintWriter(player.getOutputStream(), true).println("startGame:" + roomId);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private void cleanupPlayer() {
            String roomId = playerRooms.get(socket);
            if (roomId != null) {
                List<Socket> roomPlayers = rooms.get(roomId);
                if (roomPlayers != null) {
                    synchronized (roomPlayers) {
                        roomPlayers.remove(socket);
                        if (roomPlayers.isEmpty()) {
                            rooms.remove(roomId);
                            System.out.println("Room " + roomId + " is now empty and removed.");
                        }
                    }
                }
                playerRooms.remove(socket);
            }
            
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void killRoom() {
            String roomId = playerRooms.get(socket);
            if (roomId != null) {
                List<Socket> roomPlayers = rooms.get(roomId);
                
                if (roomPlayers != null) {
                    for (Socket player : roomPlayers) {
                        try {
                            roomPlayers.remove(player);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                playerRooms.remove(socket);
                rooms.remove(roomId);
                System.out.println("Room " + roomId + " is removed due to player left.");
            }
            
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}