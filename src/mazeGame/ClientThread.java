package mazeGame;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import javafx.application.Platform;

public class ClientThread extends Thread {

    private Socket clientSocket;
    private BufferedReader inFromServer;
    private DataOutputStream outToServer;
    private int shotX, shotY;

    public ClientThread(Socket clientSocket) throws Exception {

        this.clientSocket = clientSocket;
        inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        outToServer = new DataOutputStream(clientSocket.getOutputStream());
    }

    @Override
    public void run() {

        boolean named = false;
        // wait until we filled in a name
        while (!named) {
            Thread.yield();
            if (Client.name.length() > 0) {
                named = true;
            }
        }
        // tell server we want to create a new player with this name
        try {
            outToServer.writeBytes("n#" + Client.name + '\n');
        }
        catch (Exception e1) {
            e1.printStackTrace();
        }
        String str;
        while (true) {
            try {
                str = inFromServer.readLine();
                // System.out.println(str);
                // check the first letter of the string, depending on what the command of the server is
                //
                // spawn a player
                if (str.charAt(0) == 'c') {
                    // split the recieved string
                    String[] split = str.split("#");
                    int x = Integer.parseInt(split[1]);
                    int y = Integer.parseInt(split[2]);
                    String name = split[3];
                    int ammo = Integer.parseInt(split[4]);
                    int players = Integer.parseInt(split[5]);
                    // tell java fx we want to run a method whenever ready
                    Platform.runLater(() -> {
                        Client.playerSpawn(x, y, name, ammo, players);
                    });
                }
                // move a player
                else if (str.charAt(0) == 'm') {
                    String[] split = str.split("#");
                    int fromX = Integer.parseInt(split[1]);
                    int fromY = Integer.parseInt(split[2]);
                    int toX = Integer.parseInt(split[3]);
                    int toY = Integer.parseInt(split[4]);
                    Direction direction = Direction.valueOf(split[5]);
                    String name = split[6];
                    Platform.runLater(() -> {
                        Client.playerMove(fromX, fromY, toX, toY, direction, name);
                    });
                }
                // create a wall
                else if (str.charAt(0) == 'w') {
                    String[] split = str.split("#");
                    int x = Integer.parseInt(split[1]);
                    int y = Integer.parseInt(split[2]);
                    Platform.runLater(() -> {
                        try {
                            Client.createWall(x, y);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
                // a player shoots
                else if (str.charAt(0) == 's') {
                    String[] split = str.split("#");
                    int fromX = Integer.parseInt(split[1]);
                    int fromY = Integer.parseInt(split[2]);
                    int toX = Integer.parseInt(split[3]);
                    int toY = Integer.parseInt(split[4]);
                    Direction direction = Direction.valueOf(split[5]);
                    int ammo = Integer.parseInt(split[6]);
                    String name = split[7];
                    shotX = -1;
                    shotY = -1;
                    // tries to parse the numbers, if a player was shot
                    try {
                        shotX = Integer.parseInt(split[8]);
                        shotY = Integer.parseInt(split[9]);
                    }
                    catch (Exception e2) {
                        // do nothing
                    }
                    Platform.runLater(() -> {
                        try {
                            Client.playerShoot(fromX, fromY, toX, toY, direction, name, ammo, shotX,
                                shotY);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
                // update score
                else if (str.charAt(0) == 'u') {
                    String[] split = str.split("#");
                    Platform.runLater(() -> {
                        try {
                            Client.updateScore(split);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
                // a player gets removed
                else if (str.charAt(0) == 'r') {
                    String[] split = str.split("#");
                    int x = Integer.parseInt(split[1]);
                    int y = Integer.parseInt(split[2]);
                    String name = split[3];
                    // if we are the one getting removed, break the while loop and stop listening
                    if (name.equals(Client.name)) {
                        break;
                    }
                    else {
                        Platform.runLater(() -> {
                            try {
                                Client.removePlayer(x, y);
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }
                }
                else if (str.charAt(0) == 'a') {
                    String[] split = str.split("#");
                    int x = Integer.parseInt(split[1]);
                    int y = Integer.parseInt(split[2]);
                    Platform.runLater(() -> {
                        try {
                            Client.spawnAmmo(x, y);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
                else if (str.charAt(0) == 'b') {
                    String[] split = str.split("#");
                    int centerX = Integer.parseInt(split[1]);
                    int centerY = Integer.parseInt(split[2]);
                    int up = Integer.parseInt(split[3]);
                    int down = Integer.parseInt(split[4]);
                    int left = Integer.parseInt(split[5]);
                    int right = Integer.parseInt(split[6]);
                    String name = split[7];
                    Platform.runLater(() -> {
                        try {
                            Client.playerBomb(centerX, centerY, up, down, left, right, name);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
                else if (str.charAt(0) == 'q') {
                    String[] split = str.split("#");
                    int ammo = Integer.parseInt(split[1]);
                    Platform.runLater(() -> {
                        try {
                            Client.updateAmmo(ammo);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
                else if (str.charAt(0) == 'd') {
                    String[] split = str.split("#");
                    int x = Integer.parseInt(split[1]);
                    int y = Integer.parseInt(split[2]);
                    String name = split[3];
                    Platform.runLater(() -> {
                        try {
                            Client.playerDead(x, y, name);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
                else if (str.charAt(0) == 'f') {
                    String[] split = str.split("#");
                    String name = split[1];
                    int kills = Integer.parseInt(split[2]);
                    Platform.runLater(() -> {
                        try {
                            Client.roundFinished(name, kills);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
                else if (str.charAt(0) == 'n') {
                    Platform.runLater(() -> {
                        try {
                            Client.roundStart();
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            outToServer.close();
            inFromServer.close();
            clientSocket.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    // tell the server we want to move
    public void movePlayer(int delta_x, int delta_y, Direction direction) throws Exception {

        String str = "m#" + delta_x + "#" + delta_y + "#" + direction.toString();
        outToServer.writeBytes(str + '\n');
    }

    // tell the server we want to shoot
    public void shoot(String name) throws Exception {

        outToServer.writeBytes("s#" + name + '\n');
    }

    public void dropBomb(String name) throws IOException {

        outToServer.writeBytes("b#" + name + '\n');
    }

    // tell the server we are quitting
    public void quit() {

        try {
            outToServer.writeBytes("q" + '\n');
        }
        catch (IOException e) {
            // ignore
        }
    }
}
