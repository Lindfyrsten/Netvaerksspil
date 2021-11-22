package mazeGame;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ServerThread extends Thread {
    
    Socket connSocket;
    BufferedReader inFromClient;
    DataOutputStream outToClient;
    String playerName;
    
    public ServerThread(Socket connSocket) throws Exception {
        
        this.connSocket = connSocket;
        inFromClient = new BufferedReader(new InputStreamReader(connSocket.getInputStream()));
        outToClient = new DataOutputStream(connSocket.getOutputStream());
        // add this thread to the server thread list
        Server.threads.add(this);
    }
    
    @Override
    public void run() {
        
        try {
            // setup the clients board at start
            setupBoard();
            while (true) {
                String in = inFromClient.readLine();
                // client wants to create a new player
                if (in.charAt(0) == 'n') {
                    String[] arr = in.split("#");
                    String name = arr[1];
                    playerName = name;
                    // create the player
                    Server.createPlayer(name);
                    Server.clientsSpawnPlayers();
                }
                // client wants to move
                else if (in.charAt(0) == 'm') {
                    String[] arr = in.split("#");
                    Server.movePlayer(Integer.parseInt(arr[1]), Integer.parseInt(arr[2]),
                        Direction.valueOf(arr[3]),
                        playerName);
                }
                // client wants to shoot
                else if (in.charAt(0) == 's') {
                    String[] arr = in.split("#");
                    String name = arr[1];
                    Server.playerShoot(name);
                }
                else if (in.charAt(0) == 'b') {
                    String[] arr = in.split("#");
                    String name = arr[1];
                    Server.dropBomb(name);
                }
                // client wants to quit
                else if (in.charAt(0) == 'q') {
                    Server.removePlayer(playerName);
                    Server.threads.remove(this);
                    Server.clientsUpdateScore();
                    break;
                }
            }
            inFromClient.close();
            outToClient.close();
            connSocket.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Tell client that a player has moved
     *
     * @param fromX The x position player moved from
     * @param fromY The y position player moved from
     * @param toX The x position player moved to
     * @param toY The y position player moved to
     * @param direction The direction of the move
     * @param name The name of the player that moved
     */
    public void movePlayer(int fromX, int toX, int fromY, int toY, Direction direction, String name)
        throws Exception {
        
        String str = "m#" + fromX + "#" + fromY + "#" + toX + "#" + toY + "#" + direction.toString()
            + "#" + name;
        outToClient.writeBytes(str + '\n');
    }
    
    /**
     * Tell client to spawn a player on the map
     *
     * @param x The x coordinate of the player to be spawned
     * @param y The y coordinate of the player to be spawned
     * @param playerName The name of the player that spawned
     */
    public void spawnPlayer(int x, int y, String name, int ammo, int players)
        throws Exception {
        
        String str = "c#" + x + "#" + y + "#" + name + "#" + ammo + "#" + players;
        outToClient.writeBytes(str + '\n');
    }
    
    public void spawnAmmo(int x, int y) throws IOException {
        
        String str = "a#" + x + "#" + y;
        outToClient.writeBytes(str + '\n');
    }
    
    public void dropBomb(int centerX, int centerY, int up, int down, int left, int right,
        String name)
        throws IOException {
        
        String str = "b#" + centerX + "#" + centerY + "#" + up + "#" + down + "#" + left + "#"
            + right + "#" + name;
        outToClient.writeBytes(str + '\n');
    }
    
    /**
     * Tell client to remove a player from the game
     *
     * @param x The x coordinate of the player to be removed
     * @param y The y coordinate of the player to be removed
     */
    public void removePlayer(int x, int y, String name) throws Exception {
        
        outToClient.writeBytes("r#" + x + "#" + y + "#" + name + '\n');
    }
    
    /**
     * Tell client to create walls on his board
     */
    public void setupBoard() throws Exception {
        
        for (int j = 0; j < 20; j++) {
            for (int i = 0; i < 20; i++) {
                if (Server.board[j].charAt(i) == 'w') {
                    outToClient.writeBytes("w#" + i + "#" + j + '\n');
                }
            }
        }
    }
    
    /**
     * Tell client a player has shot
     *
     * @param fromX The first x coordinate in the path
     * @param fromY The first y coordinate in the path
     * @param toX The last x coordinate in the path
     * @param toY The last y coordinate in the path
     * @param direction Direction of the shot
     * @param name The name of the shooter
     */
    public void playerShoot(int fromX, int fromY, int toX, int toY, Direction direction, int ammo,
        String name)
        throws Exception {
        
        String str = "s#" + fromX + "#" + fromY + "#" + toX + "#" + toY + "#" + direction.toString()
            + "#" + ammo + "#"
            + name;
        outToClient.writeBytes(str + '\n');
    }
    
    public void playerDead(int x, int y, String name) throws IOException {
        
        String str = "d#" + x + "#" + y + "#" + name;
        outToClient.writeBytes(str + '\n');
    }
    
    /**
     * Updates scoreArea to fit the current score of all players
     *
     * @param scores An array of strings containing playernames and their points
     */
    public void updateScores(String scores) throws IOException {
        
        String str = "u#" + scores;
        outToClient.writeBytes(str + '\n');
    }
    
    /**
     * Update ammo
     *
     * @param ammo
     */
    public void updateAmmo(int ammo) throws IOException {
        
        outToClient.writeBytes("q#" + ammo + '\n');
    }
    
    public void roundFinished(String name, int kills) throws IOException {
        
        String str = "f#" + name + "#" + kills;
        outToClient.writeBytes(str + '\n');
    }
    
    public void roundStart() throws IOException {
        
        outToClient.writeBytes("n" + '\n');
    }
}
