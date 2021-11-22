package mazeGame;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class RoundFinishedWindow extends Stage {
    
    private Label timerLabel = new Label();
    private int seconds = 11;
    
    public RoundFinishedWindow(String name, int kills) {
        
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UNDECORATED);
        setResizable(false);
        setTitle("End of round");
        StackPane pane = new StackPane();
        pane.setPadding(new Insets(75, 0, 0, 0));
        pane.setBackground(new Background(new BackgroundFill(Color.BLACK, null, Insets.EMPTY)));
        Scene scene = new Scene(pane, 800, 600);
        setScene(scene);
        // show();
        //
        MediaPlayer count = new MediaPlayer(
            new Media(new File("src/mazeGame/Sound/countdown.mp3/").toURI().toString()));
        count.setVolume(.3);
        count.play();
        DropShadow ds = new DropShadow();
        ds.setOffsetY(10.0f);
        // ds.setOffsetX(3.0f);
        ds.setColor(Color.DARKRED);
        Image imgSpinner =
            new Image(getClass().getResourceAsStream("Image/spinner.gif"), 500, 500, false, false);
        Image imgCircle =
            new Image(getClass().getResourceAsStream("Image/colorCircle.gif"), 250, 250, false,
                false);
        ImageView spinner = new ImageView(imgSpinner);
        ImageView circle = new ImageView(imgCircle);
        Label lblWinner = new Label("Winner  is " + name + "!     (" + kills + " kills)");
        lblWinner.setFont(Font.font("IMPACT", FontWeight.BOLD, 60));
        lblWinner.setTextFill(Color.LIGHTGREY);
        lblWinner.setMinWidth(800);
        lblWinner.setAlignment(Pos.CENTER);
        lblWinner.setEffect(ds);
        Label lblCount = new Label("Next round in...");
        lblCount.setFont(Font.font("IMPACT", FontWeight.BOLD, 38));
        lblCount.setTextFill(Color.GREY);
        timerLabel.setTextFill(Color.YELLOW);
        timerLabel.setFont(Font.font("IMPACT", FontWeight.BOLD, 96));
        pane.getChildren().addAll(spinner, lblWinner, lblCount, circle, timerLabel);
        StackPane.setAlignment(lblWinner, Pos.BASELINE_CENTER);
        StackPane.setAlignment(lblCount, Pos.TOP_CENTER);
        StackPane.setAlignment(timerLabel, Pos.CENTER);
        StackPane.setAlignment(circle, Pos.CENTER);
        StackPane.setAlignment(spinner, Pos.BOTTOM_CENTER);
        StackPane.setMargin(lblCount, new Insets(25, 0, 0, 0));
        Timer t = new Timer(true);
        t.schedule(new TimerTask() {
            
            @Override
            public void run() {

                seconds--;
                if (seconds >= 0) {
                    Platform.runLater(() -> {
                        timerLabel.setText("" + seconds);
                    });
                }
                if (seconds == 9) {
                    Platform.runLater(() -> {
                        timerLabel.setTextFill(Color.GREEN);
                    });
                }
                else if (seconds == 8) {
                    Platform.runLater(() -> {
                        timerLabel.setTextFill(Color.BLUE);
                    });
                }
                else if (seconds == 7) {
                    Platform.runLater(() -> {
                        timerLabel.setTextFill(Color.HOTPINK);
                    });
                }
                else if (seconds == 6) {
                    Platform.runLater(() -> {
                        timerLabel.setTextFill(Color.RED);
                    });
                }
                else if (seconds == 5) {
                    Platform.runLater(() -> {
                        timerLabel.setTextFill(Color.YELLOW);
                    });
                }
                else if (seconds == 4) {
                    Platform.runLater(() -> {
                        timerLabel.setTextFill(Color.GREENYELLOW);
                    });
                }
                else if (seconds == 3) {
                    Platform.runLater(() -> {
                        timerLabel.setTextFill(Color.AQUAMARINE);
                    });
                }
                else if (seconds == 2) {
                    Platform.runLater(() -> {
                        timerLabel.setTextFill(Color.BLUE);
                    });
                }
                else if (seconds == 1) {
                    Platform.runLater(() -> {
                        timerLabel.setTextFill(Color.PURPLE);
                    });
                }
                else if (seconds == 0) {
                    Platform.runLater(() -> {
                        timerLabel.setTextFill(Color.RED);
                        timerLabel.setText("GO!");
                    });
                }
                else if (seconds == -1) {
                    t.cancel();
                    count.stop();
                    Platform.runLater(() -> {
                        hide();
                    });
                }
            }
        }, 1000, 1000);
    }
}
