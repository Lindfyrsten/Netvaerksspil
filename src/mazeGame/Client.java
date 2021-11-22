package mazeGame;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.GridPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Client extends Application {

    public static final int size = 25;
    public static final int scene_height = size * 20 + 100;
    public static final int scene_width = size * 20 + 200;
    private static Image image_blood, image_ammo, image_dead;
    private static Image image_bomb, bombCenter, bombVertical, bombHorizontal, bombVerticalUp,
        bombVerticalDown,
        bombHorizontalLeft, bombHorizontalRight;
    private static Image image_wall3, image_wall4, image_wall5;
    private static Image hero_right, hero_left, hero_up, hero_down, hero_rightRed,
        hero_leftRed, hero_upRed, hero_downRed;
    private static Image fireDown, fireHorizontal, fireLeft, fireRight, fireUp, fireVertical,
        fireWallEast,
        fireWallNorth, fireWallSouth, fireWallWest;
    private static ImageView bomb;
    private static ArrayList<Image> walls = new ArrayList<>();
    private static ArrayList<Point2D> shootQueue = new ArrayList<>();
    public static String name = "";
    private static Label[][] fields;
    private static TextArea scoreList;
    private static Text scoreLabel, ammoLabel, instructionsLabel, lblWaiting;
    private static Socket clientSocket;
    private static ClientThread thread;
    private static boolean shooting = false;
    private static boolean bombing = false;
    private static boolean dead = true;
    private static MediaPlayer mediaCoin;
    private static MediaPlayer mp;
    private static Media soundBombCharge, soundExplosion, soundCoin, soundShoot, soundScream,
        soundReload;
    private static GridPane grid;
    
    // -------------------------------------------
    // | Maze: (0,0) | Score: (1,0) |
    // |-----------------------------------------|
    // | boardGrid (0,1) | scorelist |
    // | | (1,1) |
    // -------------------------------------------
    @Override
    public void start(Stage primaryStage) throws Exception {
        
        // load images
        loadImages();
        // create gui elements
        grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(40, 10, 0, 10));
        lblWaiting = new Text();
        lblWaiting.setVisible(false);
        lblWaiting.setFont(Font.font("IMPACT", FontWeight.BOLD, 36));
        lblWaiting.setFill(Color.DARKRED);
        scoreLabel = new Text();
        scoreLabel.setVisible(false);
        scoreLabel.setFont(Font.font("IMPACT", FontWeight.BOLD, 36));
        scoreLabel.setFill(Color.DARKRED);
        ammoLabel = new Text();
        ammoLabel.setVisible(false);
        ammoLabel.setFont(Font.font("IMPACT", FontWeight.BOLD, 24));
        ammoLabel.setFill(Color.DARKRED);
        instructionsLabel = new Text("\tSPACE: Shoot\t\t\t\tB: Bomb\t\t\tEscape: Quit");
        instructionsLabel.setVisible(false);
        instructionsLabel.setFont(Font.font("IMPACT", FontWeight.BOLD, 20));
        instructionsLabel.setFill(Color.DARKRED);
        scoreList = new TextArea();
        scoreList.setFont(Font.font("IMPACT", FontWeight.BOLD, 20));
        scoreList.setEditable(false);
        scoreList.setMaxHeight(200);
        scoreList.setVisible(false);
        GridPane boardGrid = new GridPane();
        // adding our different wall images to the walls list, so we can choose random wall image for each wall later
        Collections.addAll(walls, image_wall3, image_wall4, image_wall5);
        BackgroundImage tableImage = new BackgroundImage(
            new Image(getClass().getResourceAsStream("Image/table.jpg/"), scene_width, scene_height,
                false, true),
            BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
            BackgroundSize.DEFAULT);
        Background table = new Background(tableImage);
        grid.setBackground(table);
        // creating a multidemensional array to act as our "board" that is 20x20 grids
        fields = new Label[20][20];
        for (int j = 0; j < 20; j++) {
            for (int i = 0; i < 20; i++) {
                // creating and inserting a blank label into each grid
                fields[i][j] = new Label("", null);
                boardGrid.add(fields[i][j], i, j);
            }
        }
        grid.add(lblWaiting, 0, 0);
        grid.add(boardGrid, 0, 1, 1, 3);
        grid.add(scoreLabel, 1, 1);
        grid.add(scoreList, 1, 2);
        grid.add(ammoLabel, 1, 3);
        grid.add(instructionsLabel, 0, 4);
        GridPane.setHalignment(scoreLabel, HPos.CENTER);
        GridPane.setValignment(scoreList, VPos.TOP);
        GridPane.setValignment(ammoLabel, VPos.TOP);
        GridPane.setHalignment(ammoLabel, HPos.CENTER);
        GridPane.setHalignment(lblWaiting, HPos.CENTER);
        GridPane.setValignment(lblWaiting, VPos.TOP);
        // create and load scene
        Scene scene = new Scene(grid, scene_width, scene_height);
        primaryStage.setScene(scene);
        primaryStage.show();
        // set key events
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            switch (event.getCode()) {
            case UP:
                try {
                    move(0, -1, Direction.UP);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case DOWN:
                try {
                    move(0, +1, Direction.DOWN);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case LEFT:
                try {
                    move(-1, 0, Direction.LEFT);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case RIGHT:
                try {
                    move(+1, 0, Direction.RIGHT);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case SPACE:
                try {
                    shoot();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case B:
                try {
                    dropBomb();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case ESCAPE:
                try {
                    quit();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
            }
        });
        // ask player for name and store it in the name variable
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("What's your name?");
        dialog.setContentText("Please enter your name:");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            name = result.get();
        }
        lblWaiting.setText("Waiting for other players to join");
        lblWaiting.setVisible(true);
    }

    private void move(int delta_x, int delta_y, Direction direction) throws Exception {

        // do not move if player is shooting
        if (!shooting && !dead) {
            // tell the thread we want to move and include delta_x, delta_y & in which direction
            thread.movePlayer(delta_x, delta_y, direction);
        }
    }

    private void shoot() throws Exception {

        // do not shoot if player is already within the shoot timeline from a previous shot
        if (!shooting && !dead) {
            // tell the thread we want to shoot and include the name of our client
            thread.shoot(name);
        }
    }

    private void dropBomb() throws IOException {

        if (!shooting && !bombing && !dead) {
            thread.dropBomb(name);
        }
    }

    private void quit() throws Exception {

        // tell the thread we are quitting
        thread.quit();
        Platform.exit();
    }

    /**
     * Create a wall in the grid
     *
     * @param x The x coordinate
     * @param y The y coordinate
     */
    public static void createWall(int x, int y) throws Exception {

        // choose a random wall image from walls list
        int random = (int) (Math.random() * walls.size());
        ImageView img = new ImageView(walls.get(random));
        img.setOpacity(.9);
        // change the graphics of the label located in x,y to the chosen image
        fields[x][y].setGraphic(img);
    }

    /**
     * Remove a player from the game
     *
     * @param x The x coordinate
     * @param y The y coordinate
     */
    public static void removePlayer(int x, int y) throws Exception {

        // player removed, so we replace his image with null
        fields[x][y].setGraphic(null);
    }

    /**
     * Spawn a player on the map
     *
     * @param x The x coordinate
     * @param y The y coordinate
     * @param playerName The name of the player
     */
    public static void playerSpawn(int x, int y, String playerName, int ammo, int players) {
        
        lblWaiting.setText("Waiting for round to finish");
        // check if player is you or opponent and choose the correct image
        if (name.equals(playerName)) {
            grid.getChildren().remove(lblWaiting);
            // lblWaiting.setVisible(false);
            fields[x][y].setGraphic(new ImageView(hero_up));
            // show GUI elements
            scoreList.setVisible(true);
            scoreLabel.setVisible(true);
            ammoLabel.setVisible(true);
            instructionsLabel.setVisible(true);
            ammoLabel.setText("Ammo: " + ammo);
            scoreLabel.setText("Scores");
        }
        else {
            fields[x][y].setGraphic(new ImageView(hero_upRed));
        }
    }

    /**
     * Player has moved
     *
     * @param fromX The x position player moved from
     * @param fromY The y position player moved from
     * @param toX The x position player moved to
     * @param toY The y position player moved to
     * @param direction The direction of the move
     * @param playerName The name of the player that moved
     */
    public static void playerMove(int fromX, int fromY, int toX, int toY, Direction direction,
        String playerName) {

        // change the graphics of the label located in x,y
        // player removed, so we replace his image with null
        if (fields[fromX][fromY].getGraphic() != bomb) {
            fields[fromX][fromY].setGraphic(null);
        }
        // change the graphics of the label depending on direction and wether the player is you or an opponent
        if (direction == Direction.RIGHT) {
            if (playerName.equals(name)) {
                fields[toX][toY].setGraphic(new ImageView(hero_right));
            }
            else {
                fields[toX][toY].setGraphic(new ImageView(hero_rightRed));
            }
        }
        if (direction == Direction.LEFT) {
            if (playerName.equals(name)) {
                fields[toX][toY].setGraphic(new ImageView(hero_left));
            }
            else {
                fields[toX][toY].setGraphic(new ImageView(hero_leftRed));
            }
        }
        if (direction == Direction.UP) {
            if (playerName.equals(name)) {
                fields[toX][toY].setGraphic(new ImageView(hero_up));
            }
            else {
                fields[toX][toY].setGraphic(new ImageView(hero_upRed));
            }
        }
        if (direction == Direction.DOWN) {
            if (playerName.equals(name)) {
                fields[toX][toY].setGraphic(new ImageView(hero_down));
            }
            else {
                fields[toX][toY].setGraphic(new ImageView(hero_downRed));
            }
        }
    }
    
    /**
     * Shoots with a player
     *
     * @param fromX The first x coordinate in the path
     * @param fromY The first y coordinate in the path
     * @param toX The last x coordinate in the path
     * @param toY The last y coordinate in the path
     * @param direction Direction of the shot
     * @param playerName The name of the shooter
     * @param shotX The x coordinate of the player shot : -1 if none.
     * @param shotY The y coordinate of the player shot : -1 if none.
     */
    public static void playerShoot(int fromX, int fromY, int toX, int toY, Direction direction,
        String playerName,
        int ammo, int shotX, int shotY) {

        MediaPlayer mediaShoot = new MediaPlayer(soundShoot);
        mediaShoot.setVolume(.5);
        mediaShoot.play();
        // if we are the shooter
        if (playerName.equals(name)) {
            // stop mediaPlayer first to play it from start again, in case it's already running
            mediaCoin.stop();
            shooting = true;
            ammoLabel.setText("Ammo: " + ammo);
            // if shotX > 0 it means we shot a player
            if (shotX > 0) {
                mediaCoin.play();
            }
        }
        // set x,y to the path start coordinates
        int x = fromX;
        int y = fromY;
        Image wall;
        Image fire;
        Image firePath;
        int delta_x, delta_y;
        // sets the image and delta_x/y depending on the direction of the shot
        if (direction == Direction.UP) {
            wall = fireWallNorth;
            fire = fireUp;
            firePath = fireVertical;
            delta_x = 0;
            delta_y = -1;
        }
        else if (direction == Direction.DOWN) {
            wall = fireWallSouth;
            fire = fireDown;
            firePath = fireVertical;
            delta_x = 0;
            delta_y = 1;
        }
        else if (direction == Direction.LEFT) {
            wall = fireWallWest;
            fire = fireLeft;
            firePath = fireHorizontal;
            delta_x = -1;
            delta_y = 0;
        }
        else {
            wall = fireWallEast;
            fire = fireRight;
            firePath = fireHorizontal;
            delta_x = 1;
            delta_y = 0;
        }
        // keep going until we meet the end coordinates
        while (x != toX || y != toY) {
            // if x,y is the first coordinate on the path we choose the fire image
            if (x == fromX && y == fromY) {
                fields[x][y].setGraphic(new ImageView(fire));
            }
            // otherwise we choose the path image
            else {
                fields[x][y].setGraphic(new ImageView(firePath));
            }
            x += delta_x;
            y += delta_y;
        }
        // loop ends before last coordinate of the path and x,y is now the coordinates of the last grid before a wall
        // so we set the wall image
        fields[x][y].setGraphic(new ImageView(wall));
        // saving x,y in global arraylist, so that we can retriev it later in the timeline, since timeline doesnt accept
        // local variables
        Point2D point = new Point2D(x, y);
        shootQueue.add(point);
        // remembers the index in the list for use later
        int index = shootQueue.indexOf(point);
        // sets up a timeline with a duration, so the shot remains visible for a certain time and disables us from
        // shooting again until that time is up
        Timeline shoot = new Timeline(new KeyFrame(Duration.seconds(.3), e -> {
            // retrieve the x,y coordinates we saved earlier
            int xx = (int) shootQueue.get(index).getX();
            int yy = (int) shootQueue.get(index).getY();
            // remove the label graphics we added earlier
            fields[xx][yy].setGraphic(null);
            // keep removing until we meet out path x,y start
            while (xx != fromX || yy != fromY) {
                xx -= delta_x;
                yy -= delta_y;
                fields[xx][yy].setGraphic(null);
            }
            // if a player is shot, replace graphics with a blood image
            if (shotX > 0) {
                fields[shotX][shotY].setGraphic(new ImageView(image_blood));
            }
        }));
        // play the timeline
        shoot.play();
        // when finished, we want to be able to shoot again
        shoot.setOnFinished(e -> {
            if (playerName.equals(name)) {
                shooting = false;
            }
        });
    }

    public static void playerBomb(int centerX, int centerY, int up, int down, int left, int right,
        String playerName) {

        if (playerName.equals(name)) {
            bombing = true;
        }
        MediaPlayer charge = new MediaPlayer(soundBombCharge);
        charge.setVolume(.5);
        MediaPlayer explode = new MediaPlayer(soundExplosion);
        explode.setVolume(.5);
        charge.play();
        bomb = new ImageView(image_bomb);
        fields[centerX][centerY].setGraphic(bomb);
        // System.out.println("Center: " + centerX + ", " + centerY);
        // System.out.println("Up: " + up);
        // System.out.println("Down: " + down);
        // System.out.println("Left: " + left);
        // System.out.println("Right: " + right);
        Timeline bomb = new Timeline(new KeyFrame(Duration.seconds(2.8), e -> {
            fields[centerX][centerY].setGraphic(new ImageView(bombCenter));
            int delta_x, delta_y;
            int x, y;
            int count;
            // up
            count = up;
            delta_x = 0;
            delta_y = -1;
            x = centerX;
            y = centerY;
            while (count > 0) {
                x += delta_x;
                y += delta_y;
                if (count == 1) {
                    fields[x][y].setGraphic(new ImageView(bombVerticalUp));
                }
                else {
                    fields[x][y].setGraphic(new ImageView(bombVertical));
                }
                count--;
            }
            // down
            count = down;
            delta_x = 0;
            delta_y = 1;
            x = centerX;
            y = centerY;
            while (count > 0) {
                x += delta_x;
                y += delta_y;
                if (count == 1) {
                    fields[x][y].setGraphic(new ImageView(bombVerticalDown));
                }
                else {
                    fields[x][y].setGraphic(new ImageView(bombVertical));
                }
                count--;
            }
            // left
            count = left;
            delta_x = -1;
            delta_y = 0;
            x = centerX;
            y = centerY;
            while (count > 0) {
                x += delta_x;
                y += delta_y;
                if (count == 1) {
                    fields[x][y].setGraphic(new ImageView(bombHorizontalLeft));
                }
                else {
                    fields[x][y].setGraphic(new ImageView(bombHorizontal));
                }
                count--;
            }
            // right
            count = right;
            delta_x = 1;
            delta_y = 0;
            x = centerX;
            y = centerY;
            while (count > 0) {
                x += delta_x;
                y += delta_y;
                if (count == 1) {
                    fields[x][y].setGraphic(new ImageView(bombHorizontalRight));
                }
                else {
                    fields[x][y].setGraphic(new ImageView(bombHorizontal));
                }
                count--;
            }
        }));
        Timeline remove = new Timeline(new KeyFrame(Duration.seconds(.5), e -> {
            int delta_x, delta_y;
            int x, y;
            int count;
            fields[centerX][centerY].setGraphic(null);
            // up
            count = up;
            delta_x = 0;
            delta_y = -1;
            x = centerX;
            y = centerY;
            while (count > 0) {
                x += delta_x;
                y += delta_y;
                fields[x][y].setGraphic(null);
                count--;
            }
            // down
            count = down;
            delta_x = 0;
            delta_y = 1;
            x = centerX;
            y = centerY;
            while (count > 0) {
                x += delta_x;
                y += delta_y;
                fields[x][y].setGraphic(null);
                count--;
            }
            // left
            count = left;
            delta_x = -1;
            delta_y = 0;
            x = centerX;
            y = centerY;
            while (count > 0) {
                x += delta_x;
                y += delta_y;
                fields[x][y].setGraphic(null);
                count--;
            }
            // right
            count = right;
            delta_x = 1;
            delta_y = 0;
            x = centerX;
            y = centerY;
            while (count > 0) {
                x += delta_x;
                y += delta_y;
                fields[x][y].setGraphic(null);
                count--;
            }
        }));
        bomb.play();
        bomb.setOnFinished(e -> {
            charge.stop();
            explode.play();
            remove.play();
            if (playerName.equals(name)) {
                bombing = false;
            }
        });
    }

    // }
    /**
     * Updates scoreArea to fit the current score of all players
     *
     * @param scores An array of strings containing playernames and their points
     */
    public static void updateScore(String[] scores) {

        StringBuffer b = new StringBuffer(100);
        for (int i = 1; i < scores.length; i++) {
            b.append(scores[i] + "\r\n");
        }
        scoreList.setText(b.toString());
    }
    private static int count;
    
    public static void roundFinished(String name, int kills) {
        
        count = 0;
        Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            
            @Override
            public void run() {
                
                count++;
                Platform.runLater(() -> {
                    RoundFinishedWindow rf = new RoundFinishedWindow(name, kills);
                    rf.showAndWait();
                });
                if (count == 1) {
                    timer.cancel();
                    timer.purge();
                    return;
                }
            }
        }, 3000, 3000);
    }

    public static void roundStart() {

        dead = false;
        clearBoard();
    }

    public static void clearBoard() {

        for (int j = 0; j < 20; j++) {
            for (int i = 0; i < 20; i++) {
                fields[i][j].setGraphic(null);
            }
        }
    }

    public static void updateAmmo(int ammo) {

        mp = new MediaPlayer(soundReload);
        mp.setVolume(.5);
        mp.play();
        ammoLabel.setText("Ammo: " + ammo);
    }

    public static void spawnAmmo(int x, int y) {

        fields[x][y].setGraphic(new ImageView(image_ammo));
    }

    public static void playerDead(int x, int y, String playerName) {

        mp = new MediaPlayer(soundScream);
        mp.setVolume(0.2);
        mp.play();
        if (playerName.equals(name)) {
            dead = true;
        }
        Timeline remove = new Timeline(new KeyFrame(Duration.seconds(.3), e -> {
            fields[x][y].setGraphic(new ImageView(image_dead));
        }));
        remove.play();
    }

    public void loadImages() {

        image_wall3 =
            new Image(getClass().getResourceAsStream("Image/wall3.png"), size, size, false, false);
        image_wall4 =
            new Image(getClass().getResourceAsStream("Image/wall4.png"), size, size, false, false);
        image_wall5 =
            new Image(getClass().getResourceAsStream("Image/wall5.png"), size, size, false, false);
        hero_right = new Image(getClass().getResourceAsStream("Image/heroRight.png"), size, size,
            false, false);
        hero_left = new Image(getClass().getResourceAsStream("Image/heroLeft.png"), size, size,
            false, false);
        hero_up =
            new Image(getClass().getResourceAsStream("Image/heroUp.png"), size, size, false, false);
        hero_down = new Image(getClass().getResourceAsStream("Image/heroDown.png"), size, size,
            false, false);
        hero_rightRed = new Image(getClass().getResourceAsStream("Image/heroRightRed.png"), size,
            size, false, false);
        hero_leftRed = new Image(getClass().getResourceAsStream("Image/heroLeftRed.png"), size,
            size, false, false);
        hero_upRed = new Image(getClass().getResourceAsStream("Image/heroUpRed.png"), size, size,
            false, false);
        hero_downRed = new Image(getClass().getResourceAsStream("Image/heroDownRed.png"), size,
            size, false, false);
        fireDown = new Image(getClass().getResourceAsStream("Image/fireDown.png"), size, size,
            false, false);
        fireUp =
            new Image(getClass().getResourceAsStream("Image/fireUp.png"), size, size, false, false);
        fireLeft = new Image(getClass().getResourceAsStream("Image/fireLeft.png"), size, size,
            false, false);
        fireRight = new Image(getClass().getResourceAsStream("Image/fireRight.png"), size, size,
            false, false);
        fireHorizontal =
            new Image(getClass().getResourceAsStream("Image/fireHorizontal.png"), size, size, false,
                false);
        fireVertical = new Image(getClass().getResourceAsStream("Image/fireVertical.png"), size,
            size, false, false);
        fireWallEast = new Image(getClass().getResourceAsStream("Image/fireWallEast.png"), size,
            size, false, false);
        fireWallNorth = new Image(getClass().getResourceAsStream("Image/fireWallNorth.png"), size,
            size, false, false);
        fireWallWest = new Image(getClass().getResourceAsStream("Image/fireWallWest.png"), size,
            size, false, false);
        fireWallSouth = new Image(getClass().getResourceAsStream("Image/fireWallSouth.png"), size,
            size, false, false);
        image_blood =
            new Image(getClass().getResourceAsStream("Image/blood.png"), size, size, false, false);
        image_ammo =
            new Image(getClass().getResourceAsStream("Image/ammo2.png"), size, size, false, false);
        image_dead =
            new Image(getClass().getResourceAsStream("Image/dead.png"), size, size, false, false);
        image_bomb =
            new Image(getClass().getResourceAsStream("Image/bomb.png"), size, size, false, false);
        bombCenter = new Image(getClass().getResourceAsStream("Image/bombCenter.png"), size, size,
            false, false);
        bombVertical = new Image(getClass().getResourceAsStream("Image/bombVertical.png"), size,
            size, false, false);
        bombVerticalUp =
            new Image(getClass().getResourceAsStream("Image/bombVerticalUp.png"), size, size, false,
                false);
        bombVerticalDown = new Image(getClass().getResourceAsStream("Image/bombVerticalDown.png"),
            size, size, false,
            false);
        bombHorizontal =
            new Image(getClass().getResourceAsStream("Image/bombHorizontal.png"), size, size, false,
                false);
        bombHorizontalLeft =
            new Image(getClass().getResourceAsStream("Image/bombHorizontalLeft.png"), size, size,
                false, false);
        bombHorizontalRight =
            new Image(getClass().getResourceAsStream("Image/bombHorizontalRight.png"), size, size,
                false, false);
        // audio
        soundShoot = new Media(new File("src/MazeGame/Sound/shoot.wav/").toURI().toString());
        soundCoin = new Media(new File("src/MazeGame/Sound/coin.wav/").toURI().toString());
        soundBombCharge =
            new Media(new File("src/MazeGame/Sound/BombCharge.mp3/").toURI().toString());
        soundExplosion =
            new Media(new File("src/MazeGame/Sound/Explosion.wav/").toURI().toString());
        soundScream = new Media(new File("src/MazeGame/Sound/scream.wav/").toURI().toString());
        soundReload = new Media(new File("src/MazeGame/Sound/reload.mp3/").toURI().toString());
        mediaCoin = new MediaPlayer(soundCoin);
    }

    public static void main(String[] args) throws Exception {

        try {
            clientSocket = new Socket("localhost", 6789);
            thread = new ClientThread(clientSocket);
            thread.start();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        launch(args);
    }
}
