package mazeGame;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import javafx.geometry.Point2D;

public class Server {

    public static ArrayList<ServerThread> threads;
    public static String[] board = { // 20x20
        "wwwwwwwwwwwwwwwwwwww", "w        ww        w", "w w  w  www w  w  ww",
        "w w  w   ww w  w  ww",
        "w  w               w", "w w w w w w w  w  ww", "w w     www w  w  ww",
        "w w     w w w  w  ww",
        "w   w w     w  w   w", "w     w  w  w  w   w", "w ww ww        w  ww",
        "w  w w    w    w  ww",
        "w        ww w  w  ww", "w         w w  w  ww", "w        w     w  ww",
        "w  w              ww",
        "w  w www  w w  ww ww", "w w      ww w     ww", "w   w   ww  w      w",
        "wwwwwwwwwwwwwwwwwwww" };
    public static Player playerShot;
    public static ArrayList<Player> players = new ArrayList<>();
    private static ArrayList<Point2D> ammoList = new ArrayList<>();
    private static ArrayList<Point2D> bombList = new ArrayList<>();
    private static Player winner;
    private static Timer ammoTimer;
    boolean roundStart = false;

    public static void main(String[] args) throws Exception {

        threads = new ArrayList<>();
        spawnAmmo();
        // accept clients
        System.out.println("Server running...");
        ServerSocket welcomeSocket = new ServerSocket(6789);
        while (true) {
            if (players.size() < 4) {
                Socket connectionSocket = welcomeSocket.accept();
                System.out.println(
                    "Connection received from " + connectionSocket.getInetAddress().getHostName());
                (new ServerThread(connectionSocket)).start();
            }
        }
    }

    // ----------------------------------------
    // Game methods
    // ----------------------------------------
    /**
     * Create a new player and add to player list
     *
     * @param name Name of the player
     */
    public static void createPlayer(String name) {

        Player p = new Player(name, 0, 0, Direction.UP);
        // spawnPlayer(p);
        players.add(p);
        if (players.size() == 2) {
            roundStart();
        }
    }

    /**
     * Remove a player from the player list
     *
     * @param name Name of the player
     */
    public static void removePlayer(String name) {

        // find the correct player
        Player p = findPlayer(name);
        // save the x,y coordinate before removal
        int x = p.getXpos();
        int y = p.getYpos();
        players.remove(p);
        // tell clients that a player was removed
        clientsRemovePlayer(x, y, name);
    }

    /**
     * Move a player
     *
     * @param delta_x The number on the players x value to move
     * @param delta_y The number on the players y value to move
     * @param direction The direction of the move
     * @param name The name of the player that moved
     */
    public static void movePlayer(int delta_x, int delta_y, Direction direction, String name)
        throws Exception {

        Player p = findPlayer(name);
        int fromX = p.getXpos();
        int fromY = p.getYpos();
        // check if move is legal
        if (playerMoved(delta_x, delta_y, direction, p)) {
            // tell clients to move player, if move is legal
            clientsMovePlayer(fromX, fromY, p.getXpos(), p.getYpos(), direction, name);
        }
        else {
            // if not, only update the direction player was trying to move - x,y remain the same
            clientsMovePlayer(fromX, fromY, fromX, fromY, direction, name);
        }
    }

    /**
     * Find a player on the player list
     *
     * @param name The name of the player
     */
    public static Player findPlayer(String name) {

        Player player = null;
        for (Player p : players) {
            if (p.getName().equals(name)) {
                player = p;
            }
        }
        return player;
    }

    /**
     * Check if coordinate contains a player
     *
     * @param x The x coordinate
     * @param y The y coordinate
     */
    private static Player getPlayerAt(int x, int y) {

        for (Player p : players) {
            if (p.getXpos() == x && p.getYpos() == y) {
                return p;
            }
        }
        return null;
    }

    private static boolean findBombAt(int x, int y) {

        boolean bomb = false;
        for (Point2D po : bombList) {
            if (x == po.getX() && y == po.getY()) {
                bomb = true;
            }
        }
        return bomb;
    }

    /**
     * Spawn a player at a random x,y location
     *
     * @param player Player to spawn
     */
    public static boolean spawnPlayer(Player player) {

        boolean spawned = false;
        int x = 0;
        int y = 0;
        while (!spawned) {
            // get a random x, y
            x = (int) (Math.random() * (board.length - 2) + 1);
            y = (int) (Math.random() * (board.length - 2) + 1);
            // if there's no wall && no players, set spawned to true and update the players x,y
            if (board[y].charAt(x) != 'w' && getPlayerAt(x, y) == null) {
                player.setXpos(x);
                player.setYpos(y);
                spawned = true;
            }
        }
        return spawned;
    }

    /**
     * Move a player
     *
     * @param delta_x The number on the players x value to move
     * @param delta_y The number on the players y value to move
     * @param direction The direction of the move
     * @param player The player that wants moved
     * @return True if move was succesful
     */
    public static boolean playerMoved(int delta_x, int delta_y, Direction direction,
        Player player) {

        boolean moved = false;
        // dont move while in the middle of shooting
        if (!player.isShooting()) {
            // update player direction
            player.setDirection(direction);
            int x = player.getXpos(), y = player.getYpos();
            // check if the coordinates player wants to move to is a wall
            if (board[y + delta_y].charAt(x + delta_x) != 'w') {
                // check if the coordinates player wants to move to is occupied by another player
                Player p = getPlayerAt(x + delta_x, y + delta_y);
                if (p == null && !findBombAt(x + delta_x, y + delta_y)) {
                    moved = true;
                    x += delta_x;
                    y += delta_y;
                    player.setXpos(x);
                    player.setYpos(y);
                    Point2D crate = null;
                    for (Point2D po : ammoList) {
                        if (po.getX() == x && po.getY() == y) {
                            player.setAmmo(player.getAmmo() + 1);
                            updateAmmo(player.getName(), player.getAmmo());
                            crate = po;
                        }
                    }
                    if (crate != null) {
                        ammoList.remove(crate);
                    }
                }
            }
        }
        return moved;
    }

    /**
     * Shoot with a player
     *
     * @param name Name of the player
     */
    public static void playerShoot(String name) {

        // find the player
        Player shooter = findPlayer(name);
        Player victim = null;
        // check if player has enough ammo
        if (shooter.getAmmo() > 0) {
            int delta_x, delta_y;
            int x = shooter.getXpos();
            int y = shooter.getYpos();
            Direction direction = shooter.getDirection();
            if (direction == Direction.UP) {
                delta_x = 0;
                delta_y = -1;
            }
            else if (direction == Direction.DOWN) {
                delta_x = 0;
                delta_y = 1;
            }
            else if (direction == Direction.LEFT) {
                delta_x = -1;
                delta_y = 0;
            }
            else {
                delta_x = 1;
                delta_y = 0;
            }
            // save the first coordinate of the shot's path for later
            int startX = x + delta_x;
            int startY = y + delta_y;
            playerShot = null;
            // keep shooting until hitting a wall
            while (board[y + delta_y].charAt(x + delta_x) != 'w') {
                x += delta_x;
                y += delta_y;
                // check if a player is shot in this coordinate
                Player p = getPlayerAt(x, y);
                if (p != null) {
                    p.setDead(true);
                    p.addDeath();
                    victim = p;
                }
                // check if any ammo was lost
                Point2D ammoLost = null;
                for (Point2D po : ammoList) {
                    if (po.getX() == x && po.getY() == y) {
                        ammoLost = po;
                    }
                }
                if (ammoLost != null) {
                    ammoList.remove(ammoLost);
                }
            }
            // if x and y has changed since we started shooting, it means we didnt hit a wall
            if (x != shooter.getXpos() || y != shooter.getYpos()) {
                shooter.setAmmo(shooter.getAmmo() - 1);
                clientsShot(startX, startY, x, y, direction, name, shooter.getAmmo());
                if (victim != null) {
                    shooter.addKill(1);
                    clientsPlayerDead(victim.getXpos(), victim.getYpos(), victim.getName());
                    clientsUpdateScore();
                    victim.setXpos(0);
                    victim.setYpos(0);
                    int alive = 0;
                    for (Player p : players) {
                        if (!p.isDead()) {
                            alive++;
                        }
                    }
                    if (alive == 1) {
                        winner = shooter;
                        try {
                            roundFinished(winner.getName(), winner.getKills());
                        }
                        catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public static void dropBomb(String name) {

        Player bomber = findPlayer(name);
        int x = bomber.getXpos();
        int y = bomber.getYpos();
        int up, down, left, right;
        Point2D point = new Point2D(x, y);
        bombList.add(point);
        ArrayList<Point2D> bombed = new ArrayList<>();
        bombed.add(new Point2D(x, y));
        if (bomber.getBombs() > 0) {
            int delta_x;
            int delta_y;
            int bombSize = bomber.getBombSize();
            int size;
            // up
            up = 0;
            int upX = x;
            int upY = y;
            size = 0;
            delta_x = 0;
            delta_y = -1;
            while (board[upY + delta_y].charAt(upX + delta_x) != 'w' && size < bombSize) {
                upX += delta_x;
                upY += delta_y;
                bombed.add(new Point2D(upX, upY));
                size++;
                up++;
            }
            // down
            down = 0;
            int downX = x;
            int downY = y;
            delta_x = 0;
            delta_y = 1;
            size = 0;
            while (board[downY + delta_y].charAt(downX + delta_x) != 'w' && size < bombSize) {
                downX += delta_x;
                downY += delta_y;
                bombed.add(new Point2D(downX, downY));
                size++;
                down++;
            }
            // left
            int leftX = x;
            int leftY = y;
            delta_x = -1;
            delta_y = 0;
            size = 0;
            left = 0;
            while (board[leftY + delta_y].charAt(leftX + delta_x) != 'w' && size < bombSize) {
                leftX += delta_x;
                leftY += delta_y;
                bombed.add(new Point2D(leftX, leftY));
                size++;
                left++;
            }
            // right
            int rightX = x;
            int rightY = y;
            delta_x = 1;
            delta_y = 0;
            size = 0;
            right = 0;
            while (board[rightY + delta_y].charAt(rightX + delta_x) != 'w' && size < bombSize) {
                rightX += delta_x;
                rightY += delta_y;
                bombed.add(new Point2D(rightX, rightY));
                size++;
                right++;
            }
            // check if any ammo was lost
            Point2D ammoLost = null;
            for (Point2D po : ammoList) {
                for (Point2D bo : bombed) {
                    if (po.equals(bo)) {
                        ammoLost = po;
                    }
                }
            }
            if (ammoLost != null) {
                ammoList.remove(ammoLost);
            }
            bomber.setBombSize(bombSize + 1);
            clientsDropBomb(x, y, up, down, left, right, name);
            new Timer(true).schedule(new TimerTask() {

                @Override
                public void run() {

                    for (Point2D po : bombed) {
                        int x = (int) po.getX();
                        int y = (int) po.getY();
                        for (Player player : players) {
                            if (player.getXpos() == x && player.getYpos() == y) {
                                playerShot = player;
                                int fromX = playerShot.getXpos();
                                int fromY = playerShot.getYpos();
                                playerShot.setDead(true);
                                bomber.addKill(1);
                                spawnPlayer(player);
                                clientsMovePlayer(fromX, fromY, playerShot.getXpos(),
                                    playerShot.getYpos(),
                                    Direction.UP, playerShot.getName());
                                clientsUpdateScore();
                            }
                        }
                    }
                    bombList.remove(point);
                }
            }, 2800);
        }
    }

    private static void spawnAmmo() {

        ammoTimer = new Timer();
        ammoTimer.schedule(new TimerTask() {

            @Override
            public void run() {

                if (players.size() >= 1 && ammoList.size() < 2) {
                    int x = 0;
                    int y = 0;
                    boolean spawned = false;
                    while (!spawned) {
                        // get a random x, y
                        x = (int) (Math.random() * (board.length - 2) + 1);
                        y = (int) (Math.random() * (board.length - 2) + 1);
                        if (board[y].charAt(x) != 'w' && getPlayerAt(x, y) == null) {
                            spawned = true;
                        }
                    }
                    ammoList.add(new Point2D(x, y));
                    clientsSpawnAmmo(x, y);
                }
            }
        }, 0, 10000);
    }

    public static void updateAmmo(String name, int ammo) {

        for (ServerThread t : threads) {
            if (t.playerName.equals(name)) {
                try {
                    t.updateAmmo(ammo);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void roundFinished(String name, int kills) throws InterruptedException {

        clientsRoundFinished(name, kills);
        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {

                roundStart();
                ammoList.clear();
            }
        }, 15000);
    }

    public static void roundStart() {

        clientsRoundStart();
        for (Player p : players) {
            p.setAmmo(2);
            p.setDead(false);
            spawnPlayer(p);
            winner = null;
            clientsSetupBoard();
        }
        clientsSpawnPlayers();
        clientsUpdateScore();
        ammoList.clear();
    }

    //
    // ServerThread call methods
    //
    public static void clientsRoundFinished(String name, int kills) {

        for (ServerThread t : threads) {
            try {
                t.roundFinished(name, kills);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void clientsRoundStart() {

        for (ServerThread t : threads) {
            try {
                t.roundStart();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Tell all server threads to update their client that a player has quit
     */
    public static void clientsRemovePlayer(int x, int y, String name) {

        for (ServerThread t : threads) {
            try {
                t.removePlayer(x, y, name);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Tell all server threads to update their client's scores
     */
    public static void clientsUpdateScore() {

        String scores = "";
        for (Player p : players) {
            scores += p.getName() + ":\t" + p.getKills() + " / " + p.getDeaths() + "#";
        }
        for (ServerThread t : threads) {
            try {
                t.updateScores(scores);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void clientsSpawnAmmo(int x, int y) {

        for (ServerThread t : threads) {
            try {
                t.spawnAmmo(x, y);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void clientsDropBomb(int centerX, int centerY, int up, int down, int left,
        int right, String name) {

        for (ServerThread t : threads) {
            try {
                t.dropBomb(centerX, centerY, up, down, left, right, name);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Tell all server threads to update their client that a player has spawned
     */
    public static void clientsSpawnPlayers() {

        for (ServerThread t : threads) {
            try {
                for (Player p : players) {
                    if (!p.isDead()) {
                        t.spawnPlayer(p.getXpos(), p.getYpos(), p.getName(), p.getAmmo(),
                            players.size());
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void clientsPlayerDead(int x, int y, String name) {

        for (ServerThread t : threads) {
            try {
                t.playerDead(x, y, name);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Tell all server threads to update their client that a player has moved
     */
    public static void clientsMovePlayer(int fromX, int fromY, int toX, int toY,
        Direction direction, String name) {

        for (ServerThread t : threads) {
            try {
                t.movePlayer(fromX, toX, fromY, toY, direction, name);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Tell all server threads to update their client that a player has shot
     */
    public static void clientsShot(int fromX, int fromY, int toX, int toY, Direction direction,
        String name, int ammo) {

        for (ServerThread t : threads) {
            try {
                t.playerShoot(fromX, fromY, toX, toY, direction, ammo, name);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void clientsSetupBoard() {

        for (ServerThread t : threads) {
            try {
                t.setupBoard();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
